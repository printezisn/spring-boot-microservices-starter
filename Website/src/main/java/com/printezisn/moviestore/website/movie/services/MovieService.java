package com.printezisn.moviestore.website.movie.services;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;

/**
 * The interface of the movie service
 */
public interface MovieService {

    /**
     * Creates a new movie
     * 
     * @param movieDto
     *            The model of the new movie
     * @return The created movie
     */
    MovieResultModel createMovie(final MovieDto movieDto);
}
