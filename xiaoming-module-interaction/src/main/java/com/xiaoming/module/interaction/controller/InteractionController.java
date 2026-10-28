package com.xiaoming.module.interaction.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.interaction.service.CollectService;
import com.xiaoming.module.interaction.service.LikeService;
import com.xiaoming.module.interaction.vo.CollectVO;
import com.xiaoming.module.interaction.vo.LikeVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 互动控制器（点赞/收藏）
 */
@RestController
@RequestMapping("/interaction")
@Tag(name = "互动模块")
public class InteractionController {

    @Autowired
    private LikeService likeService;

    @Autowired
    private CollectService collectService;

    @PostMapping("/like/{noteId}")
    public Result<LikeVO> toggleLike(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long noteId) {
        likeService.toggleLike(userId, noteId);
        boolean isLiked = likeService.isLiked(userId, noteId);

        LikeVO vo = new LikeVO();
        vo.setIsLiked(isLiked);
        return Result.ok(vo);
    }

    @GetMapping("/like/status/{noteId}")
    public Result<LikeVO> getLikeStatus(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @PathVariable Long noteId) {
        boolean isLiked = likeService.isLiked(userId, noteId);
        LikeVO vo = new LikeVO();
        vo.setIsLiked(isLiked);
        return Result.ok(vo);
    }

    @PostMapping("/collect/{noteId}")
    public Result<java.util.Map<String,Boolean>> toggleCollect(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long noteId) {
        collectService.toggleCollect(userId, noteId);
        return Result.ok(java.util.Map.of("isCollected",collectService.isCollected(userId,noteId)));
    }

    @GetMapping("/collect/list")
    public Result<List<CollectVO>> getUserCollects(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<CollectVO> collects = collectService.getUserCollects(userId, pageNum, pageSize);
        return Result.ok(collects);
    }
}
