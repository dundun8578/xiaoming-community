package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.LevelConfig;
import com.xiaoming.module.level.entity.UserPoint;
import com.xiaoming.module.level.vo.UserLevelVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 等级配置服务接口
 */
public interface LevelConfigService extends IService<LevelConfig> {

    /**
     * 根据等级查询配置
     */
    LevelConfig getByLevel(Integer level);

    /**
     * 根据经验值查询对应等级
     */
    LevelConfig getLevelByExp(Integer exp);

    /**
     * 获取用户等级信息
     */
    UserLevelVO getUserLevelInfo(Long userId);

    /**
     * 计算等级进度
     */
    Integer calculateProgress(Integer currentExp, LevelConfig currentLevel);
}
