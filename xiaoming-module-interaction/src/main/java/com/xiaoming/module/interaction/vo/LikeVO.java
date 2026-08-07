package com.xiaoming.module.interaction.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 点赞状态VO
 */
@Data
@ApiModel("点赞状态")
public class LikeVO {

    @ApiModelProperty("是否已点赞")
    private Boolean isLiked;

    @ApiModelProperty("点赞数")
    private Integer likeCount;
}
