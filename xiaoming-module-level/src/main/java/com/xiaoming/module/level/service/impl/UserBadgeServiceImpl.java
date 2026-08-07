package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.UserBadge;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoming.module.level.mapper.UserBadgeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户徽章服务实现
 */
@Service
public class UserBadgeServiceImpl extends ServiceImpl<UserBadgeMapper, UserBadge> implements UserBadgeService {

    @Override
    public boolean addBadge(Long userId, Long badgeId, String badgeName, String badgeIcon) {
        if (hasBadge(userId, badgeId)) {
            return true;
        }

        UserBadge userBadge = new UserBadge();
        userBadge.setUserId(userId);
        userBadge.setBadgeId(badgeId);
        userBadge.setBadgeName(badgeName);
        userBadge.setBadgeIcon(badgeIcon);
        userBadge.setObtainTime(java.time.LocalDateTime.now());
        userBadge.setCreateTime(java.time.LocalDateTime.now());
        return this.save(userBadge);
    }

    @Override
    public List<UserBadge> getUserBadges(Long userId) {
        return this.lambdaQuery()
                .eq(UserBadge::getUserId, userId)
                .orderByDesc(UserBadge::getObtainTime)
                .list();
    }

    @Override
    public boolean hasBadge(Long userId, Long badgeId) {
        return this.lambdaQuery()
                .eq(UserBadge::getUserId, userId)
                .eq(UserBadge::getBadgeId, badgeId)
                .count() > 0;
    }
}
