package com.xiaoming.module.statistics.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 笔记统计详情VO
 */
@Data
public class NoteDetailVO {

    /**
     * 分类统计
     */
    private List<Map<String, Object>> categoryStats;

    /**
     * 标签统计 (Top10)
     */
    private List<Map<String, Object>> tagStats;

    /**
     * 热门笔记 (Top10)
     */
    private List<Map<String, Object>> hotNotes;

    /**
     * 活跃作者 (Top10)
     */
    private List<Map<String, Object>> activeAuthors;

    /**
     * 发布趋势 (最近7天)
     */
    private List<Map<String, Object>> publishTrend;
}
