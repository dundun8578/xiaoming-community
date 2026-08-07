package com.xiaoming.module.level.service;

import com.xiaoming.module.level.entity.UserPoint;

/**
 * 用户积分服务接口
 */
public interface UserPointService {

    /**
     * 根据用户ID查询积分信息
     */
    UserPoint getByUserId(Long userId);

    /**
     * 增加积分
     */
    boolean addPoint(Long userId, Integer point, String action, Long actionId, String description);

    /**
     * 消耗积分
     */
    boolean consumePoint(Long userId, Integer point, String action, Long actionId, String description);

    /**
     * 获取用户可用积分
     */
    Integer getAvailablePoint(Long userId);
}
