package com.inno.innochat.core;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.inno.innochat.common.constant.KeyConstants;


@Component("webSocketInterceptor")
public class WebSocketInterceptor implements HandshakeInterceptor {
	
	/**
	 * 客戶端連接webSocket時，獲取對應info資訊，進行用戶訊息保存， 此處將用戶名與房間號保存到session上
	 * 
	 * @author charles
	 * @date 2019年10月28日 下午7:43:17
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> map) throws Exception {
		boolean accept = false;
		if (request instanceof ServletServerHttpRequest) {
			// 比賽ID
			String iid = ((ServletServerHttpRequest) request).getServletRequest().getParameter(KeyConstants.CHANNEL);
			String account = ((ServletServerHttpRequest) request).getServletRequest().getParameter(KeyConstants.ACCOUNT);
			String sid = ((ServletServerHttpRequest) request).getServletRequest().getParameter(KeyConstants.SID);
			if (StringUtils.isBlank(account)) {
				account = KeyConstants.ACCOUNT_GUEST;
			}
//				try {
//					LoginInfoPO loginInfoPO = authRedisService.getLoginInfo(token);
//					account = loginInfoPO.getWord();
//				}catch (Exception e) {
//		            throw ApiException.builder().errorType(CommonErrorTypes.INVALID_TOKEN).build();
//		        }
			map.put(KeyConstants.IID, iid);
			map.put(KeyConstants.ACCOUNT, account);
			if(StringUtils.isNotBlank(sid)){
				map.put(KeyConstants.SID, sid);
			}
			accept = true;
		}
		return accept;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
	}
}
