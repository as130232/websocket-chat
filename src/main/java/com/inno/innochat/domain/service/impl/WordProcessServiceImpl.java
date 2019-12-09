package com.inno.innochat.domain.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.inno.innochat.common.constant.RedisKeyEnum;
import com.inno.innochat.domain.service.WordProcessService;


@Component
public class WordProcessServiceImpl implements WordProcessService{

	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public WordProcessServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	private String getBlockStr() {
		return "*****";
	}
	
	@Override
	public String blockAccount(String account) {
		int displayCount = 3;
		String blockAccount = account.substring(0, displayCount) + getBlockStr();
		return blockAccount;
	}

	@Override
	public String blockForbiddenWord(String content) {
		Map<String, String> forbiddenWords = this.getForbiddenWordsMap();
		for(String forbiddenWord:forbiddenWords.keySet()) {
			if(StringUtils.contains(content, forbiddenWord)){
				String replaceWord = forbiddenWords.get(forbiddenWord);
				if(replaceWord == null) {
					replaceWord = getBlockStr();
				}
				content = content.replace(forbiddenWord, replaceWord);
			}
		}
		return content;
	}
	
	@Override
	public Map<String, String> getForbiddenWordsMap() {
		Map<String, String> result = new HashMap<>();
		String chatSettingRedisKey = RedisKeyEnum.getChatSettingRedisKey();
		String forbiddenWordsKey = RedisKeyEnum.FORBIDDEN_WORDS.value;
        if (redisTemplate.opsForHash().hasKey(chatSettingRedisKey, forbiddenWordsKey)) {
        	result = (Map<String, String>) redisTemplate.opsForHash().get(chatSettingRedisKey, forbiddenWordsKey);
        }
        return result;
	}
}
