package com.printezisn.moviestore.website.account.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception class for authentication errors
 */
@SuppressWarnings("serial")
public class AccountAuthenticationException extends AuthenticationException {

	/**
	 * {@inheritDoc}
	 */
	public AccountAuthenticationException(String msg) {
		super(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	public AccountAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}
}
