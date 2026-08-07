package com.xiaoming.common.constant;

/**
 * 响应状态码常量
 */
public interface ResultCode {

    /**
     * 成功
     */
    int SUCCESS = 200;

    /**
     * 失败
     */
    int FAILED = 500;

    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;

    /**
     * 未认证
     */
    int UNAUTHORIZED = 401;

    /**
     * 权限不足
     */
    int FORBIDDEN = 403;

    /**
     * 资源不存在
     */
    int NOT_FOUND = 404;

    /**
     * 用户已存在
     */
    int USER_ALREADY_EXIST = 1001;

    /**
     * 用户名或密码错误
     */
    int USERNAME_OR_PASSWORD_ERROR = 1002;

    /**
     * 用户已被禁用
     */
    int USER_DISABLED = 1003;

    /**
     * Token已过期
     */
    int TOKEN_EXPIRED = 1004;

    /**
     * Token无效
     */
    int TOKEN_INVALID = 1005;

    /**
     * 笔记不存在
     */
    int NOTE_NOT_FOUND = 2001;

    /**
     * 笔记已删除
     */
    int NOTE_DELETED = 2002;

    /**
     * 不能操作他人笔记
     */
    int NOTE_NOT_OWNER = 2003;

    /**
     * 评论不存在
     */
    int COMMENT_NOT_FOUND = 3001;

    /**
     * 不能关注自己
     */
    int CANNOT_FOLLOW_SELF = 4001;
}
