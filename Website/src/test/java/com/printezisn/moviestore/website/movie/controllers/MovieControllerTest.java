package com.printezisn.moviestore.website.movie.controllers;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

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
import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.movie.exceptions.MovieNotFoundException;
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
        final String url = "/?text=test_text&page=2&sort=rating&asc=true";

        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());

        final MoviePagedResultModel result = MoviePagedResultModel.builder()
            .entries(List.of(movieDto))
            .pageNumber(2)
            .totalPages(5)
            .sortField("rating")
            .build();

        when(movieService.searchMovies("test_text", 2, "rating", true)).thenReturn(result);

        mockMvc.perform(get(url))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/index"))
            .andExpect(model().attribute("entries", hasItem(movieDto)))
            .andExpect(model().attribute("page", result.getPageNumber()))
            .andExpect(model().attribute("totalPages", result.getTotalPages()))
            .andExpect(model().attribute("sortField", result.getSortField()))
            .andExpect(model().attribute("isAscending", result.isAscending()));
    }

    /**
     * Tests if the movie details page is rendered successfully
     */
    @Test
    public void test_getMovie_success() throws Exception {
        final UUID id = UUID.randomUUID();
        final MovieDto movieDto = mock(MovieDto.class);

        when(movieService.getMovie(id)).thenReturn(movieDto);

        mockMvc.perform(get("/movie/details/" + id + "?returnUrl=/home"))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/details"))
            .andExpect(model().attribute("movie", movieDto))
            .andExpect(model().attribute("returnUrl", "/home"));
    }

    /**
     * Tests if the correct result is returned when the movie is not found
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_getMovie_notFound() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieService.getMovie(id)).thenThrow(new MovieNotFoundException());

        mockMvc.perform(get("/movie/details/" + id))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.movieNotFound"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the correct result is returned when the operation throws an
     * exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_getMovie_exception() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieService.getMovie(id)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/movie/details/" + id))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.unexpectedError"), eq(null), any(Locale.class));
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

    /**
     * Tests if the edit movie page is rendered successfully
     */
    @Test
    public void test_editMovie_get_success() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final MovieDto movieDto = mock(MovieDto.class);

        when(movieService.getMovie(movieId)).thenReturn(movieDto);
        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto)).thenReturn(true);

        mockMvc.perform(get("/movie/edit/" + movieId + "?returnUrl=/home")
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(model().attribute("movie", movieDto))
            .andExpect(model().attributeExists("errors", "years"))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_editMovie_get_notFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.getMovie(movieId)).thenThrow(new MovieNotFoundException());

        mockMvc.perform(get("/movie/edit/" + movieId)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests the scenario in which the account is not authorized to edit the movie
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_editMovie_get_notAuthorizedOnMovie() throws Exception {
        final UUID movieId = UUID.randomUUID();
        final MovieDto movieDto = mock(MovieDto.class);

        when(movieService.getMovie(movieId)).thenReturn(movieDto);
        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto)).thenReturn(false);

        mockMvc.perform(get("/movie/edit/" + movieId)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests the scenario in which the service method throws an exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_editMovie_get_exception() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.getMovie(movieId)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/movie/edit/" + movieId)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests that only authorized user access is allowed
     */
    @Test
    public void test_editMovie_get_unauthorized() throws Exception {
        mockMvc.perform(get("/movie/edit/" + UUID.randomUUID()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_editMovie_post_unauthorized() throws Exception {
        mockMvc.perform(post("/movie/edit")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    /**
     * Tests if the correct view is returned when there is no model set
     */
    @Test
    public void test_editMovie_post_noModel() throws Exception {
        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if validation errors are returned when there there are incorrect fields
     */
    @Test
    public void test_editMovie_post_validationErrors() throws Exception {
        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", "test")
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the correct view is returned when the account is not authorized to
     * edit the movie
     */
    @Test
    public void test_editMovie_post_nonAuthorizedAccount() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieId)).thenReturn(false);

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieId.toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if validation errors are returned when the service returns errors
     */
    @Test
    public void test_editMovie_post_serviceErrors() throws Exception {
        final MovieResultModel result = new MovieResultModel();
        result.setErrors(Arrays.asList(VALIDATION_ERROR_MESSAGE));

        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto.getId())).thenReturn(true);
        when(movieService.updateMovie(movieDto)).thenReturn(result);

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(VALIDATION_ERROR_MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the user is redirected if the movie is not found
     */
    @Test
    public void test_editMovie_post_movieNotFound() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto.getId())).thenReturn(true);
        when(movieService.updateMovie(movieDto)).thenThrow(new MovieNotFoundException());

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the correct errors are returned when the service throws an exception
     */
    @Test
    public void test_editMovie_post_exception() throws Exception {
        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto.getId())).thenReturn(true);
        when(movieService.updateMovie(movieDto)).thenThrow(new RuntimeException());

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("movie"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(model().attribute("returnUrl", "/home"))
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the user is redirected when the operation is successful
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_editMovie_post_success() throws Exception {
        final MovieResultModel result = new MovieResultModel();
        result.setErrors(Collections.emptyList());

        final MovieDto movieDto = new MovieDto();
        movieDto.setId(UUID.randomUUID());
        movieDto.setTitle(TEST_TITLE);
        movieDto.setDescription(TEST_DESCRIPTION);
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto.getId())).thenReturn(true);
        when(movieService.updateMovie(movieDto)).thenReturn(result);

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR))
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl(String.format("/movie/details/%s?returnUrl=/home", movieDto.getId())));
    }

    /**
     * Tests if the movie delete page is rendered successfully
     */
    @Test
    public void test_deleteMovie_get_success() throws Exception {
        final UUID id = UUID.randomUUID();
        final MovieDto movieDto = mock(MovieDto.class);

        when(movieService.getMovie(id)).thenReturn(movieDto);
        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto)).thenReturn(true);

        mockMvc.perform(get("/movie/delete/" + id + "?returnUrl=/home")
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/delete"))
            .andExpect(model().attribute("movie", movieDto))
            .andExpect(model().attribute("returnUrl", "/home"));
    }

    /**
     * Tests the scenario in which the account is not authorized to edit the movie
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_get_notAuthorizedOnMovie() throws Exception {
        final UUID id = UUID.randomUUID();
        final MovieDto movieDto = mock(MovieDto.class);

        when(movieService.getMovie(id)).thenReturn(movieDto);
        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieDto)).thenReturn(false);

        mockMvc.perform(get("/movie/delete/" + id)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.movieDelete.notAuthorized"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the correct result is returned when the movie is not found
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_get_notFound() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieService.getMovie(id)).thenThrow(new MovieNotFoundException());

        mockMvc.perform(get("/movie/delete/" + id)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.movieNotFound"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the correct result is returned when the operation throws an
     * exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_get_exception() throws Exception {
        final UUID id = UUID.randomUUID();

        when(movieService.getMovie(id)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/movie/delete/" + id)
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.unexpectedError"), eq(null), any(Locale.class));
    }

    /**
     * Tests that only authorized user access is allowed
     */
    @Test
    public void test_deleteMovie_get_unauthorized() throws Exception {
        mockMvc.perform(get("/movie/delete/" + UUID.randomUUID()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_deleteMovie_post_unauthorized() throws Exception {
        mockMvc.perform(post("/movie/delete")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("http://localhost/auth/login"));
    }

    /**
     * Tests if the correct view is returned when the account is not authorized to
     * delete the movie
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_post_nonAuthorizedAccount() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieId)).thenReturn(false);

        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieId.toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.movieDelete.notAuthorized"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the user is redirected if the movie is not found
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_post_movieNotFound() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieId))
            .thenThrow(new MovieNotFoundException());

        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieId.toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.movieNotFound"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the correct errors are returned when the service throws an exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_post_exception() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieId)).thenReturn(true);
        doThrow(new RuntimeException()).when(movieService).deleteMovie(movieId);

        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieId.toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(messageSource).getMessage(eq("message.error.unexpectedError"), eq(null), any(Locale.class));
    }

    /**
     * Tests if the user is redirected when the operation is successful
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_deleteMovie_post_success() throws Exception {
        final UUID movieId = UUID.randomUUID();

        when(movieService.isAuthorizedOnMovie(TEST_AUTHENTICATED_USER, movieId)).thenReturn(true);

        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieId.toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"))
            .andExpect(flash().attribute("notifications", hasItems()));

        verify(movieService).deleteMovie(movieId);
        verify(messageSource).getMessage(eq("message.deleteMovieSuccess"), eq(null), any(Locale.class));
    }
}
