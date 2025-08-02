package com.chatty.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@Component
public class ContactWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    {
        // Register support for LocalDateTime and other Java 8 time types
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            sessions.add(session);
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        String query = session.getUri().getQuery(); // e.g. userId=abc123
        if (query != null && query.startsWith("userId=")) {
            return query.substring("userId=".length());
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
    }

    public void sendToUser(String userId, Object payload, String type) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(new WebSocketMessage(type, payload));
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(Object payload, String type) {
        try {
            String json = objectMapper.writeValueAsString(
                    new WebSocketMessage(type, payload)
            );

            synchronized (sessions) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(json));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper DTO for message structure
    private record WebSocketMessage(String type, Object data) {}
}
