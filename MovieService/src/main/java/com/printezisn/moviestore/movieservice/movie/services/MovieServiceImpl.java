package com.printezisn.moviestore.movieservice.movie.services;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private static final int PAGE_SIZE = 10;

    private final MovieRepository movieRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MovieSearchRepository movieSearchRepository;
    private final MovieMapper movieMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public MoviePagedResultModel searchMovies(final Optional<String> text, final Optional<Integer> pageNumber,
        final Optional<String> sortField, final boolean isAscending) {

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

            return MoviePagedResultModel.builder()
                .entries(results)
                .pageNumber(page.getNumber())
                .totalPages(page.getTotalPages())
                .sortField(requiredSortField)
                .isAscending(isAscending)
                .build();
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while searching movies: %s", ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto getMovie(final UUID id) throws MovieNotFoundException {
        final Optional<Movie> movie;
        try {
            movie = movieRepository.findById(id.toString());
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while reading movie %s: %s", id,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        if (!movie.isPresent() || movie.get().isDeleted()) {
            throw new MovieNotFoundException();
        }

        return movieMapper.movieToMovieDto(movie.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto createMovie(MovieDto movieDto) {
        movieDto.setId(UUID.randomUUID());
        movieDto.setCreationTimestamp(Instant.now());
        movieDto.setUpdateTimestamp(Instant.now());

        final Movie movie = movieMapper.movieDtoToMovie(movieDto);
        movie.setRevision(UUID.randomUUID().toString());
        movie.setUpdated(true);
        movie.setDeleted(false);
        movie.setPendingLikes(new HashSet<>());
        movie.setPendingUnlikes(new HashSet<>());

        try {
            movieRepository.save(movie);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while creating a new movie: %s",
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        return movieDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MovieDto updateMovie(final MovieDto movieDto) throws MovieNotFoundException, MovieConditionalException {
        final long affectedDocuments;

        try {
            final Optional<Movie> movie = movieRepository.findById(movieDto.getId().toString());
            if (!movie.isPresent() || movie.get().isDeleted()) {
                throw new MovieNotFoundException();
            }

            movieDto.setUpdateTimestamp(Instant.now());

            final Movie updatedMovie = movieMapper.movieDtoToMovie(movieDto);
            updatedMovie.setRevision(UUID.randomUUID().toString());
            updatedMovie.setUpdated(true);
            updatedMovie.setDeleted(movie.get().isDeleted());
            updatedMovie.setPendingLikes(movie.get().getPendingLikes());
            updatedMovie.setPendingUnlikes(movie.get().getPendingUnlikes());

            affectedDocuments = movieRepository.updateMovie(updatedMovie, movie.get().getRevision());
        }
        catch (final MovieNotFoundException ex) {
            throw ex;
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while updating movie %s: %s", movieDto.getId(),
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        if (affectedDocuments == 0) {
            throw new MovieConditionalException();
        }

        return movieDto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMovie(final UUID id) throws MovieConditionalException {
        final long affectedDocuments;

        try {
            final Optional<Movie> movie = movieRepository.findById(id.toString());
            if (!movie.isPresent() || movie.get().isDeleted()) {
                return;
            }

            final String currentRevision = movie.get().getRevision();

            movie.get().setRevision(UUID.randomUUID().toString());
            movie.get().setUpdated(true);
            movie.get().setDeleted(true);

            affectedDocuments = movieRepository.updateMovie(movie.get(), currentRevision);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while deleting movie %s: %s", id,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        if (affectedDocuments == 0) {
            throw new MovieConditionalException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void likeMovie(final UUID movieId, final String user)
        throws MovieConditionalException, MovieNotFoundException {

        final long affectedDocuments;

        try {
            final Optional<Movie> movie = movieRepository.findById(movieId.toString());
            if (!movie.isPresent() || movie.get().isDeleted()) {
                throw new MovieNotFoundException();
            }

            final String currentRevision = movie.get().getRevision();
            movie.get().setRevision(UUID.randomUUID().toString());
            movie.get().setUpdated(true);
            movie.get().getPendingLikes().add(user);
            movie.get().getPendingUnlikes().remove(user);

            affectedDocuments = movieRepository.updateMovie(movie.get(), currentRevision);
        }
        catch (final MovieNotFoundException ex) {
            throw ex;
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while updating movie %s: %s", movieId,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        if (affectedDocuments == 0) {
            throw new MovieConditionalException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlikeMovie(final UUID movieId, final String user)
        throws MovieConditionalException, MovieNotFoundException {

        final long affectedDocuments;

        try {
            final Optional<Movie> movie = movieRepository.findById(movieId.toString());
            if (!movie.isPresent() || movie.get().isDeleted()) {
                throw new MovieNotFoundException();
            }

            final String currentRevision = movie.get().getRevision();
            movie.get().setRevision(UUID.randomUUID().toString());
            movie.get().setUpdated(true);
            movie.get().getPendingLikes().remove(user);
            movie.get().getPendingUnlikes().add(user);

            affectedDocuments = movieRepository.updateMovie(movie.get(), currentRevision);
        }
        catch (final MovieNotFoundException ex) {
            throw ex;
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while updating movie %s: %s", movieId,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new MoviePersistenceException(errorMessage, ex);
        }

        if (affectedDocuments == 0) {
            throw new MovieConditionalException();
        }
    }

    /**
     * Updates the search index at a regular interval
     */
    @Scheduled(fixedRateString = "${searchIndex.fixedRate}")
    public void updateSearchIndex() {
        final Collection<Movie> movies;

        try {
            // Loads the recently updated movies
            movies = movieRepository.findByUpdated(true);
        }
        catch (final Exception ex) {
            log.error("An error occured while loading movies to index: " + ex.getMessage(), ex);
            return;
        }

        movies.forEach(movie -> {
            try {
                // Deletes the movie if it's indicated as deleted
                if (movie.isDeleted()) {
                    movieSearchRepository.deleteById(movie.getId());
                    movieLikeRepository.deleteByMovieId(movie.getId());
                    movieRepository.deleteById(movie.getId());

                    return;
                }

                // Saves the pending likes
                movie.getPendingLikes().forEach(user -> {
                    final MovieLike movieLike = new MovieLike();
                    movieLike.setId(movie.getId() + "-" + user);
                    movieLike.setMovieId(movie.getId());
                    movieLike.setUser(user);

                    movieLikeRepository.save(movieLike);
                });
                movie.setPendingLikes(new HashSet<>());

                // Removes the pending unlikes
                movie.getPendingUnlikes().forEach(user -> movieLikeRepository.deleteById(movie.getId() + "-" + user));
                movie.setPendingUnlikes(new HashSet<>());

                // Indexes the movie
                final SearchedMovie searchedMovie = movieMapper.movieToSearchedMovie(movie);
                searchedMovie.setTotalLikes(movieLikeRepository.countByMovieId(movie.getId()));
                movieSearchRepository.save(searchedMovie);

                // Updates the movie in the database
                final String currentRevision = movie.getRevision();
                movie.setRevision(UUID.randomUUID().toString());
                movie.setUpdated(false);
                movieRepository.updateMovie(movie, currentRevision);
            }
            catch (final Exception ex) {
                log.error(String.format("An error occured while indexing movie %s: %s", movie.getId(), ex.getMessage()),
                    ex);
            }
        });
    }
}
