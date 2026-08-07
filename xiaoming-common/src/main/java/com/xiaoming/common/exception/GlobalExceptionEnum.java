package com.xiaoming.common.exception;

import com.xiaoming.common.constant.ResultCode;
import lombok.Getter;

/**
 * 全局异常枚举
 */
@Getter
public enum GlobalExceptionEnum {

    SUCCESS(ResultCode.SUCCESS, "success"),

    SYSTEM_ERROR(ResultCode.FAILED, "系统繁忙，请稍后重试"),

    PARAM_ERROR(ResultCode.PARAM_ERROR, "参数错误"),

    UNAUTHORIZED(ResultCode.UNAUTHORIZED, "未认证，请先登录"),

    FORBIDDEN(ResultCode.FORBIDDEN, "权限不足"),

    NOT_FOUND(ResultCode.NOT_FOUND, "资源不存在"),

    USER_ALREADY_EXIST(ResultCode.USER_ALREADY_EXIST, "用户已存在"),

    USERNAME_OR_PASSWORD_ERROR(ResultCode.USERNAME_OR_PASSWORD_ERROR, "用户名或密码错误"),

    USER_DISABLED(ResultCode.USER_DISABLED, "用户已被禁用"),

    TOKEN_EXPIRED(ResultCode.TOKEN_EXPIRED, "Token已过期"),

    TOKEN_INVALID(ResultCode.TOKEN_INVALID, "Token无效"),

    NOTE_NOT_FOUND(ResultCode.NOTE_NOT_FOUND, "笔记不存在"),

    NOTE_DELETED(ResultCode.NOTE_DELETED, "笔记已删除"),

    NOTE_NOT_OWNER(ResultCode.NOTE_NOT_OWNER, "不能操作他人笔记"),

    COMMENT_NOT_FOUND(ResultCode.COMMENT_NOT_FOUND, "评论不存在"),

    CANNOT_FOLLOW_SELF(ResultCode.CANNOT_FOLLOW_SELF, "不能关注自己"),

    FILE_SIZE_LIMIT(4002, "文件大小超出限制"),

    FILE_TYPE_ERROR(4003, "文件类型不支持");

    private final Integer code;

    private final String message;

    GlobalExceptionEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
