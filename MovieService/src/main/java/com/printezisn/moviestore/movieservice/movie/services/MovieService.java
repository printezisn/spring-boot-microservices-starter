package com.printezisn.moviestore.movieservice.movie.services;

import java.util.Optional;
import java.util.UUID;

import com.printezisn.moviestore.movieservice.global.models.PagedResult;
import com.printezisn.moviestore.movieservice.movie.dto.MovieDto;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieConditionalException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MoviePersistenceException;

/**
 * The service layer for movies
 */
public interface MovieService {

	/**
	 * Searches for movies
	 * 
	 * @param text The text to search for
	 * @param pageNumber The page number
	 * @param sortField The sorting field
	 * @param isAscending Indicates if the sorting is ascending or descending
	 * @return The movies found
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	PagedResult<MovieDto> searchMovies(final Optional<String> text, final Optional<Integer> pageNumber,
		final Optional<String> sortField, final boolean isAscending)
		throws MoviePersistenceException;
	
	/**
	 * Returns a movie
	 * 
	 * @param id The id of the movie
	 * @return The movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieNotFoundException Exception thrown if the movie is not found
	 */
	MovieDto getMovie(final UUID id)
		throws MoviePersistenceException, MovieNotFoundException;
	
	/**
	 * Creates a new movie
	 * 
	 * @param movieDto The new movie model
	 * @return The created movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	MovieDto createMovie(final MovieDto movieDto) throws MoviePersistenceException;
	
	/**
	 * Updates a movie
	 * 
	 * @param movieDto The movie model
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieNotFoundException Exception thrown if the movie is not found
	 */
	MovieDto updateMovie(final MovieDto movieDto)
		throws MoviePersistenceException, MovieNotFoundException;
	
	/**
	 * Deletes a movie
	 * 
	 * @param id The id of the movie to delete
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	void deleteMovie(final UUID id) throws MoviePersistenceException;
	
	/**
	 * Adds a like to a movie
	 * 
	 * @param movieId The id of the movie to like
	 * @param userId The id of the user who likes the movie
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieConditionalException Exception thrown in case of conditional update failure
	 * @throws MovieNotFoundException Exception thrown if the movie is not found
	 */
	MovieDto likeMovie(final UUID movieId, final UUID userId)
			throws MoviePersistenceException, MovieConditionalException, MovieNotFoundException;
	
	/**
	 * Removes a like from a movie
	 * 
	 * @param movieId The id of the movie to unlike
	 * @param userId The id of the user who removes the like from the movie
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieConditionalException Exception thrown in case of conditional update failure
	 * @throws MovieNotFoundException Exception thrown if the movie is not found
	 */
	MovieDto unlikeMovie(final UUID movieId, final UUID userId)
			throws MoviePersistenceException, MovieConditionalException, MovieNotFoundException;
}
