package com.printezisn.moviestore.movieservice.movie.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.printezisn.moviestore.common.mappers.InstantMapper;
import com.printezisn.moviestore.common.mappers.UUIDMapper;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.entities.Movie;
import com.printezisn.moviestore.movieservice.movie.entities.MovieIndex;

/**
 * The mapper class for the Movie entity
 */
@Mapper(componentModel = "spring", uses = { InstantMapper.class, UUIDMapper.class })
public interface MovieMapper {

    /**
     * Converts a Movie object to MovieDto
     * 
     * @param movie
     *            The Movie object to convert
     * @return The converted MovieDto object
     */
    @Mappings({
        @Mapping(target = "totalLikes", ignore = true)
    })
    MovieDto movieToMovieDto(final Movie movie);

    /**
     * Converts a MovieDto object to Movie
     * 
     * @param movieDto
     *            The MovieDto object to convert
     * @return The converted Movie object
     */
    @Mappings({
        @Mapping(target = "revision", ignore = true),
        @Mapping(target = "pendingLikes", ignore = true),
        @Mapping(target = "pendingUnlikes", ignore = true),
        @Mapping(target = "updated", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    Movie movieDtoToMovie(final MovieDto movieDto);

    /**
     * Converts a Movie object to MovieIndex
     * 
     * @param movie
     *            The Movie object to convert
     * @return The converted MovieIndex object
     */
    @Mappings({
        @Mapping(target = "totalLikes", ignore = true)
    })
    MovieIndex movieToMovieIndex(final Movie movie);

    /**
     * Converts a MovieIndex object to MovieDto
     * 
     * @param movieIndex
     *            The MovieIndex object to convert
     * @return The converted MovieDto object
     */
    @Mappings({
        @Mapping(target = "creationTimestamp", ignore = true),
        @Mapping(target = "updateTimestamp", ignore = true)
    })
    MovieDto movieIndexToMovieDto(final MovieIndex movieIndex);
}
