package com.printezisn.moviestore.common.models.movie;

import java.util.LinkedList;
import java.util.List;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.Result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class that holds the result of a movie service call
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResultModel implements Result<MovieDto> {

    private MovieDto result;

    @Builder.Default
    private List<String> errors = new LinkedList<>();
}
