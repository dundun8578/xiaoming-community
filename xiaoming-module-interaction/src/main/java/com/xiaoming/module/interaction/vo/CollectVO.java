package com.xiaoming.module.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收藏列表项VO
 */
@Data
@Schema(description = "收藏列表项")
public class CollectVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "笔记ID")
    private Long noteId;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者名称")
    private String authorName;

    @Schema(description = "收藏时间")
    private String createTime;
}
