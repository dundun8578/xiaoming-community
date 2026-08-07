package com.xiaoming.web.controller;

import com.xiaoming.common.result.Result;
import com.xiaoming.module.user.dto.LoginDTO;
import com.xiaoming.module.user.dto.RegisterDTO;
import com.xiaoming.module.user.dto.UserUpdateDTO;
import com.xiaoming.module.user.service.UserService;
import com.xiaoming.module.user.vo.LoginVO;
import com.xiaoming.module.user.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.ok(userService.register(registerDTO));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.ok(userService.login(loginDTO));
    }

    @PostMapping("/refresh-token")
    public Result<String> refreshToken(@RequestParam String refreshToken) {
        return Result.ok(userService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute("userId") Long userId) {
        userService.logout(userId);
        return Result.ok();
    }

    @GetMapping("/info/{userId}")
    public Result<UserVO> getUserInfo(@PathVariable Long userId) {
        return Result.ok(userService.getUserInfo(userId));
    }

    @GetMapping("/info/username/{username}")
    public Result<UserVO> getUserInfoByUsername(@PathVariable String username) {
        return Result.ok(userService.getUserInfoByUsername(username));
    }

    @PutMapping("/info")
    public Result<UserVO> updateUserInfo(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        return Result.ok(userService.updateUserInfo(userId, updateDTO));
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam MultipartFile file) {
        return Result.ok(userService.uploadAvatar(userId, file));
    }

    @GetMapping("/search")
    public Result<List<UserVO>> searchUsers(@RequestParam String keyword) {
        return Result.ok(userService.searchUsers(keyword));
    }

    @GetMapping("/list")
    public Result<List<UserVO>> getUserInfoList(@RequestParam List<Long> userIds) {
        return Result.ok(userService.getUserInfoList(userIds));
    }
}
