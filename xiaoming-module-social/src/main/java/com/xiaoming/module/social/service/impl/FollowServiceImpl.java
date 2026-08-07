package com.xiaoming.module.social.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoming.common.constant.ResultCode;
import com.xiaoming.common.exception.BusinessException;
import com.xiaoming.module.mq.producer.NoteMessageProducer;
import com.xiaoming.module.note.entity.Note;
import com.xiaoming.module.note.mapper.NoteMapper;
import com.xiaoming.module.social.entity.Follow;
import com.xiaoming.module.social.mapper.FollowMapper;
import com.xiaoming.module.social.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 关注服务实现
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Autowired
    private NoteMapper noteMapper;

    @Autowired
    private NoteMessageProducer noteMessageProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleFollow(Long userId, Long followUserId) {
        if (userId.equals(followUserId)) {
            throw new BusinessException(ResultCode.CANNOT_FOLLOW_SELF);
        }

        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
               .eq(Follow::getFollowUserId, followUserId);

        Follow existFollow = getOne(wrapper);
        if (existFollow != null) {
            // 已关注，取消关注
            removeById(existFollow.getId());
        } else {
            // 未关注，添加关注
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);

            // 发送关注成功消息给被关注者
            // TODO: 完善消息模块后发送系统通知
        }
    }

    @Override
    public boolean isFollowed(Long userId, Long followUserId) {
        if (userId == null) return false;
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
               .eq(Follow::getFollowUserId, followUserId);
        return count(wrapper) > 0;
    }

    @Override
    public List<Follow> getFollowingList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getUserId, userId)
               .orderByDesc(Follow::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Follow> getFollowerList(Long userId) {
        LambdaQueryWrapper<Follow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Follow::getFollowUserId, userId)
               .orderByDesc(Follow::getCreateTime);
        return list(wrapper);
    }
}
