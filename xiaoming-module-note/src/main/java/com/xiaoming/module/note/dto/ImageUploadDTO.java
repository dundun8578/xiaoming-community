package com.xiaoming.module.note.dto;

import lombok.Data;

import java.util.List;

@Data
public class ImageUploadDTO {

    private Long noteId;

    private List<String> imageUrls;

    private Boolean isCover = false;
}
