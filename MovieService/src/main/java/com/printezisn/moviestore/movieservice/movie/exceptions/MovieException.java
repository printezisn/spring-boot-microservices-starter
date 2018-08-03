package com.printezisn.moviestore.movieservice.movie.exceptions;

/**
 * General exception class regarding movies
 */
@SuppressWarnings("serial")
public class MovieException extends Exception {

    /**
     * The constructor
     */
    public MovieException() {
        super();
    }

    /**
     * The constructor
     * 
     * @param message
     *            The error message
     */
    public MovieException(final String message) {
        super(message);
    }

    /**
     * The constructor
     * 
     * @param cause
     *            The error cause
     */
    public MovieException(final Throwable cause) {
        super(cause);
    }

    /**
     * The constructor
     * 
     * @param message
     *            The error message
     * @param cause
     *            The error cause
     */
    public MovieException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
