package com.xiaoming.module.user.vo;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String description;

    private Long followerCount;

    private Long followingCount;

    private Boolean isFollowed;
}
