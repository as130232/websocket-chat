package com.inno.innochat.domain.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inno.innochat.common.vo.Result;
import com.inno.innochat.domain.service.VoteService;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("vote")
public class VoteController {


    private VoteService voteService;

    @Autowired
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @ApiOperation("查詢當前使用者投票結果")
    @GetMapping("result")
    public Result getVote(@RequestParam("account") String account,
                          @RequestParam("sid") Integer sid,
                          @RequestParam("iid") Long iid){


        Result result = new Result();

        result.setCode(Result.SUCCESS_CODE);
        result.setData(voteService.getVoteResult(account, sid, iid));

        return result;
    }

}
