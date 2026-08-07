package com.xiaoming.module.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记实体类
 */
@Data
@TableName("note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 笔记类型: 1-图文 2-视频
     */
    private Integer type;

    /**
     * 状态: 0-草稿 1-已发布 2-已下架
     */
    private Integer status;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 热度分数
     */
    private Long hotScore;

    /**
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 作者信息
     */
    @TableField(exist = false)
    private String authorName;

    /**
     * 作者头像
     */
    @TableField(exist = false)
    private String authorAvatar;

    /**
     * 图片列表
     */
    @TableField(exist = false)
    private List<NoteImage> images;

    /**
     * 视频信息
     */
    @TableField(exist = false)
    private NoteVideo video;

    /**
     * 是否已点赞
     */
    @TableField(exist = false)
    private Boolean isLiked;

    /**
     * 是否已收藏
     */
    @TableField(exist = false)
    private Boolean isCollected;
}
