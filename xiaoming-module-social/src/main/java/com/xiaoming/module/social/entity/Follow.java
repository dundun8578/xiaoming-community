package com.xiaoming.module.social.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注实体
 */
@Data
@TableName("follow")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long followUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
