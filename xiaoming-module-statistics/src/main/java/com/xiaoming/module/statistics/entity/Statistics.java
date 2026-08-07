package com.xiaoming.module.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据统计表
 * 用于存储各维度的统计数据
 */
@Data
@TableName("statistics")
public class Statistics implements Serializable {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 统计日期 (yyyy-MM-dd)
     */
    private String statDate;

    /**
     * 统计类型
     * 1-用户统计 2-笔记统计 3-互动统计 4-系统统计
     */
    private Integer statType;

    /**
     * 维度 (如: 用户ID、笔记ID等)
     */
    private Long dimensionId;

    /**
     * 统计项 (JSON格式: {"key": value, ...})
     */
    private String items;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
