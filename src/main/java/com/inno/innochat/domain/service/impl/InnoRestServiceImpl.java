package com.inno.innochat.domain.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.inno.innochat.common.constant.ServerHost;
import com.inno.innochat.common.vo.Result;
import com.inno.innochat.domain.service.InnoRestService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InnoRestServiceImpl implements InnoRestService {
	private ServerHost serverHost;
	private RestTemplate restTemplate;
	private ObjectReader forbiddenWordReader;
	private ObjectReader timeIntervalReader;
	
	@Autowired
	public InnoRestServiceImpl(ServerHost serverHost, RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.serverHost = serverHost;
		this.restTemplate = restTemplate;
		this.forbiddenWordReader = objectMapper.readerFor(new TypeReference<Result<Set<String>>>() {});
		this.timeIntervalReader = objectMapper.readerFor(new TypeReference<Result<Long>>() {});
	}

	@Override
	public Set<String> getForbiddenWords() {
		Set<String> forbiddenWords = new HashSet<>(); 
		String url = serverHost.getAdminIP() + "/admin/chat/forbiddenWords";
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String resultStr = responseEntity.getBody();
			Result<Set<String>> result = forbiddenWordReader.readValue(resultStr);
			forbiddenWords = result.getData();
		} catch (Exception e) {
			throw new RuntimeException("getForbiddenWords fail! errorMsg:{}", e);
		}
		return forbiddenWords;
	}

	@Override
	public Long getTimeInterval() {
		Long timeInterval = null;
		String url = serverHost.getAdminIP() + "/admin/chat/timeInterval";
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
			String resultStr = responseEntity.getBody();
			Result<Long> result = timeIntervalReader.readValue(resultStr);
			timeInterval = result.getData();
		} catch (Exception e) {
			throw new RuntimeException("getTimeInterval fail! errorMsg:{}", e);
		}
		return timeInterval;
	}
}
