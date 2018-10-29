package com.printezisn.moviestore.common.mappers;

import java.time.Instant;

import org.springframework.stereotype.Component;

/**
 * Mapper class for Instant objects
 */
@Component
public class InstantMapper {

    /**
     * Converts an Instant object to long
     * 
     * @param instant
     *            The Instant object
     * @return The converted long
     */
    public long toLong(final Instant instant) {
        return (instant != null) ? instant.toEpochMilli() : 0;
    }

    /**
     * Converts a long object to Instant
     * 
     * @param epochMilli
     *            The long object
     * @return The converted Instant
     */
    public Instant toInstant(final long epochMilli) {
        return Instant.ofEpochMilli(epochMilli);
    }
}
