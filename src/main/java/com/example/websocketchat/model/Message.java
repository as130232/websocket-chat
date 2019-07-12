package com.example.websocketchat.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.websocketchat.constant.MessageKey;

public class Message implements MessageKey {
	private String roomId;
	private String counterpart;
	private String command;
	private String info;
	
	public Message(String json) {
		if (json != null && json.length() > 0) {
			JSONObject jsonObject = new JSONObject(json);
			this.roomId = jsonObject.optString(KEY_ROOM_ID);
			this.command = jsonObject.optString(KEY_COMMAND);
			this.info = jsonObject.optString(KEY_INFO);
			this.counterpart = jsonObject.optString(KEY_COUNTERPART);
		}
	}

	public JSONObject toJson() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put(KEY_INFO, this.info);
			jsonObject.put(KEY_COUNTERPART, this.counterpart);
			jsonObject.put(KEY_COMMAND, this.command);
			jsonObject.put(KEY_ROOM_ID, this.roomId);
			return jsonObject;
		} catch (JSONException e) {
		}
		return null;
	}

	public String getRoomId() {
		return roomId;
	}

	public String getCounterpart() {
		return counterpart;
	}

	public String getCommand() {
		return command;
	}

	public String getInfo() {
		return info;
	}
}
