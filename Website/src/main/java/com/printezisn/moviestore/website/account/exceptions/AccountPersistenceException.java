package com.printezisn.moviestore.website.account.exceptions;

/**
 * Exception class related to persistence errors for the account entity
 */
@SuppressWarnings("serial")
public class AccountPersistenceException extends RuntimeException {

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     * @param cause
     *            The inner exception
     */
    public AccountPersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}