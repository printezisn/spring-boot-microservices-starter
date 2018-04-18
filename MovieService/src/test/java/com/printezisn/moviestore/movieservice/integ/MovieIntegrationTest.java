package com.printezisn.moviestore.movieservice.integ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Instant;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.printezisn.moviestore.movieservice.integ.models.MoviePagedResultModel;
import com.printezisn.moviestore.movieservice.integ.models.MovieResultModel;
import com.printezisn.moviestore.movieservice.movie.dto.MovieDto;

/**
 * Contains integration tests for the movie entity
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.properties")
public class MovieIntegrationTest {

	private static final String TEST_TITLE = "Test Title %s";
	private static final String TEST_DESCRIPTION = "Test Description %s";
	private static final double TEST_RATING = 9;
	private static final int TEST_RELEASE_YEAR = 1988;
	private static final int TEST_TOTAL_LIKES = 5;
	private static final UUID TEST_CREATOR_ID = UUID.randomUUID();
	
	@LocalServerPort
	private int localServerPort;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	/**
	 * Tests if movies are searched successfully
	 */
	@Test
	public void test_searchMovies_success() {
		final String text = "test";
		final int pageNumber = 1;
		final String sortField = "title";
		final boolean isAscending = true;
		
		createMovie();
		
		final String params = String.format("?text=%s&page=%d&sort=%s&asc=%s",
			text, pageNumber, sortField, isAscending);
		final String getUrl = getActionUrl("movie/search" + params);
		
		final ResponseEntity<MoviePagedResultModel> result =
			testRestTemplate.getForEntity(getUrl, MoviePagedResultModel.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertFalse(result.getBody().getEntries().isEmpty());
	}
	
	/**
	 * Tests the scenario in which the movie is not found 
	 */
	@Test
	public void test_getMovie_notFound() {
		final UUID id = UUID.randomUUID();
		final String getUrl = getActionUrl("movie/get/" + id);
		
		final ResponseEntity<MovieDto> result = testRestTemplate.getForEntity(getUrl, MovieDto.class);
		
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the movie is found
	 */
	@Test
	public void test_getMovie_found() {
		final MovieDto movieDto = createMovie();
		final String getUrl = getActionUrl("movie/get/" + movieDto.getId());
		
		final ResponseEntity<MovieDto> result = testRestTemplate.getForEntity(getUrl, MovieDto.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(movieDto.getId(), result.getBody().getId());
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_createMovie_validationErrors() {
		final MovieDto movieDto = new MovieDto();
		final String postUrl = getActionUrl("movie/new");
		
		final ResponseEntity<MovieResultModel> result = testRestTemplate.postForEntity(postUrl, movieDto, MovieResultModel.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the movie is created successfully
	 */
	@Test
	public void test_createMovie_success() {
		createMovie();
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_updateMovie_validationErrors() {
		final MovieDto movieDto = new MovieDto();
		final String postUrl = getActionUrl("movie/update");
		
		final ResponseEntity<MovieResultModel> result = testRestTemplate.postForEntity(postUrl, movieDto, MovieResultModel.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the movie is not found
	 */
	@Test
	public void test_updateMovie_notFound() {
		final MovieDto movieDto = createMovie();
		final String postUrl = getActionUrl("movie/update");
		
		movieDto.setId(UUID.randomUUID());
		
		final ResponseEntity<MovieResultModel> result = testRestTemplate.postForEntity(postUrl, movieDto, MovieResultModel.class);
		
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the movie is updated successfully
	 */
	@Test
	public void test_updateMovie_success() {
		final MovieDto movieDto = createMovie();
		final String postUrl = getActionUrl("movie/update");
		
		final ResponseEntity<MovieResultModel> result = testRestTemplate.postForEntity(postUrl, movieDto, MovieResultModel.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(movieDto.getId(), result.getBody().getResult().getId());
	}
	
	/**
	 * Tests the scenario in which the movie is deleted successfully
	 */
	@Test
	public void test_deleteMovie_success() {
		final MovieDto movieDto = createMovie();
		final String getUrl = getActionUrl("movie/delete/" + movieDto.getId());
		
		final ResponseEntity<?> result = testRestTemplate.getForEntity(getUrl, Object.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}
	
	/**
	 * Tests if a movie like is created successfully
	 */
	@Test
	public void test_likeMovie_success() {
		final MovieDto movieDto = createMovie();
		likeMovie(movieDto);
	}
	
	/**
	 * Tests if a movie like is removed successfully
	 */
	@Test
	public void test_unlikeMovie_success() {
		final MovieDto movieDto = createMovie();
		likeMovie(movieDto);
		
		final String getUrl = getActionUrl(
			String.format("movie/unlike/%s/%s", movieDto.getId(), movieDto.getCreatorId()));	
		final ResponseEntity<MovieDto> result = testRestTemplate.getForEntity(getUrl, MovieDto.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(movieDto.getId(), result.getBody().getId());
		assertEquals(0, result.getBody().getTotalLikes());
	}
	
	/**
	 * Creates a movie like
	 * 
	 * @param movieDto The movie to like
	 */
	private void likeMovie(final MovieDto movieDto) {
		final String getUrl = getActionUrl(
			String.format("movie/like/%s/%s", movieDto.getId(), movieDto.getCreatorId()));	
		final ResponseEntity<MovieDto> result = testRestTemplate.getForEntity(getUrl, MovieDto.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		assertEquals(movieDto.getId(), result.getBody().getId());
		assertEquals(1, result.getBody().getTotalLikes());
	}
	
	/**
	 * Creates a new movie
	 * 
	 * @return The created movie
	 */
	private MovieDto createMovie() {
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
		movieDto.setCreatorId(TEST_CREATOR_ID);
		
		final String postUrl = getActionUrl("movie/new");
		final ResponseEntity<MovieResultModel> result = testRestTemplate.postForEntity(postUrl, movieDto, MovieResultModel.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
		
		return result.getBody().getResult();
	}
	
	/**
	 * Returns the URL to an action
	 * 
	 * @param action The action
	 * @return The action URL
	 */
	private String getActionUrl(final String action) {
		return String.format("http://localhost:%d/%s", localServerPort, action);
	}
}
