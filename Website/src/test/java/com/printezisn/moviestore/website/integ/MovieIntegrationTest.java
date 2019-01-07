package com.printezisn.moviestore.website.integ;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;

/**
 * Contains integration tests for the movie controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class MovieIntegrationTest {

    private static final String TEST_AUTHENTICATED_USER = "test_authenticated_user";
    private static final String TEST_TITLE = "Test title %s";
    private static final String TEST_DESCRIPTION = "Test description %s";
    private static final double TEST_RATING = 8;
    private static final int TEST_RELEASE_YEAR = 2000;

    @Autowired
    private ServiceProperties serviceProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

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
     * Tests if the movie details page is rendered successfully
     */
    @Test
    public void test_getMovie_success() throws Exception {
        final MovieDto movieDto = createNewMovie();

        mockMvc.perform(get("/movie/details/" + movieDto.getId()))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/details"));
    }

    /**
     * Tests if the create movie page is rendered successfully
     */
    @Test
    public void test_createMovie_get_success() throws Exception {
        mockMvc.perform(get("/movie/new")
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/create"));
    }

    /**
     * Tests if the movie is created successfully
     */
    @Test
    public void test_createMovie_post_success() throws Exception {
        final String randomString = UUID.randomUUID().toString();

        mockMvc.perform(post("/movie/new")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("title", String.format(TEST_TITLE, randomString))
            .param("description", String.format(TEST_DESCRIPTION, randomString))
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests if the edit movie page is rendered successfully
     */
    @Test
    public void test_editMovie_get_success() throws Exception {
        final MovieDto movieDto = createNewMovie();

        mockMvc.perform(get("/movie/edit/" + movieDto.getId().toString())
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the movie is updated successfully
     */
    @Test
    public void test_editMovie_post_success() throws Exception {
        final MovieDto movieDto = createNewMovie();

        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("title", movieDto.getTitle())
            .param("description", movieDto.getDescription())
            .param("rating", String.valueOf(movieDto.getRating()))
            .param("releaseYear", String.valueOf(movieDto.getReleaseYear()))
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/movie/details/" + movieDto.getId() + "?returnUrl=/home"));
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test
    public void test_editMovie_post_notFound() throws Exception {
        mockMvc.perform(post("/movie/edit")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", UUID.randomUUID().toString())
            .param("title", TEST_TITLE)
            .param("description", TEST_DESCRIPTION)
            .param("rating", String.valueOf(TEST_RATING))
            .param("releaseYear", String.valueOf(TEST_RELEASE_YEAR)))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/edit"));
    }

    /**
     * Tests if the delete movie page is rendered successfully
     */
    @Test
    public void test_deleteMovie_get_success() throws Exception {
        final MovieDto movieDto = createNewMovie();

        mockMvc.perform(get("/movie/delete/" + movieDto.getId().toString())
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(view().name("movie/delete"));
    }

    /**
     * Tests if the movie is deleted successfully
     */
    @Test
    public void test_deleteMovie_post_success() throws Exception {
        final MovieDto movieDto = createNewMovie();

        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", movieDto.getId().toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        mockMvc.perform(get("/movie/" + movieDto.getId()))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test
    public void test_deleteMovie_post_notFound() throws Exception {
        mockMvc.perform(post("/movie/delete")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("id", UUID.randomUUID().toString())
            .param("returnUrl", "/home"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Creates and returns a new movie
     * 
     * @return The created movie
     */
    private MovieDto createNewMovie() {
        final String randomString = UUID.randomUUID().toString();

        final MovieDto movieDto = new MovieDto();
        movieDto.setTitle(String.format(TEST_TITLE, randomString));
        movieDto.setDescription(String.format(TEST_DESCRIPTION, randomString));
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);
        movieDto.setCreator(TEST_AUTHENTICATED_USER);

        final String url = getMovieServiceActionUrl("/movie/new");

        return restTemplate.postForEntity(url, movieDto, MovieResultModel.class)
            .getBody()
            .getResult();
    }

    /**
     * Returns the URL to a movie service action
     * 
     * @param action
     *            The action
     * @return The action URL
     */
    private String getMovieServiceActionUrl(final String action) {
        return String.format("%s%s", serviceProperties.getMovieServiceUrl(), action);
    }
}
