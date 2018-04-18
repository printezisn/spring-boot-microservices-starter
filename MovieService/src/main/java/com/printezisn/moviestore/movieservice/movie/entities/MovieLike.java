package com.printezisn.moviestore.movieservice.movie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * The MovieLike entity
 */
@Document(collection = "movielikes")
@Data
public class MovieLike {

	@Id
	private String id;
	
	private String userId;
	
	private String movieId;
}
