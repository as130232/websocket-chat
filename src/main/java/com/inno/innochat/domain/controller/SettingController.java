package com.inno.innochat.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inno.innochat.domain.service.SessionHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "聊天室設定")
@RestController
@RequestMapping("/setting")
public class SettingController {
	private SessionHandlerService sessionHandlerService;
	
	@Autowired
	public SettingController(SessionHandlerService sessionHandlerService) {
		this.sessionHandlerService = sessionHandlerService;
	}
	
	@ApiOperation(value = "取得發言的間隔時間(單位:毫秒)")
	@GetMapping("/timeInterval")
	public Integer getTimeInterval() {
		return sessionHandlerService.getTimeInterval();
	}
	
	@ApiOperation(value = "更新聊天機器人開關")
	@PostMapping("/chatbotSwitch")
	public void updateChatbotSwitch(
			@RequestParam(value = "chatbotSwitch") Boolean chatbotSwitch) {
		sessionHandlerService.updateChatbotSwitch(chatbotSwitch);
	}
}
