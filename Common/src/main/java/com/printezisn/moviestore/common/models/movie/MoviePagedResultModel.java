package com.printezisn.moviestore.common.models.movie;

import java.util.List;

import com.printezisn.moviestore.common.dto.movie.MovieDto;

import lombok.Data;

/**
 * Class that holds the paged result of a movie service call
 */
@Data
public class MoviePagedResultModel {
	private List<MovieDto> entries;
	private int pageNumber;
	private int totalPages;
	private String sortField;
	private boolean isAscending;
}
