package com.printezisn.moviestore.movieservice.movie.dto;

import java.time.Instant;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

/**
 * The data transfer object for the Movie entity
 */
@Data
public class MovieDto {

	@NotNull(message = "message.movie.error.idRequired")
	private UUID id;
	
	@NotEmpty(message = "message.movie.error.titleRequired")
	@Size(max = 250, message = "message.movie.error.titleMaxLength")
	private String title;
	
	@NotEmpty(message = "message.movie.error.descriptionRequired")
	private String description;
	
	@NotNull(message = "message.movie.error.ratingRequired")
	@Max(value = 10, message = "message.movie.error.ratingMaxValue")
	@Min(value = 0, message = "message.movie.error.ratingMinValue")
	private Double rating;
	
	@NotNull(message = "message.movie.error.releaseYearRequired")
	private Integer releaseYear;
	
	private int totalLikes;
	
	private Instant creationTimestamp;
	
	private Instant updateTimestamp;
	
	@NotNull(message = "message.movie.error.creatorIdRequired")
	private UUID creatorId;
}
