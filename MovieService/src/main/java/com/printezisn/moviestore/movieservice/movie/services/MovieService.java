package com.printezisn.moviestore.movieservice.movie.services;

import java.util.Optional;
import java.util.UUID;

import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieConditionalException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;

/**
 * The service layer for movies
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
    MoviePagedResultModel searchMovies(final Optional<String> text, final Optional<Integer> pageNumber,
        final Optional<String> sortField, final boolean isAscending);

    /**
     * Returns a movie
     * 
     * @param id
     *            The id of the movie
     * @return The movie
     * @throws MovieNotFoundException
     *             Exception thrown if the movie is not found
     */
    MovieDto getMovie(final UUID id) throws MovieNotFoundException;

    /**
     * Creates a new movie
     * 
     * @param movieDto
     *            The new movie model
     * @return The created movie
     */
    MovieDto createMovie(final MovieDto movieDto);

    /**
     * Updates a movie
     * 
     * @param movieDto
     *            The movie model
     * @return The updated movie
     * @throws MovieNotFoundException
     *             Exception thrown if the movie is not found
     * @throws MovieConditionalException
     *             Exception thrown if the movie update conflicts with another
     *             update
     */
    MovieDto updateMovie(final MovieDto movieDto) throws MovieNotFoundException, MovieConditionalException;

    /**
     * Deletes a movie
     * 
     * @param id
     *            The id of the movie to delete
     * @throws MovieConditionalException
     *             Exception thrown if the movie update conflicts with another
     *             update
     */
    void deleteMovie(final UUID id) throws MovieConditionalException;

    /**
     * Adds a like to a movie
     * 
     * @param movieId
     *            The id of the movie to like
     * @param account
     *            The account that likes the movie
     * @throws MovieConditionalException
     *             Exception thrown in case of conditional update failure
     * @throws MovieNotFoundException
     *             Exception thrown if the movie is not found
     */
    void likeMovie(final UUID movieId, final String account) throws MovieConditionalException, MovieNotFoundException;

    /**
     * Checks if an account has liked a movie
     * 
     * @param movieId
     *            The id of the movie
     * @param account
     *            The account
     * @return True if the account has liked the movie, otherwise false
     */
    boolean hasLiked(final UUID movieId, final String account);

    /**
     * Removes a like from a movie
     * 
     * @param movieId
     *            The id of the movie to unlike
     * @param account
     *            The account that removes the like from the movie
     * @throws MovieConditionalException
     *             Exception thrown in case of conditional update failure
     * @throws MovieNotFoundException
     *             Exception thrown if the movie is not found
     */
    void unlikeMovie(final UUID movieId, final String account)
        throws MovieConditionalException, MovieNotFoundException;
}
