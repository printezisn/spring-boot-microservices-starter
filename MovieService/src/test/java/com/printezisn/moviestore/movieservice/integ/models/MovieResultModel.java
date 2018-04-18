package com.printezisn.moviestore.movieservice.integ.models;

import java.util.List;

import com.printezisn.moviestore.movieservice.movie.dto.MovieDto;

import lombok.Data;

/**
 * Class that holds the result of a movie service call
 */
@Data
public class MovieResultModel {
	private MovieDto result;
	private List<String> errors;
}
