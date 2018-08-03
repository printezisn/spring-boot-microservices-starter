package com.printezisn.moviestore.movieservice.movie.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.printezisn.moviestore.common.models.PagedResult;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.entities.Movie;
import com.printezisn.moviestore.movieservice.movie.entities.MovieLike;
import com.printezisn.moviestore.movieservice.movie.entities.SearchedMovie;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieConditionalException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.movieservice.movie.mappers.MovieMapper;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieLikeRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieSearchRepository;
import com.printezisn.moviestore.movieservice.movie.services.MovieServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyString;

/**
 * Class that contains unit tests for the MovieServiceImpl class
 */
public class MovieServiceImplTest {

    private static final long TOTAL_LIKES = 5;
    private static final String SEARCH_TEXT = "test";
    private static final int PAGE_NUMBER = 1;
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int TOTAL_PAGES = 5;
    private static final String SORT_FIELD = "totalLikes";
    private static final String DEFAULT_SORT_FIELD = "rating";
    private static final boolean IS_ASCENDING = true;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieLikeRepository movieLikeRepository;

    @Mock
    private MovieSearchRepository movieSearchRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private Page<SearchedMovie> pagedResult;

    private MovieServiceImpl movieService;

    /**
     * Sets up the unit tests
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.movieService = new MovieServiceImpl(movieRepository, movieLikeRepository,
            movieSearchRepository, movieMapper);
    }

    /**
     * Tests if movies are searched successfully
     */
    @Test
    public void test_searchMovies_success() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();
        final List<SearchedMovie> contentList = Arrays.asList(searchedMovie);
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(movieMapper.searchedMovieToMovieDto(searchedMovie)).thenReturn(movieDto);
        when(movieSearchRepository.search(eq(Optional.of(SEARCH_TEXT)), pageableCaptor.capture()))
            .thenReturn(pagedResult);
        when(pagedResult.getContent()).thenReturn(contentList);
        when(pagedResult.getNumber()).thenReturn(PAGE_NUMBER);
        when(pagedResult.getTotalPages()).thenReturn(TOTAL_PAGES);

