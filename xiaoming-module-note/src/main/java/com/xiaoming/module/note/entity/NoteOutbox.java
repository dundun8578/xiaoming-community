package com.xiaoming.module.note.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("note_outbox")
public class NoteOutbox {
    @TableId(type=IdType.AUTO) private Long id;
    private Long noteId;
    private String eventType;
    private Integer sent;
    private Integer attempts;
    private LocalDateTime availableAt;
    private LocalDateTime createTime;
}
