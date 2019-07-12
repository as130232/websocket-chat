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
	        	String username = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter(MessageKey.KEY_USERNAME);
	        	String roomId = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest().getParameter(MessageKey.KEY_ROOM_ID);
	        	if(username != null){
	        		//ServletServerHttpRequest request = (ServletServerHttpRequest) serverHttpRequest;
	        		//記錄初次連接的用戶名稱及房間ID，此處的map即儲存在webSocketSession.getAttributes()中，在afterConnectionEstablished可取得對應資訊
	        		map.put(MessageKey.KEY_USERNAME, username);
                    map.put(MessageKey.KEY_ROOM_ID, roomId);
	        	}
	        }
	        return true;
	    }

	    @Override
	    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
//	        System.out.println("Enter WebSocket's afterHandshake filter!");
	    }
}
