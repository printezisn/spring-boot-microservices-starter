package com.printezisn.moviestore.accountservice.account.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * The account entity
 */
@Data
@Document(collection = "accounts")
public class Account {
	@Id
	private String username;
	
	@Indexed(unique = true)
	private String emailAddress;
	
	private String password;
	
	private String passwordSalt;
	
	private String creationTimestamp;
	
	private String updateTimestamp;
}
