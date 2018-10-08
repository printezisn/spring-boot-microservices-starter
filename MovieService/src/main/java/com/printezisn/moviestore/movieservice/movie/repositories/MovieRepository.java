package com.printezisn.moviestore.movieservice.movie.repositories;

import java.util.Collection;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.movieservice.movie.entities.Movie;

/**
 * The repository layer for movies
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String>, CustomMovieRepository {

    /**
     * Filters movies based on their "updated" field
     * 
     * @param updated
     *            The value of the "updated" field
     * @return A list with the movies found
     */
    Collection<Movie> findByUpdated(final boolean updated);
}
