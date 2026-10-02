package com.xiaoming.module.note.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记详情VO
 */
@Data
@Schema(description = "笔记详情")
public class NoteVO {

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "作者名称")
    private String authorName;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "笔记内容")
    private String content;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "笔记类型: 1-图文 2-视频")
    private Integer type;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "标签")
    private String tags;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "图片列表")
    private List<NoteImageVO> images;

    @Schema(description = "视频信息")
    private NoteVideoVO video;

    @Schema(description = "是否已点赞")
    private Boolean isLiked;

    @Schema(description = "是否已收藏")
    private Boolean isCollected;
}
