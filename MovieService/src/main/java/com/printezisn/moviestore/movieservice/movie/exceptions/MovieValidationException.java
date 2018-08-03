package com.printezisn.moviestore.movieservice.movie.exceptions;

/**
 * Exception class for validation errors regarding movies
 */
@SuppressWarnings("serial")
public class MovieValidationException extends MovieException {

    /**
     * The constructor
     * 
     * @param message
     *            The validation error message
     */
    public MovieValidationException(final String message) {
        super(message);
    }
}
