package com.inno.innochat.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inno.innochat.domain.service.SessionHandlerService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "聊天室")
@RestController
public class ChatController {
	private SessionHandlerService sessionHandlerService;
	
	@Autowired
	public ChatController(SessionHandlerService sessionHandlerService) {
		this.sessionHandlerService = sessionHandlerService;
	}
	
	@ApiOperation(value = "設定公告")
	@PostMapping("/announcement")
	public void updateAnnouncement(
			@RequestParam(value = "iid", required = false) Integer iid,
			@RequestParam(value = "data", required = false) String data) {
		sessionHandlerService.updateAnnouncement(iid, data);
	}
	
	@ApiOperation(value = "廣播")
	@PostMapping("/broadcast")
	public void broadcast(
			@RequestParam(value = "iid", required = false) Integer iid,
			@RequestParam(value = "data") String data) {
		sessionHandlerService.broadcast(iid, data);
	}
	
}
