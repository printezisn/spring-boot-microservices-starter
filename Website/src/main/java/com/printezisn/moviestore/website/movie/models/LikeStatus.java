package com.printezisn.moviestore.website.movie.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class used to represent the like status of a movie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatus {
    private int totalLikes;
    private boolean hasLiked;
}
