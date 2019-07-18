package com.example.websocketchat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OddsVO {
	private String roomId;
	private String username;
	
	private String counterpart;
	private String command;
	private String info;
}
