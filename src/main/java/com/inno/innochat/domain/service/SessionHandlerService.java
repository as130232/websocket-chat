package com.inno.innochat.domain.service;

/**
 * websocket訊息處理服務
 * @author charles
 * @date 2019年10月31日 下午6:10:01
 */
public interface SessionHandlerService {
	
	/**
	 * 取得聊天的間隔時間
	 * @author charles
	 * @date 2019年10月30日 下午5:29:39
	 */
	Integer getTimeInterval();
	
	/**
	 * 設定公告
	 * @author charles
	 * @date 2019年10月30日 下午5:36:01
	 */
	void updateAnnouncement(Integer iid, String data);

	/**
	 * 廣播
	 * @author charles
	 * @date 2019年10月31日 下午5:09:14
	 */
	void broadcast(Integer iid, String data);

	/**
	 * 更新聊天機器人開關
	 * @param chatbotSwitch 開關
	 * @author charles
	 * @date 2019年11月5日 下午8:19:28
	 */
	void updateChatbotSwitch(Boolean chatbotSwitch);
	
}
