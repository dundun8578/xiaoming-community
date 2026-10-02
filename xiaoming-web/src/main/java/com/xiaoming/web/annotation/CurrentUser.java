package com.xiaoming.web.annotation;

import java.lang.annotation.*;

/**
 * 当前登录用户ID注解
 * 用于Controller方法参数，自动注入当前登录用户的userId
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

    /**
     * 是否必须登录
     */
    boolean required() default true;
}
