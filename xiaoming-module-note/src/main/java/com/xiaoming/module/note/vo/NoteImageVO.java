package com.xiaoming.module.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 笔记图片VO
 */
@Data
@Schema(description = "笔记图片")
public class NoteImageVO {

    @Schema(description = "图片ID")
    private Long id;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "排序")
    private Integer sort;
}
