package com.printezisn.moviestore.movieservice.movie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Data;

/**
 * Searched movie entity
 */
@Document(indexName = "#{@elasticSearchIndexName}", type = "movies")
@Data
public class SearchedMovie {
	
	@Id
	private String id;
	
	private String revision;
	
	private String title;
	
	private String description;
	
	private double rating;
	
	private int releaseYear;
	
	private long totalLikes;
	
	private String creationTimestamp;
	
	private String updateTimestamp;
	
	private String creatorId;
}
