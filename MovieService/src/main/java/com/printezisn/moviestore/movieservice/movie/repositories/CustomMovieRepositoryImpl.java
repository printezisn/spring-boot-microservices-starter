package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.UpdateResult;
import com.printezisn.moviestore.movieservice.movie.entities.Movie;

import lombok.RequiredArgsConstructor;

/**
 * The implementation of the interface with extra repository methods for movies
 */
@RequiredArgsConstructor
public class CustomMovieRepositoryImpl implements CustomMovieRepository {

    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String RATING_FIELD = "rating";
    private static final String RELEASE_YEAR_FIELD = "releaseYear";
    private static final String UPDATE_TIMESTAMP_FIELD = "updateTimestamp";
    private static final String REVISION_FIELD = "revision";
    private static final String PENDING_LIKES_FIELD = "pendingLikes";
    private static final String PENDING_UNLIKES_FIELD = "pendingUnlikes";
    private static final String UPDATED_FIELD = "updated";

    private final MongoTemplate mongoTemplate;

    /**
     * {@inheritDoc}
     */
    public long updateMovie(final Movie movie, final String currentRevision) {
        final Criteria idCriteria = Criteria.where(ID_FIELD).is(movie.getId());
        final Criteria revisionCriteria = Criteria.where(REVISION_FIELD).is(currentRevision);
        final Criteria finalCriteria = idCriteria.andOperator(revisionCriteria);

        final Query query = new Query(finalCriteria);

        final Update update = new Update();
        update.set(REVISION_FIELD, movie.getRevision());
        update.set(TITLE_FIELD, movie.getTitle());
        update.set(DESCRIPTION_FIELD, movie.getDescription());
        update.set(RATING_FIELD, movie.getRating());
        update.set(RELEASE_YEAR_FIELD, movie.getReleaseYear());
        update.set(UPDATE_TIMESTAMP_FIELD, movie.getUpdateTimestamp());
        update.set(PENDING_LIKES_FIELD, movie.getPendingLikes());
        update.set(PENDING_UNLIKES_FIELD, movie.getPendingUnlikes());
        update.set(UPDATED_FIELD, movie.isUpdated());

        final UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Movie.class);

        return (updateResult != null) ? updateResult.getModifiedCount() : 0;
    }
}
