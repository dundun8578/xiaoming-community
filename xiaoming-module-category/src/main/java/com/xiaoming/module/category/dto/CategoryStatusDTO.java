package com.xiaoming.module.category.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 分类状态更新DTO
 */
@Data
public class CategoryStatusDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long id;

    /**
     * 状态: 0-禁用 1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
