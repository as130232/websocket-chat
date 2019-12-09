package com.inno.innochat.domain.service;

import com.inno.innochat.common.io.VoteIO;
import com.inno.innochat.common.vo.VoteResultVO;
import com.inno.innochat.common.vo.VoteVO;

public interface VoteService {


    /**
     * 投票
     * @param account
     * @param voteIO
     * @return
     * @throws InterruptedException
     */
    VoteVO vote(String account, VoteIO voteIO) throws InterruptedException;


    /**
     * 取得投票結果
     * @param sid
     * @param iid
     * @return
     */
    VoteResultVO getVoteResult(Integer sid, Long iid);

    /**
     * 取得會員的投票結果
     * @param account
     * @return
     */
    VoteVO getVoteResult(String account, Integer sid, Long iid);
}
