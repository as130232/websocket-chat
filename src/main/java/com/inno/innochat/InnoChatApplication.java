package com.inno.innochat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class InnoChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnoChatApplication.class, args);
	}

}
