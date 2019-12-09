package com.inno.innochat.common.vo;

import org.apache.logging.log4j.util.Strings;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 執行投票的結果
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VoteVO {

    private Boolean success;
    @Builder.Default
    private String result = Strings.EMPTY;


}
