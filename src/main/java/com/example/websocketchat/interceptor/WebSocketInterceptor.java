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

/**
 * 當客戶端像伺服器發起"初次連接"時的握手攔截器，
 * 紀錄用戶名稱及房間ID
 * @author charles
 * @date 2019年7月10日 上午10:30:21
 */
public class WebSocketInterceptor implements HandshakeInterceptor {
	
		/**
		 * 客戶端連接/webSocket時，獲取對應info資訊，
		 * 進行用戶訊息保存，此處將用戶名與房間號保存到session上
		 * @author charles
		 * @date 2019年7月10日 上午10:33:47
		 */
	    @Override
	    public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, 
	    		WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
	        if (serverHttpRequest instanceof ServletServerHttpRequest) {
	        	String path = serverHttpRequest.getURI().getPath();
	            String INFO = path.split("INFO=")[1];
	            System.out.println("path:" + path);
	            if (INFO != null && INFO.length() > 0) {
	                JSONObject jsonObject = new JSONObject(INFO);
	                String command = jsonObject.getString("command");
	                if (command != null && MessageKey.ENTER_COMMAND.equals(command)) {
	                    System.out.println("Current session ID:="+ jsonObject.getString("name"));
	                    ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;
	                    HttpSession session = request.getServletRequest().getSession();
	                    //記錄初次連接的用戶名稱及房間ID，此處的map即儲存在webSocketSession.getAttributes()中
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
