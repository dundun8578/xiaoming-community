package com.xiaoming.module.user.dto;

import lombok.Data;
import javax.validation.constraints.Size;

/**
 * 用户信息修改请求DTO
 */
@Data
public class UserUpdateDTO {

    @Size(max = 32, message = "昵称长度不能超过32位")
    private String nickname;

    @Size(max = 256, message = "简介长度不能超过256位")
    private String description;

    private String email;

    private String phone;
}
