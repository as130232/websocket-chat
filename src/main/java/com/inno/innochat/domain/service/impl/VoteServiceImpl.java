package com.inno.innochat.domain.service.impl;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import com.inno.innochat.common.constant.SportIdEnum;
import com.inno.innochat.common.io.VoteIO;
import com.inno.innochat.common.po.VotePO;
import com.inno.innochat.common.vo.VoteResultVO;
import com.inno.innochat.common.vo.VoteVO;
import com.inno.innochat.domain.service.VoteService;

@Component
public class VoteServiceImpl implements VoteService {

    private RedisTemplate<String, Object> redisTemplate;
    private RedisLockRegistry redisLockRegistry;

    @Autowired
    public VoteServiceImpl(RedisTemplate<String, Object> redisTemplate, RedisLockRegistry redisLockRegistry) {
        this.redisTemplate = redisTemplate;
        this.redisLockRegistry = redisLockRegistry;
    }

    @Override
    public VoteVO vote(String account, VoteIO voteIO) throws InterruptedException {

        VoteVO vo = VoteVO.builder().build();


        // 檢核
        if (StringUtils.isBlank(account)
                || voteIO == null
                || voteIO.getSid() == null
                || voteIO.getIid() == null
                || voteIO.isResultCorrect() == false) {
            return vo;
        }

        if ((voteIO.getSid() != SportIdEnum.FOOTBALL.getValue() || voteIO.getSid() != SportIdEnum.BASEBALL.getValue())
                && VoteIO.D.equals(voteIO.getResult())) {
            // 只有足球與棒球才有和局
            return vo;
        }
        Lock lock = redisLockRegistry.obtain("Inplay::Vote::lock::" + voteIO.getSid() + "_" + voteIO.getIid());
        lock.tryLock(5, TimeUnit.SECONDS);
        try {
            // 投票
            boolean isSuccess = doVote(account, voteIO);
            vo.setSuccess(isSuccess);
            vo.setResult(isSuccess ? voteIO.getResult() : Strings.EMPTY);
        } finally {
            lock.unlock();
        }


        return vo;
    }

    @Override
    public VoteResultVO getVoteResult(Integer sid, Long iid) {
        VoteResultVO vo = VoteResultVO.builder().build();
        String key = getVoteRedisKey(sid, iid);
        if (redisTemplate.hasKey(key) == false) {
            return vo;
        }
        VotePO votePO = (VotePO) redisTemplate.opsForValue().get(getVoteRedisKey(sid, iid));

        BigDecimal hsize = BigDecimal.valueOf(votePO.getH().size());
        BigDecimal asize = BigDecimal.valueOf(votePO.getA().size());
        BigDecimal dsize = BigDecimal.valueOf(votePO.getD().size());
        BigDecimal all = hsize.add(asize).add(dsize);

        BigDecimal hpercent = hsize.divide(all, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal apercent = asize.divide(all, 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal dpercent = dsize.divide(all, 2, BigDecimal.ROUND_HALF_UP);
        vo = VoteResultVO.builder()
                .h(hpercent.multiply(BigDecimal.valueOf(100)).toBigInteger() + "%")
                .a(apercent.multiply(BigDecimal.valueOf(100)).toBigInteger() + "%")
                .d(dpercent.multiply(BigDecimal.valueOf(100)).toBigInteger() + "%")
                .build();


        return vo;
    }

    @Override
    public VoteVO getVoteResult(String account, Integer sid, Long iid) {
        VoteVO vo = VoteVO.builder().build();
        String key = getVoteRedisKey(sid, iid);
        if (redisTemplate.hasKey(key) == false) {
            return vo;
        }

        VotePO votePO = (VotePO) redisTemplate.opsForValue().get(getVoteRedisKey(sid, iid));
        String result = votePO.getH().contains(account) ? VoteIO.H : Strings.EMPTY;
        if(StringUtils.isBlank(result)){
            result = votePO.getA().contains(account) ? VoteIO.A : Strings.EMPTY;
        }
        if(StringUtils.isBlank(result)){
            result = votePO.getD().contains(account) ? VoteIO.D : Strings.EMPTY;
        }
        if(StringUtils.isNotBlank(result)){
            vo.setResult(result);

        }

        return vo;
    }


    private String getVoteRedisKey(Integer sid, Long iid) {
        return "inno-chat::Inplay::Vote::" + sid + "::" + iid;
    }

    private boolean doVote(String account, VoteIO voteIO) {
        String key = getVoteRedisKey(voteIO.getSid(), voteIO.getIid());
        VotePO votePO;
        if (redisTemplate.hasKey(key)) {
            votePO = (VotePO) redisTemplate.opsForValue().get(key);
            // 判斷是否投過票
            if (votePO.getH().contains(account)
                    || votePO.getA().contains(account)
                    || votePO.getD().contains(account)) {
                // 一場賽事一個人只能投一次票
                return false;
            }
        } else {
            votePO = VotePO.builder().build();
        }
        switch (voteIO.getResult()) {
            case VoteIO.H:
                votePO.getH().add(account);
                break;
            case VoteIO.A:
                votePO.getA().add(account);
                break;
            case VoteIO.D:
                votePO.getD().add(account);
                break;
        }
        redisTemplate.opsForValue().set(key, votePO);
        return true;
    }


}
