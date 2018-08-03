package com.printezisn.moviestore.movieservice.movie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * The movie entity
 */
@Document(collection = "movies")
@Data
public class Movie {

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

    private String creator;
}
