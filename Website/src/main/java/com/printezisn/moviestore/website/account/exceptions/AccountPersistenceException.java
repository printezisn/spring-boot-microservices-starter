package com.printezisn.moviestore.website.account.exceptions;

/**
 * Exception class related to persistence errors for the account entity
 */
@SuppressWarnings("serial")
public class AccountPersistenceException extends AccountException {

    /**
     * The constructor
     */
    public AccountPersistenceException() {
        super();
    }

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     */
    public AccountPersistenceException(final String message) {
        super(message);
    }

    /**
     * The constructor
     * 
     * @param cause
     *            The inner cause
     */
    public AccountPersistenceException(final Throwable cause) {
        super(cause);
    }

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