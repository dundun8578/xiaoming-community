package com.xiaoming.module.note.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("note_moderation")
public class NoteModeration {
    @TableId(type=IdType.AUTO) private Long id;
    private Long noteId;
    private String decision;
    private String reason;
    private LocalDateTime createTime;
}
