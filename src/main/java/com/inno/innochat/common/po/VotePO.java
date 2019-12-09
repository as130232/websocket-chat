package com.inno.innochat.common.po;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VotePO {

    @Builder.Default
    private List<String> h = new ArrayList<>(); // 主隊投票結果
    @Builder.Default
    private List<String> a = new ArrayList<>(); // 客隊投票結果
    @Builder.Default
    private List<String> d = new ArrayList<>(); // 和局投票結果



}
