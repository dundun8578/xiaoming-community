package com.xiaoming.module.level.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 等级配置实体
 */
@Data
@TableName("level_config")
public class LevelConfig {

    /**
     * 等级ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级(1-10)
     */
    private Integer level;

    /**
     * 等级名称
     */
    private String name;

    /**
     * 最小经验值
     */
    private Integer minExp;

    /**
     * 最大经验值
     */
    private Integer maxExp;

    /**
     * 等级图标URL
     */
    private String icon;

    /**
     * 徽章URL
     */
    private String badge;

    /**
     * 特权描述(JSON)
     */
    private String privilege;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
