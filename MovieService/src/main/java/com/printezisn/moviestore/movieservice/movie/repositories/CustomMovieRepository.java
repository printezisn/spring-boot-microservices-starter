package com.printezisn.moviestore.movieservice.movie.repositories;

import com.printezisn.moviestore.movieservice.movie.entities.Movie;

/**
 * Interface with extra repository methods for movies
 */
public interface CustomMovieRepository {

    /**
     * Updates a movie
     * 
     * @param movie
     *            The movie
     * @param currentRevision
     *            The current revision of the movie
     * @return The number of documents affected
     */
    long updateMovie(final Movie movie, final String currentRevision);
}
