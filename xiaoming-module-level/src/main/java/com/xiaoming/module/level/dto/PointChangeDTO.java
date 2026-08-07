package com.xiaoming.module.level.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 积分变动DTO
 */
@Data
public class PointChangeDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 类型: 1-增加 2-消耗
     */
    @NotNull(message = "类型不能为空")
    private Integer type;

    /**
     * 积分变动值
     */
    @NotNull(message = "积分值不能为空")
    @Positive(message = "积分必须大于0")
    private Integer point;

    /**
     * 行为
     */
    @NotNull(message = "行为不能为空")
    private String action;

    /**
     * 关联ID
     */
    private Long actionId;

    /**
     * 描述
     */
    private String description;
}
