package com.xiaoming.module.interaction.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.common.exception.GlobalExceptionEnum;
import com.xiaoming.module.interaction.entity.Collect;
import com.xiaoming.module.interaction.mapper.CollectMapper;
import com.xiaoming.module.interaction.vo.CollectVO;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.user.entity.User;
import com.xiaoming.module.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCollect(Long userId, Long noteId) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .eq(Collect::getNoteId, noteId);

        Collect existCollect = getOne(wrapper);
        Note note = noteMapper.selectById(noteId);

        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(GlobalExceptionEnum.NOTE_NOT_FOUND);
        }

        if (existCollect != null) {
            // 已收藏，取消收藏
            removeById(existCollect.getId());
            note.setCollectCount(Math.max(0, note.getCollectCount() - 1));
        } else {
            // 未收藏，添加收藏
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setNoteId(noteId);
            save(collect);
            note.setCollectCount(note.getCollectCount() + 1);
        }

        noteMapper.updateById(note);
    }

    @Override
    public boolean isCollected(Long userId, Long noteId) {
        if (userId == null) return false;
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .eq(Collect::getNoteId, noteId);
        return count(wrapper) > 0;
    }

    @Override
    public List<CollectVO> getUserCollects(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Collect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collect::getUserId, userId)
               .orderByDesc(Collect::getCreateTime);

        Page<Collect> page = new Page<>(pageNum, pageSize);
        IPage<Collect> collectPage = page(page, wrapper);

        return collectPage.getRecords().stream()
                .map(collect -> {
                    CollectVO vo = new CollectVO();
                    BeanUtils.copyProperties(collect, vo);

                    Note note = noteMapper.selectById(collect.getNoteId());
                    if (note != null) {
                        vo.setTitle(note.getTitle());
                        vo.setCoverImage(note.getCoverImage());
                        User author = userMapper.selectById(note.getUserId());
                        if (author != null) {
                            vo.setAuthorId(author.getId());
                            vo.setAuthorName(author.getNickname());
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }
}
