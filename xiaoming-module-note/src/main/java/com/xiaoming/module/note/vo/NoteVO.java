package com.xiaoming.module.note.vo;

import io.swagger.v3.annotations.ApiModel;
import io.swagger.v3.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情VO
 */
@Data
@ApiModel("笔记详情")
public class NoteVO {

    @ApiModelProperty("笔记ID")
    private Long id;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("作者名称")
    private String authorName;

    @ApiModelProperty("作者头像")
    private String authorAvatar;

    @ApiModelProperty("笔记标题")
    private String title;

    @ApiModelProperty("笔记内容")
    private String content;

    @ApiModelProperty("封面图片URL")
    private String coverImage;

    @ApiModelProperty("笔记类型: 1-图文 2-视频")
    private Integer type;

    @ApiModelProperty("点赞数")
    private Integer likeCount;

    @ApiModelProperty("收藏数")
    private Integer collectCount;

    @ApiModelProperty("评论数")
    private Integer commentCount;

    @ApiModelProperty("浏览数")
    private Integer viewCount;

    @ApiModelProperty("标签")
    private String tags;

    @ApiModelProperty("发布时间")
    private LocalDateTime publishTime;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("图片列表")
    private List<NoteImageVO> images;

    @ApiModelProperty("视频信息")
    private NoteVideoVO video;

    @ApiModelProperty("是否已点赞")
    private Boolean isLiked;

    @ApiModelProperty("是否已收藏")
    private Boolean isCollected;
}
