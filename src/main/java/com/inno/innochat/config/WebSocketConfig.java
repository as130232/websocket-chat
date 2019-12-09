package com.inno.innochat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	@Value("${websocket.chat.url:/websocket-chat}")
	String chatUrl;
	
	@Autowired
	@Qualifier("webSocketInterceptor")
	HandshakeInterceptor webSocketInterceptor;
	@Autowired
	@Qualifier("sessionHandler")
	WebSocketHandler sessionHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(sessionHandler, "websocket-chat")
				.addInterceptors(webSocketInterceptor)
				.setAllowedOrigins("*");
	}

	/**
	 * Setting WebSocket max message size to 1MB for each frame
	 *
	 * @return ServletServerContainerFactoryBean
	 */
	@Bean
	public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxTextMessageBufferSize(1024 * 1024 * 1);
		container.setMaxBinaryMessageBufferSize(1024 * 1024 * 1);
		return container;
	}
}
