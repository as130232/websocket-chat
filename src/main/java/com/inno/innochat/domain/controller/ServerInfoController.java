package com.inno.innochat.domain.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.inno.innochat.common.vo.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * For get  server information
 */
@Api(tags = "Server Information")
@RestController
@Slf4j
public class ServerInfoController {

    @ApiOperation(value = "Health check", notes = "To check server is available or not")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Server avaliable")})
    @GetMapping ("/health")
    public Result<Map<String, Object>> checkHealth() {
        Result<Map<String, Object>> result = new Result<>();
        result.setCode(HttpStatus.OK.value());
        Map<String, Object> map = Maps.newHashMap();
        result.setData(map);
        result.setMsg(HttpStatus.OK.getReasonPhrase());
        return result;
    }


    @ApiOperation(value = "maven package version", notes = "To check server version")
    @GetMapping("/version")
    public String versionInformation() {
        return readGitProperties();
    }

    private String readGitProperties() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("git.properties");
        try {
            return readFromInputStream(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "Version information could not be retrieved";
        }
    }
    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("<br>");
            }
        }
        return resultStringBuilder.toString();
    }
}
