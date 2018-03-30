package com.printezisn.moviestore.accountservice.global.mappers;

import java.time.Instant;

import org.springframework.stereotype.Component;

/**
 * Mapper class for Instant objects
 */
@Component
public class InstantMapper {

	/**
	 * Converts an Instant object to string
	 * 
	 * @param instant The Instant object
	 * @return The converted string
	 */
	public String toString(final Instant instant) {
		return instant.toString();
	}
	
	/**
	 * Converts a string object to Instant
	 * 
	 * @param str The string object
	 * @return The converted Instant
	 */
	public Instant toInstant(final String str) {
		return Instant.parse(str);
	}
}
