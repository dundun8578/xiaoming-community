package com.xiaoming.module.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 笔记视频实体
 */
@Data
@TableName("note_video")
public class NoteVideo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 视频ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 视频URL
     */
    private String videoUrl;

    /**
     * 视频封面URL
     */
    private String coverUrl;

    /**
     * 视频时长(秒)
     */
    private Integer duration;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
