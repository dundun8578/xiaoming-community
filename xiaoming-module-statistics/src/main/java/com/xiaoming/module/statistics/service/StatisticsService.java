package com.xiaoming.module.statistics.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取平台概览统计
     */
    Map<String, Object> getPlatformOverview();

    /**
     * 获取用户统计趋势 (最近30天)
     */
    Map<String, Object> getUserTrend(Integer days);

    /**
     * 获取笔记统计详情
     */
    Map<String, Object> getNoteDetail();

    /**
     * 获取互动统计详情
     */
    Map<String, Object> getInteractionDetail();

    /**
     * 生成今日统计数据
     */
    void generateDailyStatistics(LocalDate date);
}
