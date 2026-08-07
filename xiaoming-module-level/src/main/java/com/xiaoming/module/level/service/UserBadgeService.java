package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.UserBadge;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 用户徽章服务接口
 */
public interface UserBadgeService extends IService<UserBadge> {

    /**
     * 为用户添加徽章
     */
    boolean addBadge(Long userId, Long badgeId, String badgeName, String badgeIcon);

    /**
     * 查询用户拥有的徽章
     */
    List<UserBadge> getUserBadges(Long userId);

    /**
     * 检查用户是否拥有徽章
     */
    boolean hasBadge(Long userId, Long badgeId);
}
