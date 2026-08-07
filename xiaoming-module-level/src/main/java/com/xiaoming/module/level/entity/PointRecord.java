package com.xiaoming.module.level.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分明细实体
 */
@Data
@TableName("point_record")
public class PointRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型: 1-增加 2-消耗
     */
    private Integer type;

    /**
     * 积分变动值
     */
    private Integer point;

    /**
     * 行为
     */
    private String action;

    /**
     * 关联ID
     */
    private Long actionId;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
