//package com.example.websocketchat.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.messaging.simp.config.MessageBrokerRegistry;
//import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
//import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
//import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
//
//@Configuration
//@EnableWebSocketMessageBroker	//宣告啟用STOMP協定
//public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer{
// 
//    /**
//	 * 配置訊息代理(Message broker)，指定了客戶端訂閱地址，以及發送消息的路由地址
//	 * @author charles
//	 * @date 2019年7月8日 上午9:55:50
//	 */
//	@Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic");	//啟動簡單SimpleBroker，使得訂閱到此"topic"前缀的客戶端可以收到greeting消息，消息的發送的地址符合配置的前綴來的消息才發送到這個broker
//        config.setApplicationDestinationPrefixes("/app");
//    }
//
//	/**
//	 * 註冊一個Endpoint，指定了客戶端建立連接時的請求地址，
//	 * @author charles
//	 * @date 2019年7月8日 上午10:12:50
//	 */
//	@Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//        registry.addEndpoint("/gs-guide-websocket")	//此HTTP URL是供WebSocket或SockJS客户端訪問的地址(即為客戶端嘗試建立連接的地址)
//        .setAllowedOrigins("*") // 添加允許跨域訪問
//        .withSockJS();	//指定端点使用SockJS協議
//    }
//}
