package com.printezisn.moviestore.website;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main class of the application
 */
@SpringBootApplication(scanBasePackages = { "com.printezisn.moviestore.website", "com.printezisn.moviestore.common" })
public class WebsiteApplication {

    /**
     * The main method of the application
     * 
     * @param args
     *            The command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WebsiteApplication.class, args);
    }
}
