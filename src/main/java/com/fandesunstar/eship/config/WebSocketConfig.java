package com.fandesunstar.eship.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.fandesunstar.eship.websocket.NotificationWebSocketHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket配置类
 */
@Slf4j
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler notificationWebSocketHandler;

    public WebSocketConfig(NotificationWebSocketHandler notificationWebSocketHandler) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        log.info("WebSocketConfig 初始化完成");
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("注册WebSocket处理器: /ws/notifications");
        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .setAllowedOrigins("*"); // 允许所有来源，生产环境应该限制
        log.info("WebSocket处理器注册完成");
    }
}
