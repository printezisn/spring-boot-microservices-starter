package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.movieservice.movie.entities.MovieLike;

/**
 * The repository layer for movie likes
 */
@Repository
public interface MovieLikeRepository extends MongoRepository<MovieLike, String> {

	/**
	 * Deletes movie likes based on movie id
	 * 
	 * @param movieId The movie id
	 */
	void deleteByMovieId(final String movieId);
	
	/**
	 * Returns the number of likes for a movie
	 * 
	 * @param movieId The id of the movie
	 * @return The number of likes
	 */
	long countByMovieId(final String movieId);
}
