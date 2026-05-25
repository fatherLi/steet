package com.street.street.patrol.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/patrol/{userId}")
public class PatrolWebSocketEndpoint {

    // 存放所有在线的客户端会话
    private static final ConcurrentHashMap<Long, Session> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        sessionMap.put(userId, session);
        log.info("WebSocket connected. User: {}", userId);
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") Long userId) {
        sessionMap.remove(userId);
        log.info("WebSocket disconnected. User: {}", userId);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error", error);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("Received message from User {}: {}", userId, message);
        // 通常作为服务器我们只推送，或者接收心跳
    }

    /**
     * 推送最新坐标点给指定的前端用户
     * @param userId 河长ID
     * @param locationJson 坐标点JSON数据
     */
    public void pushLocationToUser(Long userId, String locationJson) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(locationJson);
            } catch (IOException e) {
                log.error("Failed to push location to User {}", userId, e);
            }
        }
    }
}
