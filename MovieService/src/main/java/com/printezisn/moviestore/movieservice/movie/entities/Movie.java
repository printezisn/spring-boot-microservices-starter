package com.printezisn.moviestore.movieservice.movie.entities;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
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

    private long creationTimestamp;

    private long updateTimestamp;

    private String creator;

    private Set<String> pendingLikes;

    private Set<String> pendingUnlikes;

    @Indexed(direction = IndexDirection.DESCENDING)
    private boolean updated;

    private boolean deleted;
}
