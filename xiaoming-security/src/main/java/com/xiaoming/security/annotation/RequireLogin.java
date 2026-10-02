package com.xiaoming.security.annotation;

import java.lang.annotation.*;

/**
 * 需要登录注解
 * 标记在 Controller 的方法或类上，表示该接口需要登录后才能访问
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
}
