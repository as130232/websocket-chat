package com.inno.innochat.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import lombok.Getter;

/**
 * 各微服務IP 路徑
 */
@Getter
@Configuration
@PropertySource("classpath:config.properties")
public class ServerHost {
    @Value("${inno.java.user.ip}")
    private String userIP;
    @Value("${inno.java.admin.ip}")
    private String adminIP;
}
