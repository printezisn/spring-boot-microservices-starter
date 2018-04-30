package com.printezisn.moviestore.common.mappers;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Mapper class for UUID objects
 */
@Component
public class UUIDMapper {

	/**
	 * Converts a UUID object to string
	 * 
	 * @param uuid The UUID object
	 * @return The converted string
	 */
	public String toString(final UUID uuid) {
		return (uuid != null) ? uuid.toString() : null;
	}
	
	/**
	 * Converts a string object to UUID
	 * 
	 * @param str The string object
	 * @return The converted UUID
	 */
	public UUID toUUID(final String str) {
		return (str != null) ? UUID.fromString(str) : null;
	}
}
