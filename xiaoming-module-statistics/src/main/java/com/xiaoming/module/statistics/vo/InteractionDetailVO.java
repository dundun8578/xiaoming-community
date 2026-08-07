package com.xiaoming.module.statistics.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 互动统计详情VO
 */
@Data
public class InteractionDetailVO {

    /**
     * 点赞趋势 (最近7天)
     */
    private List<Map<String, Object>> likeTrend;

    /**
     * 评论趋势 (最近7天)
     */
    private List<Map<String, Object>> commentTrend;

    /**
     * 收藏趋势 (最近7天)
     */
    private List<Map<String, Object>> collectTrend;

    /**
     * 互动率排名 (Top10)
     */
    private List<Map<String, Object>> interactionRateRank;

    /**
     * 平均点赞数
     */
    private Double avgLikeCount;

    /**
     * 平均评论数
     */
    private Double avgCommentCount;

    /**
     * 平均收藏数
     */
    private Double avgCollectCount;
}
