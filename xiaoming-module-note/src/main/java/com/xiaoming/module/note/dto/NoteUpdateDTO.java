package com.xiaoming.module.note.dto;

import lombok.Data;

@Data
public class NoteUpdateDTO {

    private String title;

    private String content;

    private String coverImage;

    private String tags;
}
