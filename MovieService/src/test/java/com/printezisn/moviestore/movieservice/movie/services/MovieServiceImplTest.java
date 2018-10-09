package com.printezisn.moviestore.movieservice.movie.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.entities.Movie;
import com.printezisn.moviestore.movieservice.movie.entities.MovieLike;
import com.printezisn.moviestore.movieservice.movie.entities.SearchedMovie;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieConditionalException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.movieservice.movie.exceptions.MoviePersistenceException;
import com.printezisn.moviestore.movieservice.movie.mappers.MovieMapper;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieLikeRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieSearchRepository;
import com.printezisn.moviestore.movieservice.movie.services.MovieServiceImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * Class that contains unit tests for the MovieServiceImpl class
 */
public class MovieServiceImplTest {

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

        final MoviePagedResultModel result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
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

        final MoviePagedResultModel result = movieService.searchMovies(Optional.empty(),
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

        final MoviePagedResultModel result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
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

        final MoviePagedResultModel result = movieService.searchMovies(Optional.of(SEARCH_TEXT),
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
     * Tests the scenario in which the search operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_searchMovies_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final SearchedMovie searchedMovie = new SearchedMovie();
        final ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        when(movieMapper.searchedMovieToMovieDto(searchedMovie)).thenReturn(movieDto);
        when(movieSearchRepository.search(eq(Optional.of(SEARCH_TEXT)), pageableCaptor.capture()))
            .thenThrow(new RuntimeException());

        movieService.searchMovies(Optional.of(SEARCH_TEXT), Optional.of(PAGE_NUMBER), Optional.of(SORT_FIELD),
            IS_ASCENDING);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_getMovie_notFound() throws Exception {
        final UUID id = UUID.randomUUID();
        when(movieRepository.findById(id.toString())).thenReturn(Optional.empty());

        movieService.getMovie(id);
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_getMovie_deleted() throws Exception {
        final UUID id = UUID.randomUUID();
        final Movie movie = new Movie();
        movie.setDeleted(true);

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));

        movieService.getMovie(id);
    }

    /**
     * Tests the scenario in which the movie is found
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
     * Tests the scenario in which the get operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_getMovie_exception() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieRepository.findById(id.toString())).thenThrow(new RuntimeException());

        movieService.getMovie(id);
    }

    /**
     * Tests if movie creation works correctly
     */
    @Test
    public void test_createMovie_success() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final Movie movie = new Movie();

        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(movie);

        final MovieDto result = movieService.createMovie(movieDto);

        verify(movieRepository).save(movie);

        assertEquals(movieDto, result);
        assertNotNull(result.getCreationTimestamp());
        assertNotNull(result.getUpdateTimestamp());
        assertNotNull(result.getId());

        assertNotNull(movie.getRevision());
        assertTrue(movie.isUpdated());
        assertFalse(movie.isDeleted());
        assertNotNull(movie.getPendingLikes());
        assertEquals(0, movie.getPendingLikes().size());
        assertNotNull(movie.getPendingUnlikes().size());
    }

