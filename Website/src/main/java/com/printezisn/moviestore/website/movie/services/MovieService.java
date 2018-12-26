package com.printezisn.moviestore.website.movie.services;

import java.util.UUID;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.movie.exceptions.MovieNotFoundException;

/**
 * The interface of the movie service
 */
public interface MovieService {

    /**
     * Searches for movies
     * 
     * @param text
     *            The text to search for
     * @param pageNumber
     *            The page number
     * @param sortField
     *            The sorting field
     * @param isAscending
     *            Indicates if the sorting is ascending or descending
     * @return The movies found
     */
    MoviePagedResultModel searchMovies(final String text, final int pageNumber, final String sortField,
        final boolean isAscending);

    /**
     * Creates a new movie
     * 
     * @param movieDto
     *            The model of the new movie
     * @return The created movie
     */
    MovieResultModel createMovie(final MovieDto movieDto);

    /**
     * Fetches a movie
     * 
     * @param id
     *            The id of the movie
     * @return The movie found
     * @throws MovieNotFoundException
     *             Exception thrown when the movie is not found
     */
    MovieDto getMovie(final UUID id) throws MovieNotFoundException;
}
