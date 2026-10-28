package com.xiaoming.web.filter;

import com.xiaoming.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 只负责Token验证，不查询用户详情（避免循环依赖）
 * 同时把解析出的 userId 写入 request attribute，供 @RequestAttribute("userId") / @CurrentUser 使用
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String authToken = null;
        String username = null;
        Long userId = null;

        if (header != null && header.startsWith("Bearer ")) {
            authToken = header.substring(7);
            try {
                username = jwtTokenProvider.getUsernameFromToken(authToken);
                userId = jwtTokenProvider.getUserIdFromToken(authToken);
            } catch (Exception ignored) {
                // Token解析失败，继续
            }
        }

        // 关键：把 userId 暴露给 Controller（@RequestAttribute("userId")）
        if (userId != null && jwtTokenProvider.hasTokenType(authToken,"access")) {
            request.setAttribute("userId", userId);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtTokenProvider.validateToken(authToken) && jwtTokenProvider.hasTokenType(authToken,"access")) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
