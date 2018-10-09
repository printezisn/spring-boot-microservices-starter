package com.printezisn.moviestore.movieservice.integ;

import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.common.RetryHandler;
import com.printezisn.moviestore.common.dto.movie.MovieDto;

/**
 * Contains integration tests for the movie entity
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class MovieIntegrationTest {

    private static final String TEST_TITLE = "Test Title %s";
    private static final String TEST_DESCRIPTION = "Test Description %s";
    private static final double TEST_RATING = 9;
    private static final int TEST_RELEASE_YEAR = 1988;
    private static final int TEST_TOTAL_LIKES = 5;
    private static final String TEST_CREATOR = "test_creator_%s";

    private final RetryHandler retryHandler = RetryHandler.builder()
        .useExponentialBackOff(true)
        .jitter(500)
        .build();

    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests if movies are searched successfully
     */
    @Test
    public void test_searchMovies_success() throws Exception {
        final String text = "test";
        final int pageNumber = 0;
        final String sortField = "rating";
        final boolean isAscending = true;

        createMovie();

        final String url = String.format("/movie/search?text=%s&page=%d&sort=%s&asc=%s",
            text, pageNumber, sortField, isAscending);

        retryHandler.run(() -> {
            final String responseString = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

            final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            final MoviePagedResultModel result = objectMapper.readValue(responseString, MoviePagedResultModel.class);

            assertFalse(result.getEntries().isEmpty());
            return result;
        }, ex -> true);
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test
    public void test_getMovie_notFound() throws Exception {
        mockMvc.perform(get("/movie/get/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the scenario in which the movie is found
     */
    @Test
    public void test_getMovie_found() throws Exception {
        final MovieDto movieDto = createMovie();

        mockMvc.perform(get("/movie/get/" + movieDto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id").value(movieDto.getId().toString()));
    }

    /**
     * Tests the scenario in which there are validation errors
     */
    @Test
    public void test_createMovie_validationErrors() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        final String content = objectMapper.writeValueAsString(movieDto);
        mockMvc.perform(post("/movie/new").content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which the movie is created successfully
     */
    @Test
    public void test_createMovie_success() throws Exception {
        createMovie();
    }

    /**
     * Tests the scenario in which there are validation errors
     */
    @Test
    public void test_updateMovie_validationErrors() throws Exception {
        final MovieDto movieDto = new MovieDto();
        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        final String content = objectMapper.writeValueAsString(movieDto);
        mockMvc.perform(post("/movie/update").content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which the movie is not found
     */
    @Test
    public void test_updateMovie_notFound() throws Exception {
        final MovieDto movieDto = createMovie();
        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        movieDto.setId(UUID.randomUUID());

        final String content = objectMapper.writeValueAsString(movieDto);
        mockMvc.perform(post("/movie/update").content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the scenario in which the movie is updated successfully
     */
    @Test
    public void test_updateMovie_success() throws Exception {
        final MovieDto movieDto = createMovie();
        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        final String content = objectMapper.writeValueAsString(movieDto);
        retryHandler.run(() -> {
            return mockMvc.perform(post("/movie/update").content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("result.id").value(movieDto.getId().toString()));
        }, ex -> true);
    }

    /**
     * Tests the scenario in which the movie is deleted successfully
     */
    @Test
    public void test_deleteMovie_success() throws Exception {
        final MovieDto movieDto = createMovie();

        retryHandler.run(() -> {
            return mockMvc.perform(get("/movie/delete/" + movieDto.getId()))
                .andExpect(status().isOk());
        }, ex -> true);
    }

    /**
     * Tests if a movie like is created successfully
     */
    @Test
    public void test_likeMovie_success() throws Exception {
        final MovieDto movieDto = createMovie();
        likeMovie(movieDto);

        mockMvc
            .perform(get(String.format("/movie/hasliked/%s/%s", movieDto.getId().toString(), movieDto.getCreator())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    /**
     * Tests if a movie like is removed successfully
     */
    @Test
    public void test_unlikeMovie_success() throws Exception {
        final MovieDto movieDto = createMovie();
        likeMovie(movieDto);

        retryHandler.run(() -> {
            return mockMvc
                .perform(get(String.format("/movie/unlike/%s/%s", movieDto.getId().toString(), movieDto.getCreator())))
                .andExpect(status().isOk());
        }, ex -> true);

        mockMvc
            .perform(get(String.format("/movie/hasliked/%s/%s", movieDto.getId().toString(), movieDto.getCreator())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(false));
    }

    /**
     * Creates a movie like
     * 
     * @param movieDto
     *            The movie to like
     */
    private void likeMovie(final MovieDto movieDto) throws Exception {
        retryHandler.run(() -> {
            return mockMvc.perform(get(String.format("/movie/like/%s/%s", movieDto.getId(), movieDto.getCreator())))
                .andExpect(status().isOk());
        }, ex -> true);
    }

    /**
     * Creates a new movie
     * 
     * @return The created movie
     */
    private MovieDto createMovie() throws Exception {
        final String randomString = UUID.randomUUID().toString();
        final MovieDto movieDto = new MovieDto();

        movieDto.setId(UUID.randomUUID());
        movieDto.setTitle(String.format(TEST_TITLE, randomString));
        movieDto.setDescription(String.format(TEST_DESCRIPTION, randomString));
        movieDto.setRating(TEST_RATING);
        movieDto.setReleaseYear(TEST_RELEASE_YEAR);
        movieDto.setTotalLikes(TEST_TOTAL_LIKES);
        movieDto.setCreationTimestamp(Instant.now());
        movieDto.setUpdateTimestamp(Instant.now());
        movieDto.setCreator(String.format(TEST_CREATOR, randomString));

        final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        final String content = objectMapper.writeValueAsString(movieDto);

        final String responseString = mockMvc
            .perform(post("/movie/new").content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return objectMapper.readValue(responseString, MovieResultModel.class)
            .getResult();
    }
}
