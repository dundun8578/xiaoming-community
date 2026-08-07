package com.xiaoming.module.note.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 笔记图片VO
 */
@Data
@ApiModel("笔记图片")
public class NoteImageVO {

    @ApiModelProperty("图片ID")
    private Long id;

    @ApiModelProperty("图片URL")
    private String imageUrl;

    @ApiModelProperty("排序")
    private Integer sort;
}
