package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.movieservice.movie.entities.SearchedMovie;

/**
 * The interface of the repository used for searching movies
 */
@Repository
public interface MovieSearchRepository extends ElasticsearchRepository<SearchedMovie, String>,
    CustomMovieSearchRepository {

}
