package com.printezisn.moviestore.website.movie.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.printezisn.moviestore.common.AppUtils;

/**
 * Contains unit tests for the movie controller
 */
public class MovieControllerTest {

    @Mock
    private MessageSource messageSource;

    private AppUtils appUtils;

    private MovieController movieController;

    private MockMvc mockMvc;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        appUtils = new AppUtils(messageSource);

        movieController = new MovieController(appUtils);

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
