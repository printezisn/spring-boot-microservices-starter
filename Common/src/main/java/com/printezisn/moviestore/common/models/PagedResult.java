package com.printezisn.moviestore.common.models;

import java.util.List;

/**
 * Model that holds paged entries
 *
 * @param <T>
 *            The type of entries
 */
public interface PagedResult<T> {

    /**
     * Returns the entries of the result
     * 
     * @return The list of entries
     */
    List<T> getEntries();

    /**
     * Returns the current page of the results
     * 
     * @return The current page
     */
    int getPageNumber();

    /**
     * Returns the total number of available pages
     * 
     * @return The total number of available pages
     */
    int getTotalPages();

    /**
     * Returns the field name used to sort the results
     * 
     * @return The sorting field name
     */
    String getSortField();

    /**
     * Indicates if the sorting is ascending or descending
     * 
     * @return True if the sorting is ascending, otherwise false
     */
    boolean isAscending();
}
