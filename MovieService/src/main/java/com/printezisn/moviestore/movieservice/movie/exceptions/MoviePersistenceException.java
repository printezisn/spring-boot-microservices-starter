package com.printezisn.moviestore.movieservice.movie.exceptions;

/**
 * Exception class for persistence errors regarding movies
 */
@SuppressWarnings("serial")
public class MoviePersistenceException extends MovieException {

	/**
	 * The constructor
	 */
	public MoviePersistenceException() {
		super();
	}
	
	/**
	 * The constructor
	 * 
	 * @param message The error message
	 */
	public MoviePersistenceException(final String message) {
		super(message);
	}
	
	/**
	 * The constructor
	 * 
	 * @param cause The error cause
	 */
	public MoviePersistenceException(final Throwable cause) {
		super(cause);
	}
	
	/**
	 * The constructor
	 * 
	 * @param message The error message
	 * @param cause The error cause
	 */
	public MoviePersistenceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}