package com.printezisn.moviestore.website.movie.services;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;
import com.printezisn.moviestore.website.movie.exceptions.MoviePersistenceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the movie service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private static final String CREATE_URL = "%s/movie/new?lang=%s";

    private final ServiceProperties serviceProperties;

    private final RestTemplate restTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieResultModel createMovie(final MovieDto movieDto) {
        final String url = String.format(CREATE_URL, serviceProperties.getMovieServiceUrl(),
            LocaleContextHolder.getLocale().getLanguage());

        try {
            return restTemplate.postForEntity(url, movieDto, MovieResultModel.class).getBody();
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while creating movie %s: %s",
                movieDto.getTitle(), ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }
    }

}
