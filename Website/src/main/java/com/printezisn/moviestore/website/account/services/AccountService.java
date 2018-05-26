package com.printezisn.moviestore.website.account.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;

/**
 * The interface of the account service
 */
public interface AccountService extends UserDetailsService {

	/**
	 * Authenticates a user
	 * 
	 * @param username The username
	 * @param password The password
	 * @return The details of the account
	 * @throws AccountAuthenticationException Exception thrown when there is an authentication error
	 * @throws AccountNotValidatedException Exception thrown when the account is not authenticated
	 */
	UserDetails authenticate(final String username, final String password)
		throws AccountAuthenticationException, AccountNotValidatedException;
}
