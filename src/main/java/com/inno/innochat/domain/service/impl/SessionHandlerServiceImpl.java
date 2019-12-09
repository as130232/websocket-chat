package com.inno.innochat.domain.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inno.innochat.common.constant.ErrorTypeEnum;
import com.inno.innochat.common.constant.KeyConstants;
import com.inno.innochat.common.constant.MessageTypeConstants;
import com.inno.innochat.common.constant.RedisKeyEnum;
import com.inno.innochat.common.io.MessageIO;
import com.inno.innochat.common.io.UserIO;
import com.inno.innochat.common.io.VoteIO;
import com.inno.innochat.common.po.MessagePO;
import com.inno.innochat.common.vo.VoteResultVO;
import com.inno.innochat.common.vo.VoteVO;
import com.inno.innochat.domain.service.InnoRestService;
import com.inno.innochat.domain.service.SessionHandlerService;
import com.inno.innochat.domain.service.VoteService;
import com.inno.innochat.domain.service.WordProcessService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("sessionHandler")
public class SessionHandlerServiceImpl implements WebSocketHandler, SessionHandlerService {

	// 用來保存房間、用戶、session(用於回傳訊息)三者。使用雙層Map實現對應關係。
	private static final Map<Integer, List<UserIO>> channelAndUsersMap = new HashMap<>(10);
	private ObjectMapper objectMapper;
	private WordProcessService wordProcessSv;
	private VoteService voteService;
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	public SessionHandlerServiceImpl(ObjectMapper objectMapper, WordProcessService wordProcessSv,
			VoteService voteService, RedisTemplate<String, Object> redisTemplate) {
		this.objectMapper = objectMapper;
		this.wordProcessSv = wordProcessSv;
		this.voteService = voteService;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		Map<String, Object> map = session.getAttributes();
		String account = (String) map.get(KeyConstants.ACCOUNT);
		Integer iid = Integer.valueOf((String) map.get(KeyConstants.IID));
		String sid = map.containsKey(KeyConstants.SID) ? (String)map.get(KeyConstants.SID) : null;
		log.info("成功建立連接。sid:{}, iid:{}, account:{}", sid, iid, account);
		List<UserIO> users = channelAndUsersMap.get(iid);
		if (users == null) {
			users = new ArrayList<>();
			channelAndUsersMap.put(iid, users);
		}
		UserIO user = UserIO.builder().account(account).timeInterval(null).session(session).build();
		users.add(user);
		// 更新聊天室在線人數
		this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.COUNT_OF_USERS, KeyConstants.ACCOUNT_SYSTEM,
				MessagePO.builder().message(String.valueOf(users.size())).build());
		// 發送當前賽事投票結果
		if(StringUtils.isNotBlank(sid)){
			MessageIO message = MessageIO.builder()
					.type(MessageTypeConstants.VOTE_RESULT)
					.data(voteService.getVoteResult(Integer.valueOf(sid), Long.valueOf(iid.toString())).toMap())
					.source(KeyConstants.ACCOUNT_SYSTEM)
					.build();
			sessionSendMessage(session, objectMapper.writeValueAsString(message));
		}
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
		String payload = webSocketMessage.getPayload().toString();
		MessageIO messageIO = objectMapper.readValue(payload, MessageIO.class);
		Integer iid = this.getIidBySession(session);
		String account = this.getAccountBySession(session);
		Integer sid = this.getSidBySession(session);
		//log.info("處理送入訊息 ，iid:{}, account:{}, url:{}", iid, account, session.getUri());
		switch (messageIO.getType()) {
			case MessageTypeConstants.ENTER:{
				String data = wordProcessSv.blockAccount(account) + "加入聊天室";
				this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.NOTIFICATION, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
				break;
			}
			case MessageTypeConstants.LEAVE:{
				String data = wordProcessSv.blockAccount(account) + "离开聊天室";
				this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.NOTIFICATION, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
				break;
			}
			case MessageTypeConstants.MESSAGE: {
				UserIO user = this.getUser(iid, account);
				// 檢查帳號是否為匿名帳號
				if (KeyConstants.ACCOUNT_GUEST.equals(account)) {
					this.sendMessageToTargetOnChannel(iid, MessageTypeConstants.WARN, KeyConstants.ACCOUNT_SYSTEM, account,
							ErrorTypeEnum.PERMISSION_DENIED.getCn());
					break;
				}
				// 檢查該帳號是否為進行過儲值，若未進行過儲值，則無法進行發話
	
				// 檢查該帳號的發言時間是否已達時間間隔限制
				Boolean isTimeInterval = this.checkIsTimeInterval(iid, account, user.getTimeInterval());
				if (isTimeInterval) {
					break;
				}
				MessagePO msgPO = getMessagePO(messageIO);
				String blockContent = wordProcessSv.blockForbiddenWord(msgPO.getMessage()); // 過濾不雅字
				this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.MESSAGE, account, MessagePO.builder().message(blockContent).build());
				user.setTimeInterval(new Date().getTime() + getTimeInterval()); // 紀錄當前發言時間 + 限制時間，用於下次判斷
				break;
			}
			case MessageTypeConstants.WHISPER: {
				MessagePO msgPO = getMessagePO(messageIO);
				UserIO user = this.getUser(iid, account);
				String target = messageIO.getTarget();
				UserIO targetUser = this.getUser(iid, target);
				// 密頻目標不可為空或該頻道無此用戶
				if (StringUtils.isBlank(target) || targetUser == null) {
					this.sendMessageToTargetOnChannel(iid, MessageTypeConstants.WARN, KeyConstants.ACCOUNT_SYSTEM, account,
							ErrorTypeEnum.USER_NOT_EXIST.getCn());
					break;
				}
				String blockContent = wordProcessSv.blockForbiddenWord(msgPO.getMessage()); // 過濾不雅字
				this.sendMessageToTargetOnChannel(iid, MessageTypeConstants.WHISPER, account, target, blockContent);
				user.setTimeInterval(new Date().getTime() + getTimeInterval()); // 紀錄當前發言時間 + 限制時間，用於下次判斷
				break;
			}
			case MessageTypeConstants.VOTE:{
				// 投票
				VoteIO voteIO = objectMapper.readValue(objectMapper.writeValueAsString(messageIO.getData()), VoteIO.class);
				voteIO.setSid(sid);
				voteIO.setIid(Long.valueOf(iid + ""));
				VoteVO vo = voteService.vote(account, voteIO);
				MessageIO message = MessageIO.builder()
						.type(MessageTypeConstants.VOTE)
						.data(vo)
						.source(KeyConstants.ACCOUNT_SYSTEM)
						.build();
				// 推送執行投票結果給投票者
				sessionSendMessage(session, objectMapper.writeValueAsString(message));
				if(vo.getSuccess()){
					// 發送當前投票結果公告給所有人
					VoteResultVO voteResult = voteService.getVoteResult(voteIO.getSid(), voteIO.getIid());
					sendMessageToUsersOnChannel(iid, MessageTypeConstants.VOTE_RESULT, KeyConstants.ACCOUNT_SYSTEM, voteResult);
				}
				break;
			}
		}
	}

	private MessagePO getMessagePO(MessageIO messageIO) throws IOException {
		return objectMapper.readValue(objectMapper.writeValueAsString(messageIO.getData()), MessagePO.class);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.warn("連接出錯, 關閉連線： {}, {}", session.getId(), exception);
		if (session.isOpen()) {
			session.close();
		}
		removeSession(session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		log.info("連接已關閉: {}, {}", session.getUri(), closeStatus);
		removeSession(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
	
	@Override
	public Integer getTimeInterval() {
		Integer result = 5000;
		String chatSettingRedisKey = RedisKeyEnum.getChatSettingRedisKey();
		if (redisTemplate.opsForHash().hasKey(chatSettingRedisKey, RedisKeyEnum.TIME_INTERVAL)) {
        	result = (Integer) redisTemplate.opsForHash().get(chatSettingRedisKey, RedisKeyEnum.TIME_INTERVAL);
        }
		return result;
	}

	@Override
	public void updateAnnouncement(Integer iid, String data) {
		if (iid == null) {
			// 若沒給iid，則全部頻道都設定公告
			this.sendMessageToAllUsers(MessageTypeConstants.ANNOUNCEMENT, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
		} else {
			this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.ANNOUNCEMENT, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
		}
	}

	@Override
	public void broadcast(Integer iid, String data) {
		if (iid == null) {
			// 若沒給iid，則全部頻道廣播
			this.sendMessageToAllUsers(MessageTypeConstants.BROADCAST, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
		} else {
			this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.BROADCAST, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(data).build());
		}
	}

	/**
	 * 取得session中的賽事id資訊
	 * 
	 * @param session websocket連線
	 * @return iid 賽事 id
	 * @author charles
	 * @date 2019年10月29日 下午3:00:15
	 */
	public Integer getIidBySession(WebSocketSession session) {
		Map<String, Object> map = session.getAttributes();
		String iidStr = (String) map.get("iid");
		return Integer.valueOf(iidStr);
	}

	/**
	 * 取得session中的帳號資訊
	 * 
	 * @param session websocket連線
	 * @return account 帳號
	 * @author charles
	 * @date 2019年10月29日 下午3:00:15
	 */
	public String getAccountBySession(WebSocketSession session) {
		Map<String, Object> map = session.getAttributes();
		return (String) map.get(KeyConstants.ACCOUNT);
	}

	/**
	 * 取得 session 中的運動項目 ID 資訊
	 *
	 * @param session websocket連線
	 * @return 運動項目 ID 資訊
	 * @author charles
	 * @date 2019年10月29日 下午3:00:15
	 */
	public Integer getSidBySession(WebSocketSession session) {
		Map<String, Object> map = session.getAttributes();
		if(map.containsKey(KeyConstants.SID)){
			return Integer.valueOf((String) map.get(KeyConstants.SID));
		}else{
			return null;
		}
	}

	/**
	 * 取得該用戶資訊
	 * 
	 * @param iid 比賽ID
	 * @param account 帳號
	 * @author charles
	 * @date 2019年10月31日 上午9:50:17
	 */
	public UserIO getUser(Integer iid, String account) {
		List<UserIO> users = channelAndUsersMap.get(iid);
		if (users != null) {
			for (UserIO user : users) {
				if (user.getAccount().equals(account)) {
					return user;
				}
			}
		}
		return null;
	}

	/**
	 * 移除Map中的session
	 * 
	 * @param session websocket連線
	 * @author charles
	 * @date 2019年10月29日 下午2:48:10
	 */
	private void removeSession(WebSocketSession session) {
		if (session == null)
			return;
		Integer iid = null;
		try {
			iid = getIidBySession(session);
			List<UserIO> users = channelAndUsersMap.get(iid);
			if (users != null) {
				Iterator<UserIO> iterator = users.iterator();
				while (iterator.hasNext()) {
					UserIO user = iterator.next();
					if (session.equals(user.getSession())) {
						iterator.remove();
					}
				}
			}

			// 更新聊天室在線人數
			this.sendMessageToUsersOnChannel(iid, MessageTypeConstants.COUNT_OF_USERS, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(String.valueOf(users.size())).build());
		}catch(Exception e) {
			log.error("removeSession fail! iid:{}, session:{}, errorMsg:{}", iid, session, e.getMessage());
		}
	}

	/**
	 * 檢查是否達發言的間隔時間
	 * 
	 * @author charles
	 * @date 2019年10月30日 上午11:52:20
	 */
	private Boolean checkIsTimeInterval(Integer iid, String account, Long limitChatTime) {
		Boolean isLimitChatTime = false;
		Long nowMilliseconde = new Date().getTime();
		if (limitChatTime != null && limitChatTime > nowMilliseconde) {
			this.sendMessageToTargetOnChannel(iid, MessageTypeConstants.WARN, KeyConstants.ACCOUNT_SYSTEM, account, "發言频率过高，请稍等。");
			isLimitChatTime = true;
		}
		return isLimitChatTime;
	}

	/**
	 * 傳送訊息給該session
	 * @param session websocket連線
	 * @param messageJson 訊息
	 * @author charles
	 * @date 2019年10月31日 下午7:09:50
	 */
	private void sessionSendMessage(WebSocketSession session, String messageJson) {
		try {
			if (session.isOpen()) {
				session.sendMessage(new TextMessage(messageJson));
			}
		} catch (IOException e) {
			log.error("session send message fail! data:{}, errorMsg:{}", messageJson, e);
		}
	}
	
	/**
	 * 發送訊息給所有頻道的"所有人"
	 * 
	 * @param type   訊息類型
	 * @param source 發送訊息的來源
	 * @param data   發送的訊息內容
	 * @author charles
	 * @date 2019年10月30日 下午5:52:41
	 */
	public void sendMessageToAllUsers(@NonNull String type, String source, Object data) {
		MessageIO message = MessageIO.builder().type(type).data(data).source(source).target(null).build();
		String messageJson;
		try {
			messageJson = objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e1) {
			log.error("json processing fail! data:{}, errorMsg:{}", data, e1);
			return;
		}

		Collection<List<UserIO>> collection = channelAndUsersMap.values();
		for (List<UserIO> users : collection) {
			for (UserIO user : users) {
				this.sessionSendMessage(user.getSession(), messageJson);
			}
		}
	}

	/**
	 * 發送訊息給該頻道的所有人
	 * 
	 * @param iid 	  比賽ID
	 * @param type   訊息類型
	 * @param source 發送訊息的來源
	 * @param data   發送的訊息內容
	 * @author charles
	 * @date 2019年10月29日 下午3:30:59
	 */
	public void sendMessageToUsersOnChannel(@NonNull Integer iid, @NonNull String type, String source, Object data) {
		if (channelAndUsersMap.get(iid) == null)
			return;
		Collection<WebSocketSession> sessions = channelAndUsersMap.get(iid).stream().map(UserIO::getSession).collect(Collectors.toList());
		MessageIO message = MessageIO.builder().type(type).data(data).source(source).target(null).build();
		String messageJson = null;
		try {
			messageJson = objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e1) {
			log.error("json processing fail! data:{}, errorMsg:{}", data, e1);
			return;
		}
		for (WebSocketSession session : sessions) {
			this.sessionSendMessage(session, messageJson);
		}
	}

	/**
	 * 發送訊息給該頻道的該用戶
	 * 
	 * @param iid    比賽ID
	 * @param source 發言的用戶
	 * @param target 接收的目標用戶
	 * @author charles
	 * @date 2019年10月29日 下午3:30:59
	 */
	public void sendMessageToTargetOnChannel(@NonNull Integer iid, @NonNull String type, String source, @NonNull String target, String data) {
		if (channelAndUsersMap.get(iid) == null)
			return;
		Collection<WebSocketSession> sessions = channelAndUsersMap.get(iid).stream()
				.filter(user -> source.equals(user.getAccount()) || target.equals(user.getAccount()))
				.map(UserIO::getSession).collect(Collectors.toList());
		MessageIO message = MessageIO.builder().type(type).data(MessagePO.builder().message(data).build()).source(source).target(target).build();
		String messageJson;
		try {
			messageJson = objectMapper.writeValueAsString(message);
		} catch (JsonProcessingException e1) {
			log.error("json processing fail! data:{}, errorMsg:{}", data, e1);
			return;
		}
		for (WebSocketSession session : sessions) {
			this.sessionSendMessage(session, messageJson);
		}
	}

	@Override
	public void updateChatbotSwitch(Boolean chatbotSwitch) {
		this.sendMessageToAllUsers(MessageTypeConstants.CHATBOT_SWITCH, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(chatbotSwitch.toString()).build());
	}

	/**
	 * 觸發心跳機制(45秒觸發一次)
	 * @author charles
	 * @date 2019年11月9日 下午2:32:08
	 */
	@Scheduled(fixedRate = 45 * 1000)
	private void triggerHeartbeat() {
		this.sendMessageToAllUsers(MessageTypeConstants.HEART_BEAT, KeyConstants.ACCOUNT_SYSTEM, MessagePO.builder().message(MessageTypeConstants.HEART_BEAT).build());
	}
	
}
