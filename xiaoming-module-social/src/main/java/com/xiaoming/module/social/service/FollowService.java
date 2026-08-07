package com.xiaoming.module.social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoming.module.social.entity.Follow;
import java.util.List;

/**
 * 关注服务
 */
public interface FollowService extends IService<Follow> {

    /**
     * 关注/取消关注
     */
    void toggleFollow(Long userId, Long followUserId);

    /**
     * 检查是否已关注
     */
    boolean isFollowed(Long userId, Long followUserId);

    /**
     * 获取关注列表
     */
    List<Follow> getFollowingList(Long userId);

    /**
     * 获取粉丝列表
     */
    List<Follow> getFollowerList(Long userId);
}
