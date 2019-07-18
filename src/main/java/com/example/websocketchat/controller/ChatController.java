package com.example.websocketchat.controller;

import java.net.URI;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;

//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//import org.java_websocket.WebSocket;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 模擬後台做為客戶端
 * @author charles
 * @date 2019年7月18日 上午9:46:52
 */
@RestController
public class ChatController {
//	private WebSocketClient client;
//	@GetMapping("/connect")
//	public void connect(
//			@RequestParam(value = "username", required = true) String username,
//    		@RequestParam(value = "roomId", required = true) String roomId) throws Exception {
//		if(client != null) {
//			throw new RuntimeException("已經連線！client:" + client);
//		}
//		System.out.println("準備建立連線!");
//		client  = new WebSocketClient(new URI("ws://172.28.10.148:80/webSocket?username=" + username + "&roomId=" + roomId)) {
////			WebSocketClient client = new WebSocketClient(new URI("ws://localhost:8080/webSocket/INFO")) {
//			@Override
//			public void onOpen(ServerHandshake serverHandshake) {
//				System.out.println("連結建立！");
//			}
//			
//			@Override
//			public void onMessage(String s) {
//				System.out.println("收到消息:" + s);
//			}
//			
//			@Override
//			public void onClose(int i, String s, boolean b) {
//				System.out.println("連接已關閉");
//			}
//			
//			@Override
//			public void onError(Exception e) {
//				e.printStackTrace();
//				System.out.println("連接出錯");
//			}
//		};
//		client.connect();
//	}
//	
//	@PostMapping("/sendMsg")
//	public void sendMsg(@RequestBody OddsVO oddsVO) throws Exception {
//		ObjectMapper objectMapper = new ObjectMapper();
//		client.send(objectMapper.writeValueAsString(oddsVO));
//	}
//	
//	@GetMapping("/disconnect")
//	public void disconnect() throws Exception {
//		client.close();
//		client = null;
//	}
}
