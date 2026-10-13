package com.xiaoming.module.note.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private ObjectMapper objectMapper;

    /** 热榜缓存 key 前缀（后面要拼 pageSize，避免不同页大小共用同一份缓存导致数据错乱） */
    private static final String HOT_NOTES_KEY_PREFIX = "hot:notes:size:";

    /** 热榜缓存基础过期时间（秒） */
    private static final long HOT_NOTES_TTL = 300;

    /** 超过这个页大小就不走缓存，防止用超大 pageSize 刷出大量无意义 key（防缓存穿透） */
    private static final int HOT_NOTES_CACHE_MAX_SIZE = 100;

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
        evictHotNotesCache();

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
        evictHotNotesCache();

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
        evictHotNotesCache();
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
        String cacheKey = buildHotNotesKey(pageSize);

        // 1. 先查缓存：命中就返回（Redis 连不上或数据损坏会自动降级，往下走数据库）
        if (cacheKey != null) {
            List<NoteVO> cachedList = readHotNotesFromCache(cacheKey);
            if (cachedList != null) {
                return cachedList;
            }
        }

        // 2. 未命中：查数据库
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getStatus, 1)
               .eq(Note::getIsDeleted, 0)
               .orderByDesc(Note::getHotScore)
               .last("LIMIT " + pageSize);

        List<Note> notes = list(wrapper);
        List<NoteVO> result = notes.stream()
                .map(note -> convertToVO(note, null))
                .collect(Collectors.toList());

        // 3. 回填缓存：TTL 叠加随机抖动，避免大量 key 同一秒集体过期（缓存雪崩）
        if (cacheKey != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey,
                        objectMapper.writeValueAsString(result),
                        randomTtl(HOT_NOTES_TTL), TimeUnit.SECONDS);
            } catch (Exception e) {
                // 缓存写失败不影响主流程，最坏情况只是这次没缓存上
            }
        }

        return result;
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
            evictHotNotesCache();
        }
    }

    // ------------------------------------------------------------------ 缓存相关私有方法

    /**
     * 构造热榜缓存 key。返回 null 表示本次不走缓存（pageSize 为空或过大）。
     */
    private String buildHotNotesKey(Integer pageSize) {
        if (pageSize == null || pageSize <= 0 || pageSize > HOT_NOTES_CACHE_MAX_SIZE) {
            return null;
        }
        return HOT_NOTES_KEY_PREFIX + pageSize;
    }

    /**
     * 在基础 TTL 上叠加 0~60 秒的随机抖动，防止大量 key 同一秒集体过期（缓存雪崩）。
     */
    private long randomTtl(long baseSeconds) {
        return baseSeconds + (long) (Math.random() * 60);
    }

    /**
     * 从缓存读热榜。任何异常（Redis 连不上、JSON 反序列化失败）都返回 null，
     * 让调用方降级去查数据库 —— 缓存是加速手段，绝不能因为它把主流程拖垮。
     */
    private List<NoteVO> readHotNotesFromCache(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached == null) {
                return null;
            }
            return objectMapper.readValue(cached,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, NoteVO.class));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清除热榜缓存。key 里带了 pageSize，可能存在多个变体，所以按前缀批量删除。
     * 注意：数据量大的生产环境应改用 scan 迭代或维护 key 集合，
     * 避免 keys 命令在单线程 Redis 上造成短暂阻塞；本项目数据量小，keys 足够。
     */
    private void evictHotNotesCache() {
        try {
            Set<String> keys = redisTemplate.keys(HOT_NOTES_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // 缓存删不掉不影响业务正确性（数据已经写进库了），最坏只是短期内读到旧缓存
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
