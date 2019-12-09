package com.inno.innochat.common.constant;


import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.cache.CacheKeyPrefix;


/**
 * 支援賽事
 */
public enum RedisKeyEnum {
	//聊天間隔時間(毫秒)
	TIME_INTERVAL("timeInterval"),
	//聊天機器人聊天間隔時間(毫秒)
	CHATBOT_TIME_INTERVAL("chatbotTimeInterval"),
	//聊天機器人開關
	CHATBOT_SWITCH("chatbotSwitch"),
	//禁用字
	FORBIDDEN_WORDS("forbiddenWords"),
    ;
	
	public final String value;

	private RedisKeyEnum(String value) {
        this.value = value;
    }

    public static String getChatSettingRedisKey() {
    	String chatSettingRedisKey = CacheKeyPrefix.simple().compute("inno-admin") + "ChatSetting";
        return chatSettingRedisKey;
    }

}
