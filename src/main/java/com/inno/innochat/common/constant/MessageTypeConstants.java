package com.inno.innochat.common.constant;

/**
 * 訊息類型常數
 * @author charles
 * @date 2019年10月31日 下午6:05:03
 */
public interface MessageTypeConstants {
	/**
	 * 用戶的訊息類型
	 */
	//進入
	String ENTER = "enter";
	//離開
	String LEAVE = "leave";
	//一般訊息
	String MESSAGE = "message";
	//密頻
	String WHISPER = "whisper";
	// 投票
	String VOTE = "vote";
	// 投票結果
	String VOTE_RESULT = "voteResult";
	/**
	 * 系統的訊息類型
	 */
	//系統通知：加入、離開聊天室通知
	String NOTIFICATION = "notification";
	//警告：沒有權限、發言頻率過高
	String WARN = "warn";
	//公告
	String ANNOUNCEMENT = "announcement";
	//廣播
	String BROADCAST = "broadcast";
	//該頻道的用戶數量
	String COUNT_OF_USERS = "countOfUsers";
	//聊天機器人開關
	String CHATBOT_SWITCH = "chatbotSwitch";
	//心跳機制:防止連線關閉
	String HEART_BEAT = "heartBeat";
}
