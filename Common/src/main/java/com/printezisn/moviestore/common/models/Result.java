package com.printezisn.moviestore.common.models;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

/**
 * Model that holds information about the result of an operation
 *
 * @param <T>
 *            The type of the result
 */
@Data
public class Result<T> {
    private T result;
    private List<String> errors;

    /**
     * The constructor
     */
    public Result() {
        this.errors = new LinkedList<>();
    }

    /**
     * The constructor
     * 
     * @param error
     *            An operation error
     */
    public Result(final String error) {
        this();
        this.errors.add(error);
    }

    /**
     * The constructor
     * 
     * @param result
     *            The result of the operation
     */
    public Result(final T result) {
        this();
        this.result = result;
    }
}
