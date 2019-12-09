package com.inno.innochat.common.io;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoteIO {

    // h(主), a(客), d(和)(足球、棒球才有和)
    public static final String H = "h";
    public static final String A = "a";
    public static final String D = "d";

    private Integer sid; // 運動項目 ID
    private Long iid; // 賽事 ID
    private String result; // 投票結果


    public boolean isResultCorrect(){
        if(StringUtils.isBlank(result)
                || (H.equals(this.result) == false && A.equals(this.result) == false && D.equals(this.result) == false)){
            return false;
        }
        return true;
    }

}
