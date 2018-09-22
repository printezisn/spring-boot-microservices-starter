package com.printezisn.moviestore.accountservice.account.exceptions;

/**
 * Exception class indicating that an account was not found
 */
@SuppressWarnings("serial")
public class AccountNotFoundException extends Exception {

    /**
     * The constructor
     */
    public AccountNotFoundException() {
        super("The account was not found.");
    }

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     */
    public AccountNotFoundException(final String message) {
        super(message);
    }
}
