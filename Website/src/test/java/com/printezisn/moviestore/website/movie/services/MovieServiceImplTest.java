package com.printezisn.moviestore.website.movie.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;
import java.util.UUID;

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
import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;
import com.printezisn.moviestore.website.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.website.movie.exceptions.MoviePersistenceException;

/**
 * Contains unit tests for the MovieServiceImpl class
 */
public class MovieServiceImplTest {

    private static final String MOVIE_SERVICE_URL = "http://localhost";
    private static final String MOVIE_SEARCH_URL = "/movie/search?text=test_text&page=2&sort=rating&asc=true&lang=en";
    private static final String MOVIE_CREATE_PATH = "/movie/new?lang=en";
    private static final String MOVIE_UPDATE_PATH = "/movie/update?lang=en";
    private static final String MOVIE_DELETE_PATH = "/movie/delete/%s?lang=en";
    private static final String MOVIE_GET_PATH = "/movie/get/%s?lang=en";
    private static final String MOVIE_LIKE_PATH = "/movie/like/%s/%s?lang=en";
    private static final String MOVIE_UNLIKE_PATH = "/movie/unlike/%s/%s?lang=en";
    private static final String MOVIE_HAS_LIKED_PATH = "/movie/hasliked/%s/%s?lang=en";

    @Mock
    private ServiceProperties serviceProperties;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity<MovieResultModel> movieResultModelResponse;

    @Mock
    private ResponseEntity<MovieDto> movieDtoResponse;

    @Mock
    private ResponseEntity<MoviePagedResultModel> searchResponse;

    @Mock
    private ResponseEntity<Void> voidResponse;

    @Mock
    private ResponseEntity<Boolean> booleanResponse;

    private MovieServiceImpl movieService;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        movieService = spy(new MovieServiceImpl(serviceProperties, restTemplate));

        when(serviceProperties.getMovieServiceUrl()).thenReturn(MOVIE_SERVICE_URL);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    /**
     * Tests the scenario in which movies are searched successfully
     */
    @Test
    public void test_searchMovies_success() throws Exception {
        final MoviePagedResultModel expectedResult = mock(MoviePagedResultModel.class);
        final String url = MOVIE_SERVICE_URL + MOVIE_SEARCH_URL;

        when(searchResponse.getBody()).thenReturn(expectedResult);
        when(searchResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, MoviePagedResultModel.class)).thenReturn(searchResponse);

        final MoviePagedResultModel result = movieService.searchMovies("test_text", 2, "rating", true);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests the scenario in which the movie search throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_searchMovies_exception() throws Exception {
        final String url = MOVIE_SERVICE_URL + MOVIE_SEARCH_URL;

        when(restTemplate.getForEntity(url, MoviePagedResultModel.class))
            .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        movieService.searchMovies("test_text", 2, "rating", true);
    }

