package com.xiaoming.module.level.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.level.service.LevelConfigService;
import com.xiaoming.module.level.vo.UserLevelVO;
import com.xiaoming.security.annotation.RequireLogin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 等级控制器
 */
@Tag(name = "等级管理", description = "用户等级、积分、徽章相关接口")
@RestController
@RequestMapping("/level")
public class LevelController {

    @Autowired
    private LevelConfigService levelConfigService;

    /**
     * 获取所有等级配置
     */
    @Operation(summary = "获取所有等级配置")
    @GetMapping("/configs")
    public Result<List<Map<String, Object>>> getAllLevelConfigs() {
        List<Map<String, Object>> result = levelConfigService.list().stream()
                .map(config -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("level", config.getLevel());
                    map.put("name", config.getName());
                    map.put("minExp", config.getMinExp());
                    map.put("maxExp", config.getMaxExp());
                    map.put("icon", config.getIcon());
                    map.put("badge", config.getBadge());
                    return map;
                })
                .toList();
        return Result.ok(result);
    }

    /**
     * 获取当前用户等级信息
     */
    @Operation(summary = "获取当前用户等级信息")
    @RequireLogin
    @GetMapping("/my")
    public Result<UserLevelVO> getMyLevelInfo(@RequestAttribute("userId") Long userId) {
        UserLevelVO vo = levelConfigService.getUserLevelInfo(userId);
        return Result.ok(vo);
    }

    /**
     * 获取指定用户的等级信息
     */
    @Operation(summary = "获取用户等级信息")
    @GetMapping("/user/{userId}")
    public Result<UserLevelVO> getUserLevelInfo(@PathVariable Long userId) {
        UserLevelVO vo = levelConfigService.getUserLevelInfo(userId);
        return Result.ok(vo);
    }
}
