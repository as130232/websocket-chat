package com.example.websocketchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.websocketchat.interceptor.MyHandler;
import com.example.websocketchat.interceptor.WebSocketInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MyHandler(), "/webSocket/{INFO}")
        		.setAllowedOrigins("*")
                .addInterceptors(new WebSocketInterceptor());
    }
}
