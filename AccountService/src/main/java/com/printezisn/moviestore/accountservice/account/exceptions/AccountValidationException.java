package com.printezisn.moviestore.accountservice.account.exceptions;

/**
 * Exception class indicating validation error for an account
 */
@SuppressWarnings("serial")
public class AccountValidationException extends Exception {

    /**
     * The constructor
     * 
     * @param message
     *            The error message
     */
    public AccountValidationException(final String message) {
        super(message);
    }
}