    /**
     * Tests the scenario in which the movie is created successfully
     */
    @Test
    public void test_createMovie_success() throws Exception {
        final MovieResultModel expectedResult = mock(MovieResultModel.class);
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_CREATE_PATH;

        when(movieResultModelResponse.getBody()).thenReturn(expectedResult);
        when(movieResultModelResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenReturn(movieResultModelResponse);

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

    /**
     * Tests the scenario in which the movie is fetched successfully
     */
    @Test
    public void test_getMovie_success() throws Exception {
        final UUID id = UUID.randomUUID();
        final MovieDto expectedResult = mock(MovieDto.class);
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_GET_PATH, id);

        when(restTemplate.getForEntity(url, MovieDto.class)).thenReturn(movieDtoResponse);
        when(movieDtoResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(movieDtoResponse.getBody()).thenReturn(expectedResult);

        final MovieDto result = movieService.getMovie(id);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_getMovie_notFound() throws Exception {
        final UUID id = UUID.randomUUID();
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_GET_PATH, id);

        when(restTemplate.getForEntity(url, MovieDto.class)).thenReturn(movieDtoResponse);
        when(movieDtoResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        movieService.getMovie(id);
    }

    /**
     * Tests the scenario in which the operations throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_getMovie_exception() throws Exception {
        final UUID id = UUID.randomUUID();
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_GET_PATH, id);

        when(restTemplate.getForEntity(url, MovieDto.class)).thenThrow(new RuntimeException());

        movieService.getMovie(id);
    }

    /**
     * Tests the scenario in which the movie is updated successfully
     */
    @Test
    public void test_updateMovie_success() throws Exception {
        final MovieResultModel expectedResult = mock(MovieResultModel.class);
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_UPDATE_PATH;

        when(movieResultModelResponse.getBody()).thenReturn(expectedResult);
        when(movieResultModelResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenReturn(movieResultModelResponse);

        final MovieResultModel result = movieService.updateMovie(movieDto);

        assertEquals(expectedResult, result);
    }

    /**
     * Tests the scenario in which the movie update throws a conflict error first
     */
    @Test
    public void test_updateMovie_conflict() throws Exception {
        final MovieResultModel expectedResult = mock(MovieResultModel.class);
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_UPDATE_PATH;

        when(movieResultModelResponse.getBody()).thenReturn(expectedResult);
        when(movieResultModelResponse.getStatusCode())
            .thenReturn(HttpStatus.CONFLICT)
            .thenReturn(HttpStatus.OK);
        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenReturn(movieResultModelResponse);

        final MovieResultModel result = movieService.updateMovie(movieDto);

        assertEquals(expectedResult, result);
        verify(restTemplate, times(2)).postForEntity(url, movieDto, MovieResultModel.class);
    }

    /**
     * Tests the scenario in which the movie update throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_updateMovie_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_UPDATE_PATH;

        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class)).thenThrow(new RuntimeException());

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_updateMovie_notFound() throws Exception {
        final MovieDto movieDto = new MovieDto();

        final String url = MOVIE_SERVICE_URL + MOVIE_UPDATE_PATH;

        when(movieResultModelResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(restTemplate.postForEntity(url, movieDto, MovieResultModel.class))
            .thenReturn(movieResultModelResponse);

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the account is authorized on the movie
     */
    @Test
    public void test_isAuthorizedOnMovie_withId_true() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final MovieDto movieDto = new MovieDto();
        movieDto.setCreator(account);

        doReturn(movieDto).when(movieService).getMovie(movieId);

        final boolean result = movieService.isAuthorizedOnMovie(account, movieId);

        assertTrue(result);
    }

    /**
     * Tests the scenario in which the account is not authorized on the movie
     */
    @Test
    public void test_isAuthorizedOnMovie_withId_false() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final MovieDto movieDto = new MovieDto();
        movieDto.setCreator("other_account");

        doReturn(movieDto).when(movieService).getMovie(movieId);

        final boolean result = movieService.isAuthorizedOnMovie(account, movieId);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_isAuthorizedOnMovie_withId_notFound() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        doThrow(new MovieNotFoundException()).when(movieService).getMovie(movieId);

        movieService.isAuthorizedOnMovie(account, movieId);
    }

    /**
     * Tests the scenario in which the account is authorized on the movie
     */
    @Test
    public void test_isAuthorizedOnMovie_withEntity_true() throws Exception {
        final String account = "test_account";

        final MovieDto movieDto = new MovieDto();
        movieDto.setCreator(account);

        final boolean result = movieService.isAuthorizedOnMovie(account, movieDto);

        assertTrue(result);
    }

    /**
     * Tests the scenario in which the account is not authorized on the movie
     */
    @Test
    public void test_isAuthorizedOnMovie_withEntity_false() throws Exception {
        final String account = "test_account";

        final MovieDto movieDto = new MovieDto();
        movieDto.setCreator("other_account");

        final boolean result = movieService.isAuthorizedOnMovie(account, movieDto);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the movie is deleted successfully
     */
    @Test
    public void test_deleteMovie_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_DELETE_PATH, movieId);

        when(voidResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.deleteMovie(movieId);
    }

    /**
     * Tests the scenario in which the movie deletion throws a conflict error first
     */
    @Test
    public void test_deleteMovie_conflict() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_DELETE_PATH, movieId);

        when(voidResponse.getStatusCode())
            .thenReturn(HttpStatus.CONFLICT)
            .thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.deleteMovie(movieId);
        verify(restTemplate, times(2)).getForEntity(url, Void.class);
    }

