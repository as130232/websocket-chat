package com.example.websocketchat.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.websocketchat.constant.MessageKey;
import com.example.websocketchat.model.Message;

public class MyHandler implements WebSocketHandler {

	// 用來保存用戶、房間、session三者。使用雙層Map實現對應關係。
	private static final Map<String, Map<String, WebSocketSession>> roomAndUsersMap = new HashMap<>(3);

	/**
	 * 成功建立連接(加入房間)後，會調用此方法
	 * @author charles
	 * @date 2019年7月10日 上午10:24:45
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
		System.out.println("成功建立連接");
		Map<String, Object> map = webSocketSession.getAttributes();
		String username = (String) map.get(MessageKey.KEY_USERNAME);
    	String roomId = (String) map.get(MessageKey.KEY_ROOM_ID);
    	Map<String, WebSocketSession> mapSession = roomAndUsersMap.get(roomId);
    	if(mapSession == null) {
			mapSession = new HashMap<>(3);
			roomAndUsersMap.put(roomId, mapSession);
		}else {
			//檢查該房間是否已存在該用戶
//			WebSocketSession roomAndUsersSession = mapSession.get(name);
//			if(roomAndUsersSession != null) {
//				webSocketSession.sendMessage(new TextMessage("當前房間已存在該用戶:" + name));
//				this.afterConnectionClosed(webSocketSession, null);
//				return;
//			}
		}
    	mapSession.put(username, webSocketSession);
    	webSocketSession.sendMessage(new TextMessage("當前房間線上人數" + mapSession.size() + "人"));
		System.out.println("當前房間線上人數：" + roomAndUsersMap.size());
	}

	/**
	 * 處理訊息，當前端呼叫ws.send()時會調用此方法
	 * @author charles
	 * @date 2019年7月11日 下午3:07:36
	 */
	@Override
	public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
		try {
			String payload = webSocketMessage.getPayload().toString();
			JSONObject jsonobject = new JSONObject(payload);
			Message message = new Message(jsonobject.toString());
			String username = getUsernameFromSession(webSocketSession);
			System.out.println("來自" + username + "的消息:" + message.toJson());
			if (message.getCounterpart() != null && message.getCommand() != null) {
				switch (message.getCommand()) {
				case MessageKey.ENTER_COMMAND:
					sendMessageToRoomUsers(message.getRoomId(),
							new TextMessage("【" + username + "】加入了房間，歡迎！"));
					break;
				case MessageKey.MESSAGE_COMMAND:
					//廣播所有房間所有用戶
					if (message.getCounterpart().equals("-1")) {
						this.sendMessageToAllUsers(
								new TextMessage(username + "廣播：" + message.getInfo()));
					} else if (message.getCounterpart().equals("all")) {
						sendMessageToRoomUsers(message.getRoomId(),
								new TextMessage(username + "說：" + message.getInfo()));
					} else {
						sendMessageToUser(message.getRoomId(), username, message.getCounterpart(),
								new TextMessage(username + "悄悄對" + message.getCounterpart() + "說：" + message.getInfo()));
					}
					break;
				case MessageKey.USERS_OF_ROOM_COMMAND:
					Map<String, WebSocketSession> usersMap = roomAndUsersMap.get(message.getRoomId());
					Set<String> users = usersMap.keySet();
					sendMessageToRoomUsers(message.getRoomId(), new TextMessage("頻道:" + message.getRoomId() + "，目前用戶共有:" + users));
					break;
				case MessageKey.LEAVE_COMMAND:
					sendMessageToRoomUsers(message.getRoomId(),
							new TextMessage("【" + getUsernameFromSession(webSocketSession) + "】離開了房間。"));
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 發送訊息給指定用戶
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:24:45
	 */
	public boolean sendMessageToUser(String roomId, String username, String counterpart, TextMessage message) {
		if (roomId == null || counterpart == null)
			return false; // 沒有給房間號
		if (roomAndUsersMap.get(roomId) == null)
			return false; // 該房間號不存在
		//除了密頻的對象要發送外，本身發處訊息的用戶也要發送
		Set<String> names = new HashSet<>();
		names.add(username);
		names.add(counterpart);
		for(String name:names) {
			WebSocketSession session = roomAndUsersMap.get(roomId).get(name);
			if (!session.isOpen())
				return false;
			try {
				session.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * 廣播訊息給該房間內的所有用戶
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:24:45
	 */
	public boolean sendMessageToRoomUsers(String roomId, TextMessage message) {
		if (roomId == null)
			return false;
		if (roomAndUsersMap.get(roomId) == null)
			return false;
		boolean allSendSuccess = true;
		System.out.println("sUserMap:" + roomAndUsersMap);
		Collection<WebSocketSession> sessions = roomAndUsersMap.get(roomId).values();
		for (WebSocketSession session : sessions) {
			try {
				if (session.isOpen()) {
					session.sendMessage(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
				allSendSuccess = false;
			}
		}
		return allSendSuccess;
	}

	/**
	 * 廣播訊息給所有房間的所有用戶
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:24:45
	 */
	public boolean sendMessageToAllUsers(TextMessage message) {
		boolean allSendSuccess = true;
		Collection<String> roomIds = roomAndUsersMap.keySet();
		for (String roomId : roomIds) {
			Collection<WebSocketSession> sessions = roomAndUsersMap.get(roomId).values();
			for (WebSocketSession session : sessions) {
				try {
					if (session.isOpen()) {
						session.sendMessage(message);
					}
				} catch (IOException e) {
					e.printStackTrace();
					allSendSuccess = false;
				}
			}
		}
		return allSendSuccess;
	}

	/**
	 * 離開房間時的處理
	 * 
	 * @author charles
	 * @date 2019年7月10日 上午10:50:04
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
		System.out.println("連接已關閉：" + closeStatus);
		Map<String, WebSocketSession> map = roomAndUsersMap.get(getRoomIdFromSession(webSocketSession));
		if (map != null) {
			map.remove(getUsernameFromSession(webSocketSession));
		}
	}

	@Override
	public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
		System.out.println("連接出錯");
		if (webSocketSession.isOpen()) {
			webSocketSession.close();
		}
		Map<String, WebSocketSession> map = roomAndUsersMap.get(getRoomIdFromSession(webSocketSession));
		if (map != null) {
			map.remove(getUsernameFromSession(webSocketSession));
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 取得用戶名稱
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:50:04
	 */
	private String getUsernameFromSession(WebSocketSession session) {
		try {
			String username = (String) session.getAttributes().get(MessageKey.KEY_USERNAME);
			return username;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 取得房間號
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:50:04
	 */
	private String getRoomIdFromSession(WebSocketSession session) {
		try {
			String roomId = (String) session.getAttributes().get(MessageKey.KEY_ROOM_ID);
			return roomId;
		} catch (Exception e) {
			return null;
		}
	}
}
