package com.printezisn.moviestore.movieservice.movie.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.movieservice.movie.entities.SearchedMovie;

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
 * Implementation of the interface with extra repository methods for searching movies
 */
@RequiredArgsConstructor
public class CustomMovieSearchRepositoryImpl implements CustomMovieSearchRepository {

	private static final String TITLE_FIELD = "title";
	private static final String DESCRIPTION_FIELD = "description";
	
	private final ElasticsearchTemplate elasticsearchTemplate;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Page<SearchedMovie> search(final Optional<String> text, final Pageable pageable) {
		SearchQuery searchQuery;
		if(text.isPresent()) {
			searchQuery = new NativeSearchQueryBuilder()
				.withQuery(multiMatchQuery("*" + text.get() + "*", TITLE_FIELD, DESCRIPTION_FIELD)
					.type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
					.operator(Operator.AND)
					.fuzziness(Fuzziness.TWO))
				.withPageable(pageable)
				.build();
		}
		else {
			searchQuery = new NativeSearchQueryBuilder()
				.withQuery(matchAllQuery())
				.withPageable(pageable)
				.build();
		}

		return elasticsearchTemplate.query(searchQuery, searchResponse -> {
			try {
				final ObjectMapper objectMapper = new ObjectMapper();
				final long totalHits = searchResponse.getHits().getTotalHits();
				
				final List<SearchedMovie> results = new LinkedList<>();
				for(SearchHit hit : searchResponse.getHits().getHits()) {
					final SearchedMovie searchMovie = objectMapper.readValue(hit.getSourceAsString(), SearchedMovie.class);
					results.add(searchMovie);
				}
				
				return new PageImpl<SearchedMovie>(results, pageable, totalHits);
			}
			catch(final Exception ex) {
				throw new RuntimeException(ex);
			}
		});
	}
}
