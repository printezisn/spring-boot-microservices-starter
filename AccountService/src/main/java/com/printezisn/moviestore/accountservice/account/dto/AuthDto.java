package com.printezisn.moviestore.accountservice.account.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class AuthDto {
	
	@NotEmpty(message = "message.account.error.usernameRequired")
	private String username;
	
	@NotEmpty(message = "message.account.error.passwordRequired")
	private String password;
}
