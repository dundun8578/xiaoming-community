package com.xiaoming.module.level.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分实体
 */
@Data
@TableName("user_point")
public class UserPoint {

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
     * 总经验值
     */
    private Integer totalExp;

    /**
     * 当前等级
     */
    private Integer currentLevel;

    /**
     * 总积分
     */
    private Integer totalPoint;

    /**
     * 可用积分
     */
    private Integer availablePoint;

    /**
     * 已使用积分
     */
    private Integer usedPoint;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
