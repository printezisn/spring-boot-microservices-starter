package com.printezisn.moviestore.common.models.movie;

import java.util.LinkedList;
import java.util.List;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.PagedResult;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class that holds the paged result of a movie service call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoviePagedResultModel implements PagedResult<MovieDto> {

    @Builder.Default
    private List<MovieDto> entries = new LinkedList<MovieDto>();

    private int pageNumber;

    private int totalPages;

    private String sortField;

    private boolean isAscending;
}