    /**
     * Tests the scenario in which the movie deletion throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_deleteMovie_exception() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_DELETE_PATH, movieId);

        when(restTemplate.getForEntity(url, Void.class)).thenThrow(new RuntimeException());

        movieService.deleteMovie(movieId);
    }

    /**
     * Tests the scenario in which the movie is liked successfully
     */
    @Test
    public void test_likeMovie_success() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_LIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.likeMovie(account, movieId);

        verify(restTemplate).getForEntity(url, Void.class);
    }

    /**
     * Tests the scenario in which the movie like throws a conflict error first
     */
    @Test
    public void test_likeMovie_conflict() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_LIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode())
            .thenReturn(HttpStatus.CONFLICT)
            .thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.likeMovie(account, movieId);

        verify(restTemplate, times(2)).getForEntity(url, Void.class);
    }

    /**
     * Tests the scenario in which the movie like throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_likeMovie_exception() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_LIKE_PATH, movieId, account);

        when(restTemplate.getForEntity(url, Void.class)).thenThrow(new RuntimeException());

        movieService.likeMovie(account, movieId);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_likeMovie_notFound() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_LIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.likeMovie(account, movieId);
    }

    /**
     * Tests the scenario in which the movie is unliked successfully
     */
    @Test
    public void test_unlikeMovie_success() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_UNLIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.unlikeMovie(account, movieId);

        verify(restTemplate).getForEntity(url, Void.class);
    }

    /**
     * Tests the scenario in which the movie unlike throws a conflict error first
     */
    @Test
    public void test_unlikeMovie_conflict() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_UNLIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode())
            .thenReturn(HttpStatus.CONFLICT)
            .thenReturn(HttpStatus.OK);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.unlikeMovie(account, movieId);

        verify(restTemplate, times(2)).getForEntity(url, Void.class);
    }

    /**
     * Tests the scenario in which the movie unlike throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_unlikeMovie_exception() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_UNLIKE_PATH, movieId, account);

        when(restTemplate.getForEntity(url, Void.class)).thenThrow(new RuntimeException());

        movieService.unlikeMovie(account, movieId);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_unlikeMovie_notFound() throws Exception {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_UNLIKE_PATH, movieId, account);

        when(voidResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        when(restTemplate.getForEntity(url, Void.class)).thenReturn(voidResponse);

        movieService.unlikeMovie(account, movieId);
    }

    /**
     * Tests if the correct result is returned when the account has liked the movie
     */
    @Test
    public void test_hasLiked_true() {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_HAS_LIKED_PATH, movieId, account);

        when(booleanResponse.getBody()).thenReturn(true);
        when(restTemplate.getForEntity(url, Boolean.class)).thenReturn(booleanResponse);

        final boolean result = movieService.hasLiked(account, movieId);

        assertTrue(result);
    }

    /**
     * Tests if the correct result is returned when the account hasn't liked the
     * movie
     */
    @Test
    public void test_hasLiked_false() {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_HAS_LIKED_PATH, movieId, account);

        when(booleanResponse.getBody()).thenReturn(false);
        when(restTemplate.getForEntity(url, Boolean.class)).thenReturn(booleanResponse);

        final boolean result = movieService.hasLiked(account, movieId);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the operation throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_hasLiked_exception() {
        final String account = "test_account";
        final UUID movieId = UUID.randomUUID();

        final String url = MOVIE_SERVICE_URL + String.format(MOVIE_HAS_LIKED_PATH, movieId, account);

        when(restTemplate.getForEntity(url, Boolean.class)).thenThrow(new RuntimeException());

        movieService.hasLiked(account, movieId);
    }
}
