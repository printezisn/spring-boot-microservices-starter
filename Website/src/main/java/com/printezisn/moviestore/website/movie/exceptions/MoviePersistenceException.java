package com.printezisn.moviestore.website.movie.exceptions;

/**
 * Exception class related to persistence errors for the movie entity
 */
@SuppressWarnings("serial")
public class MoviePersistenceException extends RuntimeException {

    /**
     * The constructor
     * 
     * @param message
     *            The exception message
     * @param cause
     *            The inner exception
     */
    public MoviePersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