    /**
     * Tests the scenario in which the create operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_createMovie_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final Movie movie = new Movie();

        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(movie);
        when(movieRepository.save(movie)).thenThrow(new RuntimeException());

        movieService.createMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_updateMovie_notFound() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        when(movieRepository.findById(movieDto.getId().toString())).thenReturn(Optional.empty());

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_updateMovie_deleted() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        final Movie movie = new Movie();
        movie.setDeleted(true);

        when(movieRepository.findById(movieDto.getId().toString())).thenReturn(Optional.of(movie));

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is updated successfully
     */
    @Test
    public void test_updateMovie_success() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);
        movie.setPendingLikes(Collections.singleton("account1"));
        movie.setPendingUnlikes(Collections.singleton("account2"));

        final Movie updatedMovie = new Movie();

        when(movieRepository.findById(movieDto.getId().toString())).thenReturn(Optional.of(movie));
        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(updatedMovie);
        when(movieRepository.updateMovie(updatedMovie, currentRevision)).thenReturn(1L);

        final MovieDto result = movieService.updateMovie(movieDto);

        assertEquals(movieDto, result);
        assertTrue(updatedMovie.isUpdated());
        assertFalse(updatedMovie.isDeleted());
        assertNotNull(updatedMovie.getPendingLikes());
        assertEquals(1, updatedMovie.getPendingLikes().size());
        assertTrue(updatedMovie.getPendingLikes().contains("account1"));
        assertNotNull(updatedMovie.getPendingUnlikes());
        assertEquals(1, updatedMovie.getPendingUnlikes().size());
        assertTrue(updatedMovie.getPendingUnlikes().contains("account2"));
    }

    /**
     * Tests the scenario in which the update operation throws a conditional
     * exception
     */
    @Test(expected = MovieConditionalException.class)
    public void test_updateMovie_conditionalException() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        final Movie updatedMovie = new Movie();

        when(movieRepository.findById(movieDto.getId().toString())).thenReturn(Optional.of(movie));
        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(updatedMovie);
        when(movieRepository.updateMovie(updatedMovie, currentRevision)).thenReturn(0L);

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the update operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_updateMovie_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        final Movie updatedMovie = new Movie();

        when(movieRepository.findById(movieDto.getId().toString())).thenReturn(Optional.of(movie));
        when(movieMapper.movieDtoToMovie(movieDto)).thenReturn(updatedMovie);
        when(movieRepository.updateMovie(updatedMovie, currentRevision)).thenThrow(new RuntimeException());

        movieService.updateMovie(movieDto);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test
    public void test_deleteMovie_notFound() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieRepository.findById(id.toString())).thenReturn(Optional.empty());

        movieService.deleteMovie(id);

        verify(movieRepository, never()).updateMovie(any(Movie.class), anyString());
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test
    public void test_deleteMovie_deleted() throws Exception {
        final UUID id = UUID.randomUUID();
        final Movie movie = new Movie();
        movie.setDeleted(true);

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));

        movieService.deleteMovie(id);

        verify(movieRepository, never()).updateMovie(any(Movie.class), anyString());
    }

    /**
     * Tests if the operation completes successfully
     */
    @Test
    public void test_deleteMovie_success() throws Exception {
        final UUID id = UUID.randomUUID();
        final String currentRevision = UUID.randomUUID().toString();

        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenReturn(1L);

        movieService.deleteMovie(id);

        assertTrue(movie.isUpdated());
        assertTrue(movie.isDeleted());
    }

    /**
     * Tests the scenario in which the delete operation throws a conditional
     * exception
     */
    @Test(expected = MovieConditionalException.class)
    public void test_deleteMovie_conditionalException() throws Exception {
        final UUID id = UUID.randomUUID();
        final String currentRevision = UUID.randomUUID().toString();

        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenReturn(0L);

        movieService.deleteMovie(id);
    }

    /**
     * Tests the scenario in which the delete operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_deleteMovie_exception() throws Exception {
        final UUID id = UUID.randomUUID();
        final String currentRevision = UUID.randomUUID().toString();

        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        when(movieRepository.findById(id.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenThrow(new RuntimeException());

        movieService.deleteMovie(id);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_likeMovie_movieNotFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.empty());

        movieService.likeMovie(movieId, "test_account");
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_likeMovie_deleted() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final Movie movie = new Movie();
        movie.setDeleted(true);

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));

        movieService.likeMovie(movieId, "test_account");
    }

    /**
     * Tests if the movie is liked successfully
     */
    @Test
    public void test_likeMovie_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>(Arrays.asList(account)));

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenReturn(1L);

        movieService.likeMovie(movieId, account);

        assertEquals(1, movie.getPendingLikes().size());
        assertTrue(movie.getPendingLikes().contains(account));
        assertEquals(0, movie.getPendingUnlikes().size());
    }

    /**
     * Tests the scenario in which the like operation throws a conditional exception
     */
    @Test(expected = MovieConditionalException.class)
    public void test_likeMovie_conditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        final Movie updatedMovie = new Movie();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(updatedMovie, currentRevision)).thenReturn(0L);

        movieService.likeMovie(movieId, account);
    }

    /**
     * Tests the scenario in which the like operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_likeMovie_exception() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenThrow(new RuntimeException());

        movieService.likeMovie(movieId, account);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_unlikeMovie_movieNotFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.empty());

        movieService.unlikeMovie(movieId, "test_account");
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test(expected = MovieNotFoundException.class)
    public void test_unlikeMovie_deleted() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final Movie movie = new Movie();
        movie.setDeleted(true);

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));

        movieService.unlikeMovie(movieId, "test_account");
    }

    /**
     * Tests if the movie is unliked successfully
     */
    @Test
    public void test_unlikeMovie_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>(Arrays.asList(account)));
        movie.setPendingUnlikes(new HashSet<>());

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenReturn(1L);

        movieService.unlikeMovie(movieId, account);

        assertEquals(0, movie.getPendingLikes().size());
        assertEquals(1, movie.getPendingUnlikes().size());
        assertTrue(movie.getPendingUnlikes().contains(account));
    }

    /**
     * Tests the scenario in which the unlike operation throws a conditional
     * exception
     */
    @Test(expected = MovieConditionalException.class)
    public void test_unlikeMovie_conditionalException() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenReturn(0L);

        movieService.unlikeMovie(movieId, account);
    }

    /**
     * Tests the scenario in which the unlike operation throws a runtime exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_unlikeMovie_exception() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setRevision(currentRevision);

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieRepository.updateMovie(movie, currentRevision)).thenThrow(new RuntimeException());

        movieService.unlikeMovie(movieId, account);
    }

    /**
     * Tests the scenario in which a movie is deleted
     */
    @Test
    public void test_updateSearchIndex_deleteMovie() {
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setDeleted(true);

        when(movieRepository.findByUpdated(true)).thenReturn(Arrays.asList(movie));

        movieService.updateSearchIndex();

        verify(movieRepository).deleteById(movie.getId());
        verify(movieSearchRepository).deleteById(movie.getId());
        verify(movieLikeRepository).deleteByMovieId(movie.getId());
    }

    /**
     * Tests the scenario in which a movie is updated
     */
    @Test
    public void test_updateSearchIndex_updateMovie() {
        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>(Arrays.asList("account1")));
        movie.setPendingUnlikes(new HashSet<>(Arrays.asList("account2")));

        final SearchedMovie searchedMovie = new SearchedMovie();

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movie.getId() + "-account1");
        movieLike.setMovieId(movie.getId());
        movieLike.setAccount("account1");

        when(movieRepository.findByUpdated(true)).thenReturn(Arrays.asList(movie));
        when(movieMapper.movieToSearchedMovie(movie)).thenReturn(searchedMovie);
        when(movieLikeRepository.countByMovieId(movie.getId())).thenReturn(5L);

        movieService.updateSearchIndex();

        verify(movieLikeRepository).save(movieLike);
        verify(movieLikeRepository).deleteById(movie.getId() + "-account2");
        verify(movieSearchRepository).save(searchedMovie);
        verify(movieRepository).updateMovie(movie, currentRevision);

        assertTrue(movie.getPendingLikes().isEmpty());
        assertTrue(movie.getPendingUnlikes().isEmpty());
        assertFalse(movie.isUpdated());
    }

    /**
     * Tests the scenario in which an exception is thrown while loading movies
     */
    @Test
    public void test_updateSearchIndex_loadException() {
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setDeleted(true);

        when(movieRepository.findByUpdated(true)).thenThrow(new RuntimeException());

        movieService.updateSearchIndex();

        verify(movieRepository, never()).deleteById(movie.getId());
    }

    /**
     * Tests the scenario in which an exception is thrown while processing a movie
     */
    @Test
    public void test_updateSearchIndex_processException() {
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setDeleted(true);

        when(movieRepository.findByUpdated(true)).thenReturn(Arrays.asList(movie));
        doThrow(new RuntimeException()).when(movieLikeRepository).deleteByMovieId(movie.getId());

        movieService.updateSearchIndex();

        verify(movieRepository, never()).deleteById(movie.getId());
    }

    /**
     * Tests the scenario in which the movie doesn't exist
     */
    @Test
    public void test_hasLiked_notFound() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.empty());

        final boolean result = movieService.hasLiked(movieId, account);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the movie is deleted
     */
    @Test
    public void test_hasLiked_deleted() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setDeleted(true);

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));

        final boolean result = movieService.hasLiked(movieId, account);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the account is stored in the pending likes
     */
    @Test
    public void test_hasLiked_inPendingLikes() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setPendingLikes(new HashSet<>(Arrays.asList(account)));

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));

        final boolean result = movieService.hasLiked(movieId, account);

        assertTrue(result);
    }

    /**
     * Tests the scenario in which the account is stored in the pending unlikes
     */
    @Test
    public void test_hasLiked_inPendingUnlikes() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>(Arrays.asList(account)));

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));

        final boolean result = movieService.hasLiked(movieId, account);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the account has not liked the movie
     */
    @Test
    public void test_hasLiked_notLiked() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.findById(movieId + "-" + account)).thenReturn(Optional.empty());

        final boolean result = movieService.hasLiked(movieId, account);

        assertFalse(result);
    }

    /**
     * Tests the scenario in which the account has liked the movie
     */
    @Test
    public void test_hasLiked_liked() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        final MovieLike movieLike = new MovieLike();

        when(movieRepository.findById(movieId.toString())).thenReturn(Optional.of(movie));
        when(movieLikeRepository.findById(movieId + "-" + account)).thenReturn(Optional.of(movieLike));

        final boolean result = movieService.hasLiked(movieId, account);

        assertTrue(result);
    }

    /**
     * Tests the scenario in which the operations throws an exception
     */
    @Test(expected = MoviePersistenceException.class)
    public void test_hasLiked_exception() {
        final UUID movieId = UUID.randomUUID();
        final String account = "test_account";

        final Movie movie = new Movie();
        movie.setId(movieId.toString());
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        when(movieRepository.findById(movieId.toString())).thenThrow(new RuntimeException());

        movieService.hasLiked(movieId, account);
    }
}
