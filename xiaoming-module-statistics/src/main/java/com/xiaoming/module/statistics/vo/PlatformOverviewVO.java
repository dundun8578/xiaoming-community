package com.xiaoming.module.statistics.vo;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 平台统计概览VO
 */
@Data
public class PlatformOverviewVO {

    /**
     * 用户统计
     */
    private UserStatVO userStat;

    /**
     * 笔记统计
     */
    private NoteStatVO noteStat;

    /**
     * 互动统计
     */
    private InteractionStatVO interactionStat;

    /**
     * 趋势数据 (最近7天)
     */
    private List<Map<String, Object>> trendData;

    @Data
    public static class UserStatVO {
        private Long totalUsers;      // 总用户数
        private Long todayUsers;      // 今日新增
        private Long weekUsers;       // 本周新增
        private Long monthUsers;      // 本月新增
        private Long activeUsers;     // 活跃用户数
    }

    @Data
    public static class NoteStatVO {
        private Long totalNotes;      // 总笔记数
        private Long todayNotes;      // 今日新增
        private Long weekNotes;       // 本周新增
        private Long monthNotes;      // 本月新增
        private Long publishedNotes;  // 已发布
        private Long draftNotes;      // 草稿
    }

    @Data
    public static class InteractionStatVO {
        private Long totalLikes;      // 总点赞数
        private Long totalCollects;   // 总收藏数
        private Long totalComments;   // 总评论数
        private Long totalFollows;    // 总关注数
        private Long todayLikes;      // 今日点赞
        private Long todayComments;   // 今日评论
    }
}
