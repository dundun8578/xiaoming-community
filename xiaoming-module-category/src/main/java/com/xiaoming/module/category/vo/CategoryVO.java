package com.xiaoming.module.category.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分类列表VO
 */
@Data
public class CategoryVO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类图标URL
     */
    private String icon;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 笔记数量
     */
    private Integer noteCount;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态: 0-禁用 1-启用
     */
    private Integer status;
}
