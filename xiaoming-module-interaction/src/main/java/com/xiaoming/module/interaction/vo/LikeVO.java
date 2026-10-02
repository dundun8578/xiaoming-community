package com.xiaoming.module.interaction.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 点赞状态VO
 */
@Data
@Schema(description = "点赞状态")
public class LikeVO {

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "点赞数")
    private Integer likeCount;
}
