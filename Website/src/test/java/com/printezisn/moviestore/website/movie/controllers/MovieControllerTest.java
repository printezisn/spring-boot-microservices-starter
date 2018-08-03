package com.printezisn.moviestore.website.movie.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contains unit tests for the movie controller
 */
public class MovieControllerTest {

    private MovieController movieController;

    private MockMvc mockMvc;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        movieController = new MovieController();

        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
    }

    /**
     * Tests if the index page is rendered successfully
     */
    @Test
    public void test_index_success() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/index"));
    }
}
