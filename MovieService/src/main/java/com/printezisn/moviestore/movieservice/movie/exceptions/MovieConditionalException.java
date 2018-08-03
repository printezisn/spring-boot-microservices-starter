package com.printezisn.moviestore.movieservice.movie.exceptions;

/**
 * Exception class thrown when there is a conditional exception while updating a
 * database document
 */
@SuppressWarnings("serial")
public class MovieConditionalException extends MovieException {

    /**
     * The constructor
     */
    public MovieConditionalException() {
        super();
    }
}
