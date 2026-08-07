package com.xiaoming.module.level.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户等级信息VO
 */
@Data
public class UserLevelVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前等级
     */
    private Integer currentLevel;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级图标
     */
    private String levelIcon;

    /**
     * 总经验值
     */
    private Integer totalExp;

    /**
     * 当前等级进度(0-100)
     */
    private Integer progress;

    /**
     * 下一等级名称
     */
    private String nextLevelName;

    /**
     * 距离下一等级还需经验
     */
    private Integer nextLevelExp;

    /**
     * 总积分
     */
    private Integer totalPoint;

    /**
     * 可用积分
     */
    private Integer availablePoint;

    /**
     * 徽章列表
     */
    private List<Map<String, Object>> badges;

    /**
     * 特权信息
     */
    private Map<String, Object> privilege;
}
