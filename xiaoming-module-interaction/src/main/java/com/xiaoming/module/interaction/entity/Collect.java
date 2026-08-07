package com.xiaoming.module.interaction.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@TableName("collect")
public class Collect implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long noteId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
