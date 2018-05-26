package com.printezisn.moviestore.movieservice.movie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

/**
 * Searched movie entity
 */
@Document(indexName = "#{@elasticSearchIndexName}", type = "movies")
@Data
public class SearchedMovie {
	
	@Id
	private String id;
	
	@Field(
		type = FieldType.text,
		index = true,
		store = true
	)
	private String title;
	
	@Field(
		type = FieldType.text,
		index = true,
		store = true
	)
	private String description;

	@Field(
		type = FieldType.Double,
		index = true,
		store = true
	)
	private double rating;
	
	@Field(
		type = FieldType.Integer,
		index = true,
		store = true
	)
	private int releaseYear;

	@Field(
		type = FieldType.Long,
		index = true,
		store = true
	)	
	private long totalLikes;
	
	@Field(
		type = FieldType.text,
		index = false,
		store = true
	)
	private String creator;
}
