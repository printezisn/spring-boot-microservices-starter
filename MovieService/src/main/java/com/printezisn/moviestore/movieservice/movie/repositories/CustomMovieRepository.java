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
     * @return The number of documents affected
     */
    long updateMovie(final Movie movie);

    /**
     * Updates the total likes of a movie
     * 
     * @param movie
     *            The movie
     * @param newRevision
     *            The new revision of the movie
     * @return The number of documents affected
     */
    long updateTotalLikes(final Movie movie, final String newRevision);
}
