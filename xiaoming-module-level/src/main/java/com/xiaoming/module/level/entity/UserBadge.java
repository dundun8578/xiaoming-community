package com.xiaoming.module.level.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户徽章实体
 */
@Data
@TableName("user_badge")
public class UserBadge {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 徽章ID
     */
    private Long badgeId;

    /**
     * 徽章名称
     */
    private String badgeName;

    /**
     * 徽章图标
     */
    private String badgeIcon;

    /**
     * 获得时间
     */
    private LocalDateTime obtainTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
