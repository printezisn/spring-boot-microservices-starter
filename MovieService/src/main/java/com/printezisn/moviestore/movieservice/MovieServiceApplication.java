package com.printezisn.moviestore.movieservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main class of the application
 */
@SpringBootApplication(scanBasePackages = { "com.printezisn.moviestore.movieservice",
    "com.printezisn.moviestore.common" })
@EnableScheduling
@EnableElasticsearchRepositories
public class MovieServiceApplication {

    /**
     * The main method of the application
     * 
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(MovieServiceApplication.class, args);
    }
}
