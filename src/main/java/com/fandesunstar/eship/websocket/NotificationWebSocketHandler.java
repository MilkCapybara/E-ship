package com.fandesunstar.eship.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket通知处理器
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    // 存储用户ID和WebSocket会话的映射
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从URL参数中获取用户ID
        String query = session.getUri().getQuery();
        if (query != null && query.startsWith("userId=")) {
            Long userId = Long.parseLong(query.substring(7));
            sessions.put(userId, session);
            log.info("WebSocket连接建立，用户ID: {}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 处理客户端发来的消息（如果需要）
        log.info("收到消息: {}", message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 移除断开连接的会话
        sessions.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.info("WebSocket连接关闭");
    }

    /**
     * 发送通知给指定用户
     */
    public void sendNotification(Long userId, String type, Object data) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> notification = Map.of(
                        "type", type,
                        "data", data,
                        "timestamp", System.currentTimeMillis()
                );
                String json = objectMapper.writeValueAsString(notification);
                session.sendMessage(new TextMessage(json));
                log.info("发送通知给用户 {}: {}", userId, type);
            } catch (IOException e) {
                log.error("发送WebSocket消息失败", e);
            }
        }
    }

    /**
     * 发送合约通知
     */
    public void sendContractNotification(Long userId, String action, Long contractId) {
        Map<String, Object> data = Map.of(
                "action", action,
                "contractId", contractId
        );
        sendNotification(userId, "CONTRACT_UPDATE", data);
    }
}
