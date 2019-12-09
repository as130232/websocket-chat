package com.inno.innochat.common.constant;

import lombok.Getter;

/**
 * 錯誤訊息列舉
 * @author charles
 * @date 2019年10月31日 上午11:22:22
 */
@Getter
public enum ErrorTypeEnum {
	PERMISSION_DENIED("没有权限，请登录。"),
	USER_NOT_EXIST("用户不存在")
	
	;
	private String cn;
	ErrorTypeEnum(String cn) {
		this.cn = cn;
	}
}
