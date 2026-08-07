package com.xiaoming.module.social.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.social.entity.Follow;
import com.xiaoming.module.social.service.FollowService;
import io.swagger.v3.annotations.Api;
import io.swagger.v3.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关注控制器
 */
@RestController
@RequestMapping("/follow")
{tags = "社交模块-关注")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{followUserId}")
    public Result<Void> toggleFollow(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long followUserId) {
        followService.toggleFollow(userId, followUserId);
        return Result.ok();
    }

    @GetMapping("/following")
    public Result<List<Follow>> getFollowingList(@RequestAttribute("userId") Long userId) {
        List<Follow> follows = followService.getFollowingList(userId);
        return Result.ok(follows);
    }

    @GetMapping("/follower")
    public Result<List<Follow>> getFollowerList(@RequestAttribute("userId") Long userId) {
        List<Follow> followers = followService.getFollowerList(userId);
        return Result.ok(followers);
    }

    @GetMapping("/status/{followUserId}")
    public Result<Boolean> getFollowStatus(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long followUserId) {
        boolean isFollowed = followService.isFollowed(userId, followUserId);
        return Result.ok(isFollowed);
    }
}
