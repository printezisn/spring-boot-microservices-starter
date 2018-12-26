package com.printezisn.moviestore.website.movie.exceptions;

/**
 * Exception class thrown when a movie is not found
 */
@SuppressWarnings("serial")
public class MovieNotFoundException extends Exception {

    /**
     * The constructor
     */
    public MovieNotFoundException() {
        super("The movie was not found.");
    }

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     */
    public MovieNotFoundException(final String message) {
        super(message);
    }
}