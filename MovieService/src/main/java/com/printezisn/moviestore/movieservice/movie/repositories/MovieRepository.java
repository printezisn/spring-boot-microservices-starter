package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.movieservice.movie.entities.Movie;

/**
 * The repository layer for movies
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String>, CustomMovieRepository {

}
