package com.printezisn.moviestore.movieservice.movie.helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.printezisn.moviestore.movieservice.movie.entities.Movie;
import com.printezisn.moviestore.movieservice.movie.entities.MovieIndex;
import com.printezisn.moviestore.movieservice.movie.entities.MovieLike;
import com.printezisn.moviestore.movieservice.movie.mappers.MovieMapper;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieIndexRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieLikeRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieRepository;

/**
 * Class that contains unit tests for the MovieIndexHelper class
 */
public class MovieIndexHelperTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieLikeRepository movieLikeRepository;

    @Mock
    private MovieIndexRepository movieIndexRepository;

    @Mock
    private MovieMapper movieMapper;

    private MovieIndexHelper movieIndexHelper;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        movieIndexHelper = new MovieIndexHelper(movieRepository, movieLikeRepository, movieIndexRepository,
            movieMapper);
    }

    /**
     * Tests the scenario in which a movie is deleted
     */
    @Test
    public void test_indexMovie_deleteMovie() {
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setDeleted(true);

        movieIndexHelper.indexMovie(movie);

        verify(movieRepository).deleteById(movie.getId());
        verify(movieIndexRepository).deleteById(movie.getId());
        verify(movieLikeRepository).deleteByMovieId(movie.getId());
    }

    /**
     * Tests the scenario in which a movie is updated
     */
    @Test
    public void test_indexMovie_updateMovie() {
        final String currentRevision = UUID.randomUUID().toString();
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setRevision(currentRevision);
        movie.setPendingLikes(new HashSet<>(Arrays.asList("account1")));
        movie.setPendingUnlikes(new HashSet<>(Arrays.asList("account2")));

        final MovieIndex movieIndex = new MovieIndex();

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movie.getId() + "-account1");
        movieLike.setMovieId(movie.getId());
        movieLike.setAccount("account1");

        when(movieRepository.findByUpdated(true)).thenReturn(Arrays.asList(movie));
        when(movieMapper.movieToMovieIndex(movie)).thenReturn(movieIndex);
        when(movieLikeRepository.countByMovieId(movie.getId())).thenReturn(5L);

        movieIndexHelper.indexMovie(movie);

        verify(movieLikeRepository).save(movieLike);
        verify(movieLikeRepository).deleteById(movie.getId() + "-account2");
        verify(movieIndexRepository).save(movieIndex);
        verify(movieRepository).updateMovie(movie, currentRevision);

        assertTrue(movie.getPendingLikes().isEmpty());
        assertTrue(movie.getPendingUnlikes().isEmpty());
        assertFalse(movie.isUpdated());
    }

    /**
     * Tests the scenario in which an exception is thrown while processing a movie
     */
    @Test
    public void test_indexMovie_processException() {
        final Movie movie = new Movie();
        movie.setId(UUID.randomUUID().toString());
        movie.setDeleted(true);

        doThrow(new RuntimeException()).when(movieLikeRepository).deleteByMovieId(movie.getId());

        movieIndexHelper.indexMovie(movie);

        verify(movieRepository, never()).deleteById(movie.getId());
    }
}
