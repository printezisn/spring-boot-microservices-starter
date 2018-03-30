package com.printezisn.moviestore.accountservice.account.exceptions;

/**
 * General exception class related to account entity
 */
@SuppressWarnings("serial")
public class AccountException extends Exception {

	/**
	 * The constructor
	 */
	public AccountException() {
		super();
	}
	
	/**
	 * The constructor
	 * 
	 * @param message The exception message
	 */
	public AccountException(final String message) {
		super(message);
	}
	
	/**
	 * The constructor
	 * 
	 * @param cause The inner cause
	 */
	public AccountException(final Throwable cause) {
		super(cause);
	}
	
	/**
	 * The constructor
	 * 
	 * @param message The exception message
	 * @param cause The inner exception
	 */
	public AccountException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
