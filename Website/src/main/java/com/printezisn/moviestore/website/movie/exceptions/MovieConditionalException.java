package com.printezisn.moviestore.website.movie.exceptions;

/**
 * Exception class thrown when there is a conditional exception while updating a
 * movie
 */
@SuppressWarnings("serial")
public class MovieConditionalException extends Exception {

    /**
     * The constructor
     */
    public MovieConditionalException() {
        super("The conditional update failed.");
    }

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     */
    public MovieConditionalException(final String message) {
        super(message);
    }
}
