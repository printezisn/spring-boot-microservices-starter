package com.printezisn.moviestore.movieservice.movie.helpers;

import java.util.HashSet;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.printezisn.moviestore.movieservice.movie.entities.Movie;
import com.printezisn.moviestore.movieservice.movie.entities.MovieIndex;
import com.printezisn.moviestore.movieservice.movie.entities.MovieLike;
import com.printezisn.moviestore.movieservice.movie.mappers.MovieMapper;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieIndexRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieLikeRepository;
import com.printezisn.moviestore.movieservice.movie.repositories.MovieRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class used to index movies
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MovieIndexHelper {

    private final MovieRepository movieRepository;
    private final MovieLikeRepository movieLikeRepository;
    private final MovieIndexRepository movieIndexRepository;
    private final MovieMapper movieMapper;

    /**
     * Updates a movie in the search index and the database
     * 
     * @param movie
     *            The movie to update
     */
    public void indexMovie(final Movie movie) {
        try {
            // Deletes the movie if it's indicated as deleted
            if (movie.isDeleted()) {
                movieIndexRepository.deleteById(movie.getId());
                movieLikeRepository.deleteByMovieId(movie.getId());
                movieRepository.deleteById(movie.getId());

                return;
            }

            // Saves the pending likes
            movie.getPendingLikes().forEach(account -> {
                final MovieLike movieLike = new MovieLike();
                movieLike.setId(movie.getId() + "-" + account);
                movieLike.setMovieId(movie.getId());
                movieLike.setAccount(account);

                movieLikeRepository.save(movieLike);
            });
            movie.setPendingLikes(new HashSet<>());

            // Removes the pending unlikes
            movie.getPendingUnlikes()
                .forEach(account -> movieLikeRepository.deleteById(movie.getId() + "-" + account));
            movie.setPendingUnlikes(new HashSet<>());

            // Indexes the movie
            final MovieIndex movieIndex = movieMapper.movieToMovieIndex(movie);
            movieIndex.setTotalLikes(movieLikeRepository.countByMovieId(movie.getId()));
            movieIndexRepository.save(movieIndex);

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
    }
}
