package com.xiaoming.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoming.web.annotation.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前用户参数解析器
 * userId 由 JwtAuthenticationTokenFilter 解析 Token 后写入 request attribute
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = annotation == null || annotation.required();

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Long userId = null;
        if (request != null) {
            Object attribute = request.getAttribute("userId");
            if (attribute instanceof Long) {
                userId = (Long) attribute;
            } else if (attribute != null) {
                userId = Long.valueOf(attribute.toString());
            }
        }

        if (userId == null && required) {
            throw new RuntimeException("用户未登录");
        }

        return userId;
    }
}
