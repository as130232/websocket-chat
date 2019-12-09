package com.inno.innochat.domain.service;

import java.util.Set;

/**
 * inno rest請求服務
 * @author charles
 * @date 2019年10月31日 下午6:10:01
 */
public interface InnoRestService {

	/**
	 * 取得所有的禁用字
	 * @author charles
	 * @date 2019年10月30日 下午3:47:48
	 */
	Set<String> getForbiddenWords();

	/**
	 * 取得聊天的間隔時間(單位:毫秒)
	 * @author charles
	 * @date 2019年10月31日 下午12:02:15
	 */
	Long getTimeInterval();

}
