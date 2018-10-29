package com.printezisn.moviestore.website.movie.controllers;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.movie.services.MovieService;

/**
 * Contains unit tests for the movie controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MovieControllerTest {

    private static final String TEST_AUTHENTICATED_USER = "test_authenticated_user";
    private static final String TEST_TITLE = "Test title";
    private static final String TEST_DESCRIPTION = "Test description";
    private static final double TEST_RATING = 10;
    private static final int TEST_RELEASE_YEAR = 2000;

    private static final String MESSAGE = "Test message.";
    private static final String VALIDATION_ERROR_MESSAGE = "Test error.";

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MovieService movieService;

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
        when(messageSource.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn(MESSAGE);

        movieController = new MovieController(movieService, appUtils);

        mockMvc = MockMvcBuilders
            .standaloneSetup(movieController)
            .apply(springSecurity(springSecurityFilterChain))
            .build();
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

    /**
     * Tests if the create movie page is rendered successfully
     */
    @Test
    public void test_createMovie_get_success() throws Exception {
        mockMvc.perform(get("/movie/new")
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie", "errors", "years"))
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_createMovie_get_unauthorized() throws Exception {
        mockMvc.perform(get("/movie/new"))
            .andExpect(status().is3xxRedirection());
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_createMovie_post_unauthorized() throws Exception {
        mockMvc.perform(post("/movie/new")
            .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    /**
     * Tests if the correct view is returned when there is no model set
     */
    @Test
    public void test_createMovie_post_noModel() throws Exception {
        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests if validation errors are returned when there there are incorrect fields
     */
    @Test
    public void test_createMovie_post_validationErrors() throws Exception {
        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", "test")
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests if validation errors are returned when the service returns errors
     */
    @Test
    public void test_createMovie_post_serviceErrors() throws Exception {
        final MovieResultModel result = new MovieResultModel();
        result.setErrors(Arrays.asList(VALIDATION_ERROR_MESSAGE));

        final MovieDto movieDto = new MovieDto();
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);
        movieDto.setCreator(TEST_AUTHENTICATED_USER);

        when(movieService.createMovie(movieDto)).thenReturn(result);

        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(VALIDATION_ERROR_MESSAGE)))
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests if the correct errors are returned when the service throws an exception
     */
    @Test
    public void test_createMovie_post_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);
        movieDto.setCreator(TEST_AUTHENTICATED_USER);

        when(movieService.createMovie(movieDto)).thenThrow(new RuntimeException());

        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests if the user is redirected when the operation is successful
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_createMovie_post_success() throws Exception {
        final MovieResultModel result = new MovieResultModel();
        result.setErrors(Collections.emptyList());

        final MovieDto movieDto = new MovieDto();
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);
        movieDto.setCreator(TEST_AUTHENTICATED_USER);

        when(movieService.createMovie(movieDto)).thenReturn(result);

        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }
}
