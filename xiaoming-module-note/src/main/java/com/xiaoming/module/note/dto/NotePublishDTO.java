package com.xiaoming.module.note.dto;

import lombok.Data;

@Data
public class NotePublishDTO {

    private String title;

    private String content;

    private String coverImage;

    private Integer type;

    private String tags;
}
