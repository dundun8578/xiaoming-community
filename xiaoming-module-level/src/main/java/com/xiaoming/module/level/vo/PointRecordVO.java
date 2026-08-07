package com.xiaoming.module.level.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分明细VO
 */
@Data
public class PointRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 类型: 1-增加 2-消耗
     */
    private Integer type;

    /**
     * 积分变动值
     */
    private Integer point;

    /**
     * 行为描述
     */
    private String action;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
