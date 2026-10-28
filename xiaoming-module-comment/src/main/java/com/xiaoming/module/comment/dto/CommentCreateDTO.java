package com.xiaoming.module.comment.dto;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
@Data
public class CommentCreateDTO {
    @NotBlank(message = "评论不能为空")
    @Size(max = 1000, message = "评论不能超过1000字")
    private String content;
}
