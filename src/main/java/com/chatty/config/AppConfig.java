package com.chatty.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.base-url-ws}")
    private String baseUrl_ws;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBaseUrl_ws(){
        return baseUrl_ws;
    }
}
