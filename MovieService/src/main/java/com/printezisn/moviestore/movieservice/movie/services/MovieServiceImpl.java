package com.printezisn.moviestore.movieservice.movie.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.printezisn.moviestore.common.models.PagedResult;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the service layer for movies
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private static List<String> SORT_FIELDS = Arrays.asList("rating", "releaseYear", "totalLikes");

    private static final int MAX_RETRIES = 5;
    private static final Duration RETRY_INTERVAL = Duration.ofMillis(500);
    private static final int PAGE_SIZE = 10;

    private final MovieRepository movieRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MovieSearchRepository movieSearchRepository;
    private final MovieMapper movieMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedResult<MovieDto> searchMovies(final Optional<String> text, final Optional<Integer> pageNumber,
        final Optional<String> sortField, final boolean isAscending)
        throws MoviePersistenceException {

        try {
            final String requiredSortField = (sortField.isPresent() && SORT_FIELDS.contains(sortField.get()))
                ? sortField.get()
                : SORT_FIELDS.get(0);

            final Pageable pageable = PageRequest.of(
                Math.max(0, pageNumber.orElse(0)),
                PAGE_SIZE,
                isAscending ? Direction.ASC : Direction.DESC,
                requiredSortField);

            final Page<SearchedMovie> page = movieSearchRepository.search(text, pageable);
            final List<MovieDto> results = page.getContent()
                .stream()
                .map(searchedMovie -> movieMapper.searchedMovieToMovieDto(searchedMovie))
                .collect(Collectors.toList());

            return new PagedResult<>(
                results,
                page.getNumber(),
                page.getTotalPages(),
                requiredSortField,
                isAscending);
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto getMovie(final UUID id)
        throws MoviePersistenceException, MovieNotFoundException {

        Optional<Movie> movie;
        try {
            movie = movieRepository.findById(id.toString());
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }

        if (!movie.isPresent()) {
            throw new MovieNotFoundException();
        }

        return movieMapper.movieToMovieDto(movie.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto createMovie(MovieDto movieDto) throws MoviePersistenceException {

        movieDto.setId(UUID.randomUUID());
        movieDto.setTotalLikes(0);
        movieDto.setCreationTimestamp(Instant.now());
        movieDto.setUpdateTimestamp(Instant.now());

        final Movie movie = movieMapper.movieDtoToMovie(movieDto);
        movie.setRevision(UUID.randomUUID().toString());

        try {
            movieRepository.save(movie);

            final SearchedMovie searchedMovie = movieMapper.movieToSearchedMovie(movie);
            movieSearchRepository.save(searchedMovie);
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }

        return movieDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto updateMovie(final MovieDto movieDto)
        throws MoviePersistenceException, MovieNotFoundException {

        movieDto.setUpdateTimestamp(Instant.now());

        final Movie movie = movieMapper.movieDtoToMovie(movieDto);
        long affectedDocuments;

        try {
            affectedDocuments = movieRepository.updateMovie(movie);
            if (affectedDocuments > 0) {
                final SearchedMovie searchedMovie = movieMapper.movieToSearchedMovie(movie);
                movieSearchRepository.save(searchedMovie);
            }
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }

        if (affectedDocuments == 0) {
            throw new MovieNotFoundException();
        }

        return movieDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMovie(final UUID id) throws MoviePersistenceException {
        try {
            movieSearchRepository.deleteById(id.toString());
            movieRepository.deleteById(id.toString());
            movieLikeRepository.deleteByMovieId(id.toString());
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto likeMovie(final UUID movieId, final String user)
        throws MoviePersistenceException, MovieConditionalException, MovieNotFoundException {

        final MovieLike movieLike = new MovieLike();
        movieLike.setId(movieId + "-" + user);
        movieLike.setMovieId(movieId.toString());
        movieLike.setUser(user);

        final Optional<MovieDto> movieDto;

        try {
            movieLikeRepository.save(movieLike);
            movieDto = updateTotalLikes(movieId.toString());
        }
        catch (final MovieConditionalException ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw ex;
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }

        if (!movieDto.isPresent()) {
            throw new MovieNotFoundException();
        }

        return movieDto.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto unlikeMovie(final UUID movieId, final String user)
        throws MoviePersistenceException, MovieConditionalException, MovieNotFoundException {

        final Optional<MovieDto> movieDto;
        final String id = movieId + "-" + user;

        try {
            movieLikeRepository.deleteById(id);
            movieDto = updateTotalLikes(movieId.toString());
        }
        catch (final MovieConditionalException ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw ex;
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new MoviePersistenceException(ex);
        }

        if (!movieDto.isPresent()) {
            throw new MovieNotFoundException();
        }

        return movieDto.get();
    }

    /**
     * Updates the total likes of a movie
     * 
     * @param movieId
     *            The id of the movie
     * @return The updated movie
     * @throws Exception
     *             Exception thrown by the callable method
     */
    private Optional<MovieDto> updateTotalLikes(final String movieId) throws Exception {
        return runWithRetry(() -> {
            final Optional<Movie> movie = movieRepository.findById(movieId);
            if (!movie.isPresent()) {
                return Optional.empty();
            }

            final long totalLikes = movieLikeRepository.countByMovieId(movieId);
            final String newRevision = UUID.randomUUID().toString();

            movie.get().setTotalLikes(totalLikes);
            if (movieRepository.updateTotalLikes(movie.get(), newRevision) == 0) {
                throw new MovieConditionalException();
            }

            movie.get().setRevision(newRevision);

            final SearchedMovie searchedMovie = movieMapper.movieToSearchedMovie(movie.get());
            movieSearchRepository.save(searchedMovie);

            return Optional.of(movieMapper.movieToMovieDto(movie.get()));
        });
    }

    /**
     * Runs a method and retries a few times in case of a conditional exception
     * 
     * @param callable
     *            The method to call
     * @return The return value of the method
     * @throws Exception
     *             Exception thrown by the callable method
     */
    private <T> T runWithRetry(final Callable<T> callable) throws Exception {
        int maxRetries = MAX_RETRIES;

        while (maxRetries > 0) {
            try {
                return callable.call();
            }
            catch (final MovieConditionalException ex) {
                maxRetries--;
                Thread.sleep(RETRY_INTERVAL.toMillis());
            }
        }

        throw new MovieConditionalException();
    }
}