        final PagedResult<MovieDto> result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
            Optional.of(PAGE_NUMBER), Optional.of(SORT_FIELD), IS_ASCENDING);

        assertEquals(PAGE_NUMBER, pageableCaptor.getValue().getPageNumber());
        assertEquals(IS_ASCENDING, pageableCaptor.getValue().getSort().getOrderFor(SORT_FIELD).isAscending());

        assertEquals(PAGE_NUMBER, result.getPageNumber());
        assertEquals(TOTAL_PAGES, result.getTotalPages());
        assertEquals(SORT_FIELD, result.getSortField());
        assertEquals(IS_ASCENDING, result.isAscending());
        assertEquals(1, result.getEntries().size());
        assertEquals(movieDto, result.getEntries().get(0));
    }

    /**
     * Tests if default values are used correctly
     */
    @Test
    public void test_searchMovies_defaultValue() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();
        final List<SearchedMovie> contentList = Arrays.asList(searchedMovie);
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(movieMapper.searchedMovieToMovieDto(searchedMovie)).thenReturn(movieDto);
        when(movieSearchRepository.search(eq(Optional.empty()), pageableCaptor.capture()))
            .thenReturn(pagedResult);
        when(pagedResult.getContent()).thenReturn(contentList);
        when(pagedResult.getNumber()).thenReturn(DEFAULT_PAGE_NUMBER);
        when(pagedResult.getTotalPages()).thenReturn(TOTAL_PAGES);

        final PagedResult<MovieDto> result = movieService.searchMovies(Optional.empty(),
            Optional.empty(), Optional.empty(), IS_ASCENDING);

        assertEquals(DEFAULT_PAGE_NUMBER, pageableCaptor.getValue().getPageNumber());
        assertEquals(IS_ASCENDING, pageableCaptor.getValue().getSort().getOrderFor(DEFAULT_SORT_FIELD).isAscending());

        assertEquals(DEFAULT_PAGE_NUMBER, result.getPageNumber());
        assertEquals(TOTAL_PAGES, result.getTotalPages());
        assertEquals(DEFAULT_SORT_FIELD, result.getSortField());
        assertEquals(IS_ASCENDING, result.isAscending());
        assertEquals(1, result.getEntries().size());
        assertEquals(movieDto, result.getEntries().get(0));
    }

    /**
     * Tests if the default sort field is used in case an invalid sort field is
     * entered
     */
    @Test
    public void test_searchMovies_invalidSortField() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();
        final List<SearchedMovie> contentList = Arrays.asList(searchedMovie);
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(movieMapper.searchedMovieToMovieDto(searchedMovie)).thenReturn(movieDto);
        when(movieSearchRepository.search(eq(Optional.of(SEARCH_TEXT)), pageableCaptor.capture()))
            .thenReturn(pagedResult);
        when(pagedResult.getContent()).thenReturn(contentList);
        when(pagedResult.getNumber()).thenReturn(PAGE_NUMBER);
        when(pagedResult.getTotalPages()).thenReturn(TOTAL_PAGES);

        final PagedResult<MovieDto> result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
            Optional.of(PAGE_NUMBER), Optional.of("wrong"), IS_ASCENDING);

        assertEquals(PAGE_NUMBER, pageableCaptor.getValue().getPageNumber());
        assertEquals(IS_ASCENDING, pageableCaptor.getValue().getSort().getOrderFor(DEFAULT_SORT_FIELD).isAscending());

        assertEquals(PAGE_NUMBER, result.getPageNumber());
        assertEquals(TOTAL_PAGES, result.getTotalPages());
        assertEquals(DEFAULT_SORT_FIELD, result.getSortField());
        assertEquals(IS_ASCENDING, result.isAscending());
        assertEquals(1, result.getEntries().size());
        assertEquals(movieDto, result.getEntries().get(0));
    }

    /**
     * Tests if the default page number is used in case an invalid one is entered
     */
    @Test
    public void test_searchMovies_invalidPageNumber() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();
        final List<SearchedMovie> contentList = Arrays.asList(searchedMovie);
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(movieMapper.searchedMovieToMovieDto(searchedMovie)).thenReturn(movieDto);
        when(movieSearchRepository.search(eq(Optional.of(SEARCH_TEXT)), pageableCaptor.capture()))
            .thenReturn(pagedResult);
        when(pagedResult.getContent()).thenReturn(contentList);
        when(pagedResult.getNumber()).thenReturn(DEFAULT_PAGE_NUMBER);
        when(pagedResult.getTotalPages()).thenReturn(TOTAL_PAGES);

        final PagedResult<MovieDto> result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
            Optional.of(-1), Optional.of(SORT_FIELD), IS_ASCENDING);

        assertEquals(DEFAULT_PAGE_NUMBER, pageableCaptor.getValue().getPageNumber());
        assertEquals(IS_ASCENDING, pageableCaptor.getValue().getSort().getOrderFor(SORT_FIELD).isAscending());

        assertEquals(DEFAULT_PAGE_NUMBER, result.getPageNumber());
        assertEquals(TOTAL_PAGES, result.getTotalPages());
        assertEquals(SORT_FIELD, result.getSortField());
        assertEquals(IS_ASCENDING, result.isAscending());
        assertEquals(1, result.getEntries().size());
        assertEquals(movieDto, result.getEntries().get(0));
    }

    /**
     * Tests the scenario in which the movie is not found
     * 
     * @throws Exception
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_getMovie_notFound() throws Exception {
        final UUID id = UUID.randomUUID();
        when(movieRepository.findById(id.toString())).thenReturn(Optional.empty());

        movieService.getMovie(id);
    }

    /**
     * Tests the scenario in which the movie is found
     * 
     * @throws Exception
     */
    @Test
    public void test_getMovie_found() throws Exception {
        final UUID id = UUID.randomUUID();
        final Movie movie = new Movie();
        final MovieDto movieDto = new MovieDto();

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));
        when(movieMapper.movieToMovieDto(movie)).thenReturn(movieDto);

        final MovieDto result = movieService.getMovie(id);

        assertEquals(movieDto, result);
    }

    /**
     * Tests if movie creation works correctly
     * 
     * @throws Exception
     */
    @Test
    public void test_createMovie_success() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final Movie movie = new Movie();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(movie);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);

        final MovieDto result = movieService.createMovie(movieDto);

        verify(movieRepository).save(movie);
        verify(movieSearchRepository).save(searchedMovie);

        assertEquals(movieDto, result);
        assertNotNull(result.getCreationTimestamp());
        assertNotNull(result.getUpdateTimestamp());
        assertNotNull(result.getId());
        assertEquals(0, result.getTotalLikes());
    }

    /**
     * Tests the scenario in which the movie is not found
     * 
     * @throws Exception
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_updateMovie_notFound() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final Movie movie = new Movie();

        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(movie);
        when(movieRepository.updateMovie(movie)).thenReturn(0L);

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is updated successfully
     * 
     * @throws Exception
     */
    @Test
    public void test_updateMovie_success() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final Movie movie = new Movie();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(movie);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);
        when(movieRepository.updateMovie(movie)).thenReturn(1L);

        final MovieDto result = movieService.updateMovie(movieDto);

        verify(movieSearchRepository).save(searchedMovie);
        assertEquals(movieDto, result);
    }

    /**
     * Tests if movie deletion works correctly
     * 
     * @throws Exception
     */
    @Test
    public void test_deleteMovie_success() throws Exception {
        final UUID id = UUID.randomUUID();

        movieService.deleteMovie(id);

        verify(movieRepository).deleteById(id.toString());
        verify(movieLikeRepository).deleteByMovieId(id.toString());
        verify(movieSearchRepository).deleteById(id.toString());
    }

    /**
     * Tests the scenario in which the movie is not found
     * 
     * @throws Exception
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_likeMovie_movieNotFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.empty());

        movieService.likeMovie(movieId, "test_user");
    }

    /**
     * Tests if the update is successful
     * 
     * @throws Exception
     */
    @Test
    public void test_likeMovie_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movieId + "-" + user);
        movieLike.setMovieId(movieId.toString());
        movieLike.setUser(user);

        final Movie movie = new Movie();
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString())).thenReturn(1L);
        when(movieMapper.movieToMovieDto(movie)).thenReturn(movieDto);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);

        final MovieDto result = movieService.likeMovie(movieId, user);

        verify(movieLikeRepository).save(movieLike);
        verify(movieSearchRepository).save(searchedMovie);
        assertEquals(movieDto, result);
        assertEquals(TOTAL_LIKES, movie.getTotalLikes());
    }

    /**
     * Tests if the update is successful even with a single conditional exception
     * 
     * @throws Exception
     */
    @Test
    public void test_likeMovie_successWithConditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movieId + "-" + user);
        movieLike.setMovieId(movieId.toString());
        movieLike.setUser(user);

        final Movie movie = new Movie();
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString()))
            .thenReturn(0L)
            .thenReturn(1L);
        when(movieMapper.movieToMovieDto(movie)).thenReturn(movieDto);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);

        final MovieDto result = movieService.likeMovie(movieId, user);

        verify(movieLikeRepository).save(movieLike);
        verify(movieSearchRepository).save(searchedMovie);
        assertEquals(movieDto, result);
        assertEquals(TOTAL_LIKES, movie.getTotalLikes());
    }

    /**
     * Tests if the update completes with errors after a series of conditional
     * exceptions
     * 
     * @throws Exception
     */
    @Test
    public void test_likeMovie_conditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movieId + "-" + user);
        movieLike.setMovieId(movieId.toString());
        movieLike.setUser(user);

        final Movie movie = new Movie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString())).thenReturn(0L);

        try {
            movieService.likeMovie(movieId, user);
            fail();
        }
        catch (final MovieConditionalException ex) {

        }

        verify(movieLikeRepository).save(movieLike);
    }

    /**
     * Tests the scenario in which the movie is not found
     * 
     * @throws Exception
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_unlikeMovie_movieNotFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.empty());

        movieService.unlikeMovie(movieId, "test_user");
    }

    /**
     * Tests if the update is successful
     * 
     * @throws Exception
     */
    @Test
    public void test_unlikeMovie_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final Movie movie = new Movie();
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString())).thenReturn(1L);
        when(movieMapper.movieToMovieDto(movie)).thenReturn(movieDto);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);

        final MovieDto result = movieService.unlikeMovie(movieId, user);

        verify(movieLikeRepository).deleteById(movieId + "-" + user);
        verify(movieSearchRepository).save(searchedMovie);
        assertEquals(movieDto, result);
        assertEquals(TOTAL_LIKES, movie.getTotalLikes());
    }

    /**
     * Tests if the update is successful even with a single conditional exception
     * 
     * @throws Exception
     */
    @Test
    public void test_unlikeMovie_successWithConditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final Movie movie = new Movie();
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString()))
            .thenReturn(0L)
            .thenReturn(1L);
        when(movieMapper.movieToMovieDto(movie)).thenReturn(movieDto);
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);

        final MovieDto result = movieService.unlikeMovie(movieId, user);

        verify(movieLikeRepository).deleteById(movieId + "-" + user);
        verify(movieSearchRepository).save(searchedMovie);
        assertEquals(movieDto, result);
        assertEquals(TOTAL_LIKES, movie.getTotalLikes());
    }

    /**
     * Tests if the update completes with errors after a series of conditional
     * exceptions
     * 
     * @throws Exception
     */
    @Test
    public void test_unlikeMovie_conditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String user = "test_user";

        final Movie movie = new Movie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.countByMovieId(movieId.toString())).thenReturn(TOTAL_LIKES);
        when(movieRepository.updateTotalLikes(eq(movie), anyString())).thenReturn(0L);

        try {
            movieService.unlikeMovie(movieId, user);
            fail();
        }
        catch (final MovieConditionalException ex) {

        }

        verify(movieLikeRepository).deleteById(movieId + "-" + user);
    }
}
