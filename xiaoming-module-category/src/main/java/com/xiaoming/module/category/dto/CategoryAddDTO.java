package com.xiaoming.module.category.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 新增/更新分类DTO
 */
@Data
public class CategoryAddDTO {

    /**
     * ID(更新时需要)
     */
    private Long id;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称长度不能超过32")
    private String name;

    /**
     * 分类图标URL
     */
    @Size(max = 256, message = "图标URL长度不能超过256")
    private String icon;

    /**
     * 分类描述
     */
    @Size(max = 256, message = "描述长度不能超过256")
    private String description;

    /**
     * 排序
     */
    private Integer sort;
}
