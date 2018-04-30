package com.printezisn.moviestore.movieservice.movie.controllers;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.printezisn.moviestore.common.controllers.BaseController;
import com.printezisn.moviestore.common.models.PagedResult;
import com.printezisn.moviestore.common.models.Result;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieConditionalException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MoviePersistenceException;
import com.printezisn.moviestore.movieservice.movie.services.MovieService;

import lombok.RequiredArgsConstructor;

/**
 * The movies controller
 */
@RestController
@RequiredArgsConstructor
public class MovieController extends BaseController {

	private final MovieService movieService;
	private final MessageSource messageSource;
	
	/**
	 * Searches for movies
	 * 
	 * @param text The text to search for
	 * @param pageNumber The page number
	 * @param sortField The sorting field
	 * @param isAscending Indicates if sorting is ascending or descending
	 * @return The movies found
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	@GetMapping("/movie/search")
	public ResponseEntity<?> searchMovies(
		@RequestParam(value = "text") final Optional<String> text,
		@RequestParam(value = "page") final Optional<Integer> pageNumber,
		@RequestParam(value = "sort") final Optional<String> sortField,
		@RequestParam(value = "asc", defaultValue = "true") final boolean isAscending)
		throws MoviePersistenceException {
		
		final PagedResult<MovieDto> result = movieService.searchMovies(text, pageNumber, sortField, isAscending);
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * Returns a movie
	 * 
	 * @param id The id of the movie
	 * @return The movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	@GetMapping("/movie/get/{id}")
	public ResponseEntity<?> getMovie(@PathVariable("id") final UUID id)
		throws MoviePersistenceException {
		
		try {
			final MovieDto result = movieService.getMovie(id);
			
			return ResponseEntity.ok(result);
		}
		catch(final MovieNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
	/**
	 * Creates a new movie
	 * 
	 * @param movieDto The movie model
	 * @param bindingResult The model binding result
	 * @return The created movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	@PostMapping("/movie/new")
	public ResponseEntity<?> createMovie(@Valid @RequestBody final MovieDto movieDto, final BindingResult bindingResult)
		throws MoviePersistenceException {
		
		final Result<MovieDto> errorResult = getErrorResult(bindingResult, messageSource, "id");
		if(!errorResult.getErrors().isEmpty()) {
			return ResponseEntity.badRequest().body(errorResult);
		}
		
		final MovieDto createdMovieDto = movieService.createMovie(movieDto);
		final Result<MovieDto> result = new Result<>(createdMovieDto);
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * Updates an existing movie
	 * 
	 * @param movieDto The movie model
	 * @param bindingResult The model binding result
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	@PostMapping("/movie/update")
	public ResponseEntity<?> updateMovie(@Valid @RequestBody final MovieDto movieDto, final BindingResult bindingResult)
		throws MoviePersistenceException {
		
		final Result<MovieDto> errorResult = getErrorResult(bindingResult, messageSource, "creatorId");
		if(!errorResult.getErrors().isEmpty()) {
			return ResponseEntity.badRequest().body(errorResult);
		}
		
		try {
			final MovieDto updatedMovieDto = movieService.updateMovie(movieDto);
			final Result<MovieDto> result = new Result<>(updatedMovieDto);
			
			return ResponseEntity.ok(result);
		}
		catch(final MovieNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
	/**
	 * Deletes a movie
	 * 
	 * @param id The id of the movie
	 * @return The result of the operation
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 */
	@GetMapping("/movie/delete/{id}")
	public ResponseEntity<?> deleteMovie(@PathVariable("id") final UUID id)
		throws MoviePersistenceException {
		
		movieService.deleteMovie(id);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * Adds a like to a movie
	 * 
	 * @param movieId The id of the movie to like
	 * @param userId The id of the user who likes the movie
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieConditionalException Exception thrown in case of conditional update failure
	 */
	@GetMapping("/movie/like/{movieId}/{userId}")
	public ResponseEntity<?> likeMovie(
			@PathVariable("movieId") final UUID movieId,
			@PathVariable("userId") final UUID userId)
			throws MoviePersistenceException, MovieConditionalException {
		
		try {
			final MovieDto result = movieService.likeMovie(movieId, userId);
			
			return ResponseEntity.ok(result);
		}
		catch(final MovieNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
	
	/**
	 * Removes a like from a movie
	 * 
	 * @param movieId The id of the movie to unlike
	 * @param userId The id of the user whose like is removed from the movie
	 * @return The updated movie
	 * @throws MoviePersistenceException Exception thrown in case of persistence error
	 * @throws MovieConditionalException Exception thrown in case of conditional update failure
	 */
	@GetMapping("/movie/unlike/{movieId}/{userId}")
	public ResponseEntity<?> unlikeMovie(
			@PathVariable("movieId") final UUID movieId,
			@PathVariable("userId") final UUID userId)
			throws MoviePersistenceException, MovieConditionalException {
		
		try {
			final MovieDto result = movieService.unlikeMovie(movieId, userId);
			
			return ResponseEntity.ok(result);
		}
		catch(final MovieNotFoundException ex) {
			return ResponseEntity.notFound().build();
		}
	}
}
