package com.xiaoming.module.level.service.impl;

import com.xiaoming.module.level.entity.LevelConfig;
import com.xiaoming.module.level.entity.UserPoint;
import com.xiaoming.module.level.mapper.UserPointMapper;
import com.xiaoming.module.level.mapper.PointRecordMapper;
import com.xiaoming.module.level.mapper.UserBadgeMapper;
import com.xiaoming.module.level.entity.UserBadge;
import com.xiaoming.module.level.entity.PointRecord;
import com.xiaoming.module.level.service.LevelConfigService;
import com.xiaoming.module.level.service.PointRecordService;
import com.xiaoming.module.level.service.UserBadgeService;
import com.xiaoming.module.level.service.UserPointService;
import com.xiaoming.module.level.vo.UserLevelVO;
import com.xiaoming.module.level.vo.PointRecordVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户积分服务实现
 */
@Service
public class UserPointServiceImpl extends ServiceImpl<UserPointMapper, UserPoint> implements UserPointService {

    private final PointRecordService pointRecordService;
    private final LevelConfigService levelConfigService;
    private final UserBadgeService userBadgeService;

    public UserPointServiceImpl(PointRecordService pointRecordService,
                                LevelConfigService levelConfigService,
                                UserBadgeService userBadgeService) {
        this.pointRecordService = pointRecordService;
        this.levelConfigService = levelConfigService;
        this.userBadgeService = userBadgeService;
    }

    @Override
    public UserPoint getByUserId(Long userId) {
        return this.lambdaQuery()
                .eq(UserPoint::getUserId, userId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPoint(Long userId, Integer point, String action, Long actionId, String description) {
        UserPoint userPoint = getByUserId(userId);
        if (userPoint == null) {
            userPoint = new UserPoint();
            userPoint.setUserId(userId);
            userPoint.setTotalExp(0);
            userPoint.setCurrentLevel(1);
            userPoint.setTotalPoint(0);
            userPoint.setAvailablePoint(0);
            userPoint.setUsedPoint(0);
        }

        int oldLevel = userPoint.getCurrentLevel();

        userPoint.setTotalExp(userPoint.getTotalExp() + point);
        userPoint.setTotalPoint(userPoint.getTotalPoint() + point);
        userPoint.setAvailablePoint(userPoint.getAvailablePoint() + point);

        LevelConfig newLevel = levelConfigService.getLevelByExp(userPoint.getTotalExp());
        if (newLevel != null && !newLevel.getLevel().equals(oldLevel)) {
            userPoint.setCurrentLevel(newLevel.getLevel());
        }

        this.saveOrUpdate(userPoint);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setType(1);
        record.setPoint(point);
        record.setAction(action);
        record.setActionId(actionId);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordService.save(record);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean consumePoint(Long userId, Integer point, String action, Long actionId, String description) {
        UserPoint userPoint = getByUserId(userId);
        if (userPoint == null || userPoint.getAvailablePoint() < point) {
            return false;
        }

        userPoint.setAvailablePoint(userPoint.getAvailablePoint() - point);
        userPoint.setUsedPoint(userPoint.getUsedPoint() + point);
        this.updateById(userPoint);

        PointRecord record = new PointRecord();
        record.setUserId(userId);
        record.setType(2);
        record.setPoint(point);
        record.setAction(action);
        record.setActionId(actionId);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointRecordService.save(record);

        return true;
    }

    @Override
    public Integer getAvailablePoint(Long userId) {
        UserPoint userPoint = getByUserId(userId);
        return userPoint != null ? userPoint.getAvailablePoint() : 0;
    }

    /**
     * 构建用户等级信息VO
     */
    public UserLevelVO buildUserLevelVO(Long userId) {
        UserPoint userPoint = getByUserId(userId);
        if (userPoint == null) {
            return null;
        }

        LevelConfig currentConfig = levelConfigService.getByLevel(userPoint.getCurrentLevel());
        if (currentConfig == null) {
            return null;
        }

        int progress = levelConfigService.calculateProgress(userPoint.getTotalExp(), currentConfig);

        LevelConfig nextConfig = levelConfigService.getByLevel(userPoint.getCurrentLevel() + 1);
        int nextLevelExp = 0;
        if (nextConfig != null) {
            nextLevelExp = nextConfig.getMinExp() - userPoint.getTotalExp();
        }

        return null;
    }
}
