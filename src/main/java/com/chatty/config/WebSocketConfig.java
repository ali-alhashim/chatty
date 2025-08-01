package com.chatty.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler contactRequestHandler;

    public WebSocketConfig(WebSocketHandler contactRequestHandler) {
        this.contactRequestHandler = contactRequestHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(contactRequestHandler, "/ws")
                .setAllowedOrigins("*"); // or specify your allowed origin
    }
}

