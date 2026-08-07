package com.xiaoming.module.note.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 笔记视频VO
 */
@Data
@ApiModel("笔记视频")
public class NoteVideoVO {

    @ApiModelProperty("视频ID")
    private Long id;

    @ApiModelProperty("视频URL")
    private String videoUrl;

    @ApiModelProperty("视频封面URL")
    private String coverUrl;

    @ApiModelProperty("视频时长(秒)")
    private Integer duration;
}
