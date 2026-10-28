package com.xiaoming.module.note.dto;

import lombok.Data;

@Data
public class NotePublishDTO {

    @javax.validation.constraints.NotBlank(message="标题不能为空")
    @javax.validation.constraints.Size(max=200)
    private String title;

    @javax.validation.constraints.NotBlank(message="正文不能为空")
    private String content;

    private String coverImage;

    @javax.validation.constraints.NotNull
    @javax.validation.constraints.Min(1)
    @javax.validation.constraints.Max(2)
    private Integer type;

    private String tags;
}
