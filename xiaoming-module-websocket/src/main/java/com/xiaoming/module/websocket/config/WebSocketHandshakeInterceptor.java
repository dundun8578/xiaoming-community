package com.xiaoming.module.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoming.security.jwt.JwtTokenProvider;
import com.xiaoming.module.websocket.handler.WebSocketHandler;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * WebSocket握手拦截器 - 用于认证和注入用户信息
 */
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final WebSocketHandler webSocketHandler;

    public WebSocketHandshakeInterceptor(JwtTokenProvider jwtTokenProvider, WebSocketHandler webSocketHandler) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            String token = servletRequest.getParameter("token");

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                attributes.put("userId", userId);
                attributes.put("token", token);
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
