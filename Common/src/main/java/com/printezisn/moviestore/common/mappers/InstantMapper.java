package com.printezisn.moviestore.common.mappers;

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
     * @param instant
     *            The Instant object
     * @return The converted string
     */
    public String toString(final Instant instant) {
        return (instant != null) ? instant.toString() : null;
    }

    /**
     * Converts a string object to Instant
     * 
     * @param str
     *            The string object
     * @return The converted Instant
     */
    public Instant toInstant(final String str) {
        return (str != null) ? Instant.parse(str) : null;
    }
}
