package com.inno.innochat.common.vo;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 目前投票結果
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class VoteResultVO {

    @Builder.Default
    private String h = Strings.EMPTY; // 主隊投票比例
    @Builder.Default
    private String a = Strings.EMPTY; // 客隊投票比例
    @Builder.Default
    private String d = Strings.EMPTY; // 和局投票比例


    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("h", this.h);
        map.put("a", this.a);
        map.put("d", this.d);

        return map;
    }
}
