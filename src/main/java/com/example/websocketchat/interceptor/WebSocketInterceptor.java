package com.example.websocketchat.interceptor;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.websocketchat.constant.MessageKey;

public class WebSocketInterceptor implements HandshakeInterceptor {
	    @Override
	    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
	        if (serverHttpRequest instanceof ServletServerHttpRequest) {
	            String INFO = serverHttpRequest.getURI().getPath().split("INFO=")[1];
	            System.out.println("INFO:" + INFO);
	            if (INFO != null && INFO.length() > 0) {
	                JSONObject jsonObject = new JSONObject(INFO);
	                String command = jsonObject.getString("command");
	                if (command != null && MessageKey.ENTER_COMMAND.equals(command)) {
	                    System.out.println("Current session ID:="+ jsonObject.getString("name"));
	                    ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;
	                    HttpSession session = request.getServletRequest().getSession();
	                    map.put(MessageKey.KEY_WEBSOCKET_USERNAME, jsonObject.getString("name"));
	                    map.put(MessageKey.KEY_ROOM_ID, jsonObject.getString("roomId"));
	                    System.out.println("map:" + map);
	                }
	            }
	        }
	        return true;
	    }

	    @Override
	    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
	        System.out.println("Enter WebSocket's afterHandshake filter!");
	    }
}
