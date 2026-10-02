package com.xiaoming.module.statistics.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.statistics.service.StatisticsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计控制器
 */
@Tag(name = "数据统计管理")
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取平台概览统计")
    @GetMapping("/overview")
    public Result<Map<String, Object>> getPlatformOverview() {
        return Result.ok(statisticsService.getPlatformOverview());
    }

    @Operation(summary = "获取用户统计趋势")
    @GetMapping("/user/trend")
    public Result<Map<String, Object>> getUserTrend(@RequestParam(defaultValue = "30") Integer days) {
        return Result.ok(statisticsService.getUserTrend(days));
    }

    @Operation(summary = "获取笔记统计详情")
    @GetMapping("/note/detail")
    public Result<Map<String, Object>> getNoteDetail() {
        return Result.ok(statisticsService.getNoteDetail());
    }

    @Operation(summary = "获取互动统计详情")
    @GetMapping("/interaction/detail")
    public Result<Map<String, Object>> getInteractionDetail() {
        return Result.ok(statisticsService.getInteractionDetail());
    }
}
