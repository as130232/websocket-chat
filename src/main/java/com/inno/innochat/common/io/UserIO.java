package com.inno.innochat.common.io;


import org.springframework.web.socket.WebSocketSession;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 已建立連線的用戶IO
 * @author charles
 * @date 2019年10月31日 下午6:06:12
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIO {
	@ApiModelProperty(value = "用戶帳號")
	private String account;
	@ApiModelProperty(value = "用戶發言的間隔時間(單位:毫秒)")
	private Long timeInterval;
	@ApiModelProperty(value = "該用戶的websocket連線")
	private WebSocketSession session;
}
