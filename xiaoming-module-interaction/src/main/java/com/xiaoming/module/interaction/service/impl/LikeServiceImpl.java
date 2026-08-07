package com.xiaoming.module.interaction.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.module.interaction.entity.Like;
import com.xiaoming.module.interaction.entity.Collect;
import com.xiaoming.module.interaction.mapper.LikeMapper;
import com.xiaoming.module.interaction.mapper.CollectMapper;
import com.xiaoming.module.interaction.service.LikeService;
import com.xiaoming.module.interaction.service.CollectService;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 点赞服务实现
 */
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private CollectService collectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, Long noteId) {
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getNoteId, noteId);

        Like existLike = getOne(wrapper);
        Note note = noteMapper.selectById(noteId);

        if (note == null || note.getIsDeleted() == 1) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }

        if (existLike != null) {
            // 已点赞，取消点赞
            removeById(existLike.getId());
            note.setLikeCount(Math.max(0, note.getLikeCount() - 1));
        } else {
            // 未点赞，添加点赞
            Like like = new Like();
            like.setUserId(userId);
            like.setNoteId(noteId);
            save(like);
            note.setLikeCount(note.getLikeCount() + 1);
        }

        noteMapper.updateById(note);
    }

    @Override
    public boolean isLiked(Long userId, Long noteId) {
        if (userId == null) return false;
        LambdaQueryWrapper<Like> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Like::getUserId, userId)
               .eq(Like::getNoteId, noteId);
        return count(wrapper) > 0;
    }

    @Override
    public boolean getLikeStatus(Long userId, Long noteId) {
        return isLiked(userId, noteId);
    }
}
