package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.movieservice.movie.entities.MovieIndex;

import lombok.RequiredArgsConstructor;

import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.search.SearchHit;

/**
 * Implementation of the interface with extra repository methods for indexing
 * movies
 */
@RequiredArgsConstructor
public class CustomMovieIndexRepositoryImpl implements CustomMovieIndexRepository {

    private static final String TITLE_FIELD = "title";
    private static final String DESCRIPTION_FIELD = "description";

    @Value("${elasticsearch.indexName}")
    private final String indexName;

    private final ElasticsearchTemplate elasticsearchTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<MovieIndex> search(final Optional<String> text, final Pageable pageable) {
        final SearchQuery searchQuery;

        if (text.isPresent() && !text.get().isBlank()) {
            searchQuery = new NativeSearchQueryBuilder()
                .withIndices(indexName)
                .withQuery(multiMatchQuery("*" + text.get() + "*", TITLE_FIELD, DESCRIPTION_FIELD)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .operator(Operator.AND)
                    .fuzziness(Fuzziness.TWO))
                .withPageable(pageable)
                .build();
        }
        else {
            searchQuery = new NativeSearchQueryBuilder()
                .withIndices(indexName)
                .withQuery(matchAllQuery())
                .withPageable(pageable)
                .build();
        }

        elasticsearchTemplate.putMapping(MovieIndex.class);

        return elasticsearchTemplate.query(searchQuery, searchResponse -> {
            try {
                final ObjectMapper objectMapper = new ObjectMapper();
                final long totalHits = searchResponse.getHits().getTotalHits();

                final List<MovieIndex> results = new LinkedList<>();
                for (final SearchHit hit : searchResponse.getHits().getHits()) {
                    final MovieIndex searchMovie = objectMapper.readValue(hit.getSourceAsString(),
                        MovieIndex.class);
                    results.add(searchMovie);
                }

                return new PageImpl<MovieIndex>(results, pageable, totalHits);
            }
            catch (final Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
