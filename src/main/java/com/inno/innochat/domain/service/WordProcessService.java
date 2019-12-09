package com.inno.innochat.domain.service;

import java.util.Map;

/**
 * 文字處理服務
 * @author charles
 * @date 2019年10月30日 下午3:51:55
 */
public interface WordProcessService {
	
	/**
	 * 遮蔽帳號
	 * 
	 * @author charles
	 * @date 2019年10月29日 下午3:11:44
	 */
	String blockAccount(String account);

	/**
	 * 遮蔽不雅字
	 * 
	 * @author charles
	 * @date 2019年10月29日 下午3:11:44
	 */
	String blockForbiddenWord(String content);

	/**
	 * 取得redis中的禁用字與替換字Map
	 * @author charles
	 * @date 2019年12月4日 下午2:14:10
	 */
	Map<String, String> getForbiddenWordsMap();

}
