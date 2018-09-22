package com.printezisn.moviestore.movieservice.movie.exceptions;

/**
 * Exception class for persistence errors regarding movies
 */
@SuppressWarnings("serial")
public class MoviePersistenceException extends RuntimeException {

    /**
     * The constructor
     * 
     * @param message
     *            The error message
     * @param cause
     *            The error cause
     */
    public MoviePersistenceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}