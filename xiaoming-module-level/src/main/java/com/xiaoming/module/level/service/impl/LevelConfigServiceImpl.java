package com.xiaoming.module.level.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoming.module.level.entity.LevelConfig;
import com.xiaoming.module.level.entity.UserPoint;
import com.xiaoming.module.level.mapper.LevelConfigMapper;
import com.xiaoming.module.level.service.LevelConfigService;
import com.xiaoming.module.level.service.UserPointService;
import com.xiaoming.module.level.vo.UserLevelVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 等级配置服务实现
 */
@Service
public class LevelConfigServiceImpl extends ServiceImpl<LevelConfigMapper, LevelConfig> implements LevelConfigService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * UserPointServiceImpl 用构造器注入了 LevelConfigService，两边互相依赖会形成循环，
     * 这里延迟加载打断循环（真正调用时才解析代理）。
     */
    @Lazy
    @Autowired
    private UserPointService userPointService;

    @Override
    public LevelConfig getByLevel(Integer level) {
        LambdaQueryWrapper<LevelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LevelConfig::getLevel, level);
        return this.getOne(wrapper);
    }

    @Override
    public LevelConfig getLevelByExp(Integer exp) {
        LambdaQueryWrapper<LevelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(LevelConfig::getMinExp, exp)
               .ge(LevelConfig::getMaxExp, exp);
        return this.getOne(wrapper);
    }

    @Override
    public List<LevelConfig> list() {
        LambdaQueryWrapper<LevelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(LevelConfig::getLevel);
        return this.list(wrapper);
    }

    @Override
    public UserLevelVO getUserLevelInfo(Long userId) {
        UserPoint userPoint = userPointService.getByUserId(userId);
        if (userPoint == null) {
            return null;
        }

        LevelConfig currentConfig = getByLevel(userPoint.getCurrentLevel());
        if (currentConfig == null) {
            return null;
        }

        int progress = calculateProgress(userPoint.getTotalExp(), currentConfig);

        LevelConfig nextConfig = getByLevel(userPoint.getCurrentLevel() + 1);
        String nextLevelName = null;
        Integer nextLevelExp = null;
        if (nextConfig != null) {
            nextLevelName = nextConfig.getName();
            nextLevelExp = nextConfig.getMinExp() - userPoint.getTotalExp();
        }

        UserLevelVO vo = new UserLevelVO();
        vo.setUserId(userId);
        vo.setCurrentLevel(userPoint.getCurrentLevel());
        vo.setLevelName(currentConfig.getName());
        vo.setLevelIcon(currentConfig.getIcon());
        vo.setTotalExp(userPoint.getTotalExp());
        vo.setProgress(progress);
        vo.setNextLevelName(nextLevelName);
        vo.setNextLevelExp(nextLevelExp);
        vo.setTotalPoint(userPoint.getTotalPoint());
        vo.setAvailablePoint(userPoint.getAvailablePoint());
        vo.setPrivilege(parsePrivilege(currentConfig.getPrivilege()));

        return vo;
    }

    @Override
    public Integer calculateProgress(Integer currentExp, LevelConfig currentLevel) {
        if (currentLevel == null || currentLevel.getMinExp().equals(currentLevel.getMaxExp())) {
            return 100;
        }
        int range = currentLevel.getMaxExp() - currentLevel.getMinExp();
        int progress = (currentExp - currentLevel.getMinExp()) * 100 / range;
        return Math.min(Math.max(progress, 0), 100);
    }

    /**
     * 解析等级特权JSON（原实现依赖 fastjson2，这里统一改用项目已有的 Jackson）
     */
    private Map<String, Object> parsePrivilege(String privilege) {
        if (privilege == null || privilege.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(privilege, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}
