package com.example.websocketchat.interceptor;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.websocketchat.constant.MessageKey;
import com.example.websocketchat.model.Message;

public class MyHandler implements WebSocketHandler {

    private static final Map<String, Map<String, WebSocketSession>> sUserMap = new HashMap<>(3);

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
            System.out.println(message.toJson() + ":來自" + webSocketSession.getAttributes().get(MessageKey.KEY_WEBSOCKET_USERNAME) + "的消息");
            if (message.getName() != null && message.getCommand() != null) {
                switch (message.getCommand()) { 
                    case MessageKey.ENTER_COMMAND:
                        sendMessageToRoomUsers(message.getRoomId(), new TextMessage("【" + getNameFromSession(webSocketSession) + "】加入了房間，歡迎！"));
                        break;
                    case MessageKey.MESSAGE_COMMAND:
                        if (message.getName().equals("all")) {
                            sendMessageToRoomUsers(message.getRoomId(), new TextMessage(getNameFromSession(webSocketSession) +
                                    "說：" + message.getInfo()
                            ));
                        } else {
                            sendMessageToUser(message.getRoomId(), message.getName(), new TextMessage(getNameFromSession(webSocketSession) +
                                    "悄悄對你说：" + message.getInfo()));
                        }
                        break;
                    case MessageKey.LEAVE_COMMAND:
                        sendMessageToRoomUsers(message.getRoomId(), new TextMessage("【" + getNameFromSession(webSocketSession) + "】離開了房間。"));
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
     * @param name
     * @param message
     * @return
     */
    public boolean sendMessageToUser(String roomId, String name, TextMessage message) {
        if (roomId == null || name == null) return false;
        if (sUserMap.get(roomId) == null) return false;
        WebSocketSession session = sUserMap.get(roomId).get(name);
        if (!session.isOpen()) return false;
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 廣播信息
     *
     * @param message
     * @return
     */
    public boolean sendMessageToRoomUsers(String roomId, TextMessage message) {
        if (roomId == null) return false;
        if (sUserMap.get(roomId) == null) return false;
        boolean allSendSuccess = true;
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
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) {
        System.out.println("連接已關閉：" + closeStatus);
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
     * 获取用户名称
     *
     * @param session
     * @return
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
     * 获取房间号
     *
     * @param session
     * @return
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
