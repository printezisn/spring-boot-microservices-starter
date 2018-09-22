package com.printezisn.moviestore.common.models;

import java.util.List;

/**
 * Model that holds information about the result of an operation
 *
 * @param <T>
 *            The type of the result
 */
public interface Result<T> {

    /**
     * Returns the result of the operation
     * 
     * @return The result
     */
    T getResult();

    /**
     * Returns the errors associated with the operation
     * 
     * @return The list of errors
     */
    List<String> getErrors();

    /**
     * Indicates if the operation has errors
     * 
     * @return True if the operation has errors, otherwise false
     */
    default boolean hasErrors() {
        return !getErrors().isEmpty();
    }
}
