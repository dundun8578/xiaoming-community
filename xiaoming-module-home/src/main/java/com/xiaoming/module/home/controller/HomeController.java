package com.xiaoming.module.home.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.home.service.HomeService;
import com.xiaoming.module.note.vo.NoteVO;
import io.swagger.v3.annotations.Api;
import io.swagger.v3.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 首页推荐控制器
 */
@RestController
@RequestMapping("/home")
{tags = "首页推荐")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/feed")
    public Result<List<NoteVO>> getHomeFeed(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        List<NoteVO> notes = homeService.getHomeFeed(userId, pageNum, pageSize);
        return Result.ok(notes);
    }
}
