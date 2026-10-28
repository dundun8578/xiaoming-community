package com.xiaoming.module.comment.vo;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class CommentVO {
    private Long id;
    private Long noteId;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private LocalDateTime createTime;
}
