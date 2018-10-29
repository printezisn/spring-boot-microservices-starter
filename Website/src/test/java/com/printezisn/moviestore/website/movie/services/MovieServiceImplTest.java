package com.printezisn.moviestore.website.movie.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;
import com.printezisn.moviestore.website.movie.exceptions.MoviePersistenceException;

/**
 * Contains unit tests for the MovieServiceImpl class
 */
public class MovieServiceImplTest {

    private static final String MOVIE_SERVICE_URL = "http://localhost";
    private static final String MOVIE_CREATE_PATH = "/movie/new?lang=en";

    @Mock
    private ServiceProperties serviceProperties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<MovieResultModel> response;

    private MovieServiceImpl movieService;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        movieService = new MovieServiceImpl(serviceProperties, restTemplate);

        when(serviceProperties.getMovieServiceUrl()).thenReturn(MOVIE_SERVICE_URL);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    /**
     * Tests the scenario in which the movie is created successfully
     */
    @Test
    public void test_createMovie_success() throws Exception {
        final MovieResultModel expectedResult = new MovieResultModel();
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_CREATE_PATH;

        when(response.getBody()).thenReturn(expectedResult);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenReturn(response);

        final MovieResultModel result = movieService.createMovie(movieDto);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests the scenario in which the movie creation throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_createMovie_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_CREATE_PATH;

        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        movieService.createMovie(movieDto);
    }
}
