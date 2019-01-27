package com.printezisn.moviestore.movieservice.movie.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

/**
 * Movie index entity
 */
@Document(indexName = "#{@elasticSearchIndexName}", type = "movies", createIndex = true)
@Data
public class MovieIndex {

    @Id
    private String id;

    @Field(type = FieldType.Text, index = true, store = true)
    private String title;

    @Field(type = FieldType.Text, index = true, store = true)
    private String description;

    @Field(type = FieldType.Double, index = true, store = true)
    private double rating;

    @Field(type = FieldType.Integer, index = true, store = true)
    private int releaseYear;

    @Field(type = FieldType.Long, index = true, store = true)
    private long totalLikes;

    @Field(type = FieldType.Text, index = false, store = true)
    private String creator;
}
