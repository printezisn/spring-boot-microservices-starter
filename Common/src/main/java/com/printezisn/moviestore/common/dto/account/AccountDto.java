package com.printezisn.moviestore.common.dto.account;

import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.printezisn.moviestore.common.RegexLibrary;

import lombok.Data;

/**
 * The data transfer object for accounts
 */
@Data
public class AccountDto {
	
	@NotNull(message = "message.account.error.idRequired")
	private UUID id;
	
	@NotEmpty(message = "message.account.error.usernameRequired")
	@Size(max = 50, message = "message.account.error.usernameMaxLength")
	@Pattern(regexp = RegexLibrary.USERNAME_REGEX, message = "message.account.error.usernameFormat")
	private String username;
	
	@NotEmpty(message = "message.account.error.emailAddressRequired")
	@Size(max = 250, message = "message.account.error.emailAddressMaxLength")
	@Email(message = "message.account.error.emailAddressFormat")
	private String emailAddress;
	
	@NotEmpty(message = "message.account.error.passwordRequired")
	@Size(max = 250, message = "message.account.error.passwordMaxLength")
	@Pattern(regexp = RegexLibrary.PASSWORD_REGEX, message = "message.account.error.passwordFormat")
	private String password;
	
	private String passwordSalt;
	
	private Instant creationTimestamp;
	
	private Instant updateTimestamp;
}
