package com.xiaoming.module.interaction.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 收藏列表项VO
 */
@Data
@ApiModel("收藏列表项")
public class CollectVO {

    @ApiModelProperty("收藏ID")
    private Long id;

    @ApiModelProperty("笔记ID")
    private Long noteId;

    @ApiModelProperty("笔记标题")
    private String title;

    @ApiModelProperty("封面图")
    private String coverImage;

    @ApiModelProperty("作者ID")
    private Long authorId;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("收藏时间")
    private String createTime;
}
