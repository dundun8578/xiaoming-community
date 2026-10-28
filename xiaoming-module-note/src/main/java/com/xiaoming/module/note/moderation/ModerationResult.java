package com.xiaoming.module.note.moderation;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class ModerationResult {
    private String decision;
    private String reason;
}
