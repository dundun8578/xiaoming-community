package com.xiaoming.module.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoming.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息处理器
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    /**
     * 存储用户session: userId -> session
     */
    private static final Map<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.put(userId, session);
            log.info("用户 {} WebSocket连接建立", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到WebSocket消息: {}", payload);

        // 解析消息
        Map<String, Object> msgMap = objectMapper.readValue(payload, Map.class);
        String type = (String) msgMap.get("type");

        if ("heartbeat".equals(type)) {
            session.sendMessage(new TextMessage("{\"type\":\"heartbeat\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSIONS.remove(userId);
            log.info("用户 {} WebSocket连接关闭: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误", exception);
    }

    /**
     * 从session中提取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            String token = (String) session.getAttributes().get("token");
            if (token != null && jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            log.error("解析用户ID失败", e);
        }
        return null;
    }

    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, Object message) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("发送消息给用户 {}: {}", userId, json);
            } catch (Exception e) {
                log.error("发送WebSocket消息失败", e);
            }
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(Object message) {
        String json;
        try {
            json = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("序列化消息失败", e);
            return;
        }

        USER_SESSIONS.values().stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(json));
                    } catch (Exception e) {
                        log.error("广播消息失败", e);
                    }
                });
    }

    /**
     * 获取在线用户数
     */
    public int getOnlineUserCount() {
        return (int) USER_SESSIONS.values().stream()
                .filter(WebSocketSession::isOpen)
                .count();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }
}
