package com.printezisn.moviestore.movieservice.movie.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.printezisn.moviestore.movieservice.movie.entities.SearchedMovie;

/**
 * Interface with extra repository methods for searching movies
 */
public interface CustomMovieSearchRepository {

    /**
     * Searches for movies using full text search
     * 
     * @param text
     *            The text used as filter
     * @param pageable
     *            The pageable criteria
     * @return The movies found
     */
    Page<SearchedMovie> search(final Optional<String> text, final Pageable pageable);
}
