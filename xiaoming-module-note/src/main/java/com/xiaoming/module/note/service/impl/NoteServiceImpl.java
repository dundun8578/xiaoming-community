package com.xiaoming.module.note.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.exception.GlobalExceptionEnum;
import com.xiaoming.module.note.dto.NotePublishDTO;
import com.xiaoming.module.note.dto.NoteUpdateDTO;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.entity.NoteImage;
import com.xiaoming.module.note.entity.NoteVideo;
import com.xiaoming.module.note.mapper.NoteImageMapper;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.note.mapper.NoteVideoMapper;
import com.xiaoming.module.note.vo.NoteImageVO;
import com.xiaoming.module.note.vo.NoteVO;
import com.xiaoming.module.note.vo.NoteVideoVO;
import com.xiaoming.module.user.entity.User;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 笔记服务实现
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Autowired
    private NoteImageMapper noteImageMapper;

    @Autowired
    private NoteVideoMapper noteVideoMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String HOT_NOTES_KEY = "hot:notes";
    private static final long HOT_NOTES_TTL = 300; // 5分钟

    @Override
    public NoteVO publishNote(Long userId, NotePublishDTO publishDTO) {
        // 创建笔记
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(publishDTO.getTitle());
        note.setContent(publishDTO.getContent());
        note.setCoverImage(publishDTO.getCoverImage());
        note.setType(publishDTO.getType());
        note.setStatus(1); // 已发布
        note.setTags(publishDTO.getTags());
        note.setPublishTime(LocalDateTime.now());
        note.setLikeCount(0);
        note.setCollectCount(0);
        note.setCommentCount(0);
        note.setViewCount(0);
        note.setHotScore(0L);
        save(note);

        // 清除热门笔记缓存
        redisTemplate.delete(HOT_NOTES_KEY);

        // 转换为VO
        return convertToVO(note, userId);
    }

    @Override
    public NoteVO updateNote(Long userId, Long noteId, NoteUpdateDTO updateDTO) {
        // 查询笔记
        Note note = getById(noteId);
        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_OWNER);
        }

        // 更新字段
        if (updateDTO.getTitle() != null) note.setTitle(updateDTO.getTitle());
        if (updateDTO.getContent() != null) note.setContent(updateDTO.getContent());
        if (updateDTO.getCoverImage() != null) note.setCoverImage(updateDTO.getCoverImage());
        if (updateDTO.getTags() != null) note.setTags(updateDTO.getTags());
        note.setUpdateTime(LocalDateTime.now());
        updateById(note);

        // 清除热门笔记缓存
        redisTemplate.delete(HOT_NOTES_KEY);

        return convertToVO(note, userId);
    }

    @Override
    public void deleteNote(Long userId, Long noteId) {
        Note note = getById(noteId);
        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_OWNER);
        }

        // 逻辑删除
        note.setIsDeleted(1);
        note.setStatus(2); // 已下架
        updateById(note);

        // 清除热门笔记缓存
        redisTemplate.delete(HOT_NOTES_KEY);
    }

    @Override
    public NoteVO getNoteDetail(Long noteId, Long currentUserId) {
        Note note = getById(noteId);
        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        }
        if (note.getStatus() != 1) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_DELETED);
        }

        // 增加浏览数
        note.setViewCount(note.getViewCount() + 1);
        updateById(note);

        // 转换VO
        return convertToVO(note, currentUserId);
    }

    @Override
    public List<NoteVO> getUserNotes(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getStatus, 1)
               .eq(Note::getIsDeleted, 0)
               .orderByDesc(Note::getPublishTime);

        Page<Note> page = new Page<>(pageNum, pageSize);
        IPage<Note> notePage = page(page, wrapper);

        return notePage.getRecords().stream()
                .map(note -> convertToVO(note, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteVO> getHotNotes(Integer pageNum, Integer pageSize) {
        // 先尝试从Redis获取
        String cachedNotes = redisTemplate.opsForValue().get(HOT_NOTES_KEY);
        if (cachedNotes != null) {
            // TODO: 反序列化JSON
            // 这里暂时返回空列表
        }

        // 查询热门笔记
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .eq(Note::getIsDeleted, 0)
               .orderByDesc(Note::getHotScore)
               .last("LIMIT " + pageSize);

        List<Note> notes = list(wrapper);
        return notes.stream()
                .map(note -> convertToVO(note, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteVO> getRecommendNotes(Long userId, Integer pageNum, Integer pageSize) {
        // TODO: Step8 ES搜索模块完成后实现个性化推荐
        return getHotNotes(pageNum, pageSize);
    }

    @Override
    public void increaseViewCount(Long noteId) {
        Note note = getById(noteId);
        if (note != null) {
            note.setViewCount(note.getViewCount() + 1);
            updateById(note);
        }
    }

    @Override
    public void updateHotScore(Long noteId) {
        Note note = getById(noteId);
        if (note != null) {
            // 热度算法: 浏览*1 + 点赞*5 + 收藏*10 + 评论*3
            long hotScore = note.getViewCount() * 1L
                    + note.getLikeCount() * 5L
                    + note.getCollectCount() * 10L
                    + note.getCommentCount() * 3L;
            note.setHotScore(hotScore);
            updateById(note);

            // 清除热门笔记缓存
            redisTemplate.delete(HOT_NOTES_KEY);
        }
    }

    /**
     * 转换为VO
     */
    private NoteVO convertToVO(Note note, Long currentUserId) {
        NoteVO vo = new NoteVO();
        BeanUtils.copyProperties(note, vo);

        // 作者信息
        UserVO userVO = userService.getUserInfo(note.getUserId());
        vo.setAuthorName(userVO.getNickname());
        vo.setAuthorAvatar(userVO.getAvatar());

        // 图片列表
        List<NoteImage> images = noteImageMapper.selectList(
                new LambdaQueryWrapper<NoteImage>().eq(NoteImage::getNoteId, note.getId()).orderByAsc(NoteImage::getSort)
        );
        if (!images.isEmpty()) {
            vo.setImages(images.stream().map(img -> {
                NoteImageVO imgVO = new NoteImageVO();
                BeanUtils.copyProperties(img, imgVO);
                return imgVO;
            }).collect(Collectors.toList()));
        }

        // 视频信息
        NoteVideo video = noteVideoMapper.selectOne(
                new LambdaQueryWrapper<NoteVideo>().eq(NoteVideo::getNoteId, note.getId())
        );
        if (video != null) {
            NoteVideoVO videoVO = new NoteVideoVO();
            BeanUtils.copyProperties(video, videoVO);
            vo.setVideo(videoVO);
        }

        // 点赞状态和收藏状态
        if (currentUserId != null) {
            // TODO: Step6 完成后从互动模块查询
            vo.setIsLiked(false);
            vo.setIsCollected(false);
        }

        return vo;
    }
}
