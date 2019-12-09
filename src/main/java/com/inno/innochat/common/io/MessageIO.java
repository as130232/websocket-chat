package com.inno.innochat.common.io;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 傳遞訊息使用的IO
 * @author charles
 * @date 2019年10月31日 下午6:05:29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageIO<T> {
	@ApiModelProperty(value = "訊息類型(MessageTypeConstants的常數)")
	private String type;
	@ApiModelProperty(value = "發言者")
	private String source;
	@ApiModelProperty(value = "發言對象('':所有人、someone:密頻)")
	private String target;
	@ApiModelProperty(value = "訊息內容")
	private T data;
}
