package com.printezisn.moviestore.common.models.movie;

import java.util.List;

import com.printezisn.moviestore.common.dto.movie.MovieDto;

import lombok.Data;

/**
 * Class that holds the result of a movie service call
 */
@Data
public class MovieResultModel {
    private MovieDto result;
    private List<String> errors;
}
