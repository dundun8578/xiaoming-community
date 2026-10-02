package com.xiaoming.module.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 笔记视频VO
 */
@Data
@Schema(description = "笔记视频")
public class NoteVideoVO {

    @Schema(description = "视频ID")
    private Long id;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "视频封面URL")
    private String coverUrl;

    @Schema(description = "视频时长(秒)")
    private Integer duration;
}
