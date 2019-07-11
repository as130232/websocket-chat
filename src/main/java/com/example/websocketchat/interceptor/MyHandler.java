package com.example.websocketchat.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
	private static final Map<String, Map<String, WebSocketSession>> sUserMap = new HashMap<>(3);

	/**
	 * 成功建立連接(加入房間)後，會調用此方法，在這個節點上，向其他用戶發送有用戶加入的通知消息
	 * 
	 * @author charles
	 * @date 2019年7月10日 上午10:24:45
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.out.println("成功建立連接");
		String path = session.getUri().getPath();
		System.out.println("path:" + path);
		String INFO = path.split("INFO=")[1];
		if (INFO != null && INFO.length() > 0) {
			JSONObject jsonObject = new JSONObject(INFO);
			String command = jsonObject.getString("command");
			String roomId = jsonObject.getString("roomId");
			if (command != null && MessageKey.ENTER_COMMAND.equals(command)) {
				Map<String, WebSocketSession> mapSession = sUserMap.get(roomId);
				if (mapSession == null) {
					mapSession = new HashMap<>(3);
					sUserMap.put(roomId, mapSession);
				}
				mapSession.put(jsonObject.getString("name"), session);
				session.sendMessage(new TextMessage("當前房間線上人數" + mapSession.size() + "人"));
				System.out.println(session);
			}
		}
		System.out.println("當前房間線上人數：" + sUserMap.size());
	}

	@Override
	public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) {
		try {
			JSONObject jsonobject = new JSONObject(webSocketMessage.getPayload().toString());
			Message message = new Message(jsonobject.toString());
			System.out.println(message.toJson() + ":來自"
					+ webSocketSession.getAttributes().get(MessageKey.KEY_WEBSOCKET_USERNAME) + "的消息");
			if (message.getName() != null && message.getCommand() != null) {
				switch (message.getCommand()) {
				case MessageKey.ENTER_COMMAND:
					sendMessageToRoomUsers(message.getRoomId(),
							new TextMessage("【" + getNameFromSession(webSocketSession) + "】加入了房間，歡迎！"));
					break;
				case MessageKey.MESSAGE_COMMAND:
					if (message.getName().equals("-1")) {
						this.sendMessageToAllUsers(
								new TextMessage(getNameFromSession(webSocketSession) + "說：" + message.getInfo()));
					} else if (message.getName().equals("all")) {
						sendMessageToRoomUsers(message.getRoomId(),
								new TextMessage(getNameFromSession(webSocketSession) + "說：" + message.getInfo()));
					} else {
						sendMessageToUser(message.getRoomId(), message.getName(),
								new TextMessage(getNameFromSession(webSocketSession) + "悄悄對你说：" + message.getInfo()));
					}
					break;
				case MessageKey.LEAVE_COMMAND:
					sendMessageToRoomUsers(message.getRoomId(),
							new TextMessage("【" + getNameFromSession(webSocketSession) + "】離開了房間。"));
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
	public boolean sendMessageToUser(String roomId, String name, TextMessage message) {
		if (roomId == null || name == null)
			return false; // 沒有給房間號
		if (sUserMap.get(roomId) == null)
			return false; // 該房間號不存在
		WebSocketSession session = sUserMap.get(roomId).get(name);
		if (!session.isOpen())
			return false;
		try {
			session.sendMessage(message);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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
		if (sUserMap.get(roomId) == null)
			return false;
		boolean allSendSuccess = true;
		System.out.println("sUserMap:" + sUserMap);
		Collection<WebSocketSession> sessions = sUserMap.get(roomId).values();
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
		Collection<String> roomIds = sUserMap.keySet();
		for (String roomId : roomIds) {
			Collection<WebSocketSession> sessions = sUserMap.get(roomId).values();
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
		Map<String, WebSocketSession> map = sUserMap.get(getRoomIdFromSession(webSocketSession));
		if (map != null) {
			map.remove(getNameFromSession(webSocketSession));
		}
	}

	@Override
	public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
		System.out.println("連接出錯");
		if (webSocketSession.isOpen()) {
			webSocketSession.close();
		}
		Map<String, WebSocketSession> map = sUserMap.get(getRoomIdFromSession(webSocketSession));
		if (map != null) {
			map.remove(getNameFromSession(webSocketSession));
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	/**
	 * 獲取用戶名稱
	 *
	 * @author charles
	 * @date 2019年7月10日 上午10:50:04
	 */
	private String getNameFromSession(WebSocketSession session) {
		try {
			String name = (String) session.getAttributes().get(MessageKey.KEY_WEBSOCKET_USERNAME);
			return name;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 獲取房間號
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
