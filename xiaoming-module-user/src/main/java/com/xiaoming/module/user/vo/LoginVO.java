package com.xiaoming.module.user.vo;

import lombok.Data;

/**
 * 登录响应VO
 */
@Data
public class LoginVO {

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";
}
