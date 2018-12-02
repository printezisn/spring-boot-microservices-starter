package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.printezisn.moviestore.movieservice.movie.entities.MovieIndex;

/**
 * The interface of the repository used for indexing movies
 */
@Repository
public interface MovieIndexRepository extends ElasticsearchRepository<MovieIndex, String>,
    CustomMovieIndexRepository {

}
