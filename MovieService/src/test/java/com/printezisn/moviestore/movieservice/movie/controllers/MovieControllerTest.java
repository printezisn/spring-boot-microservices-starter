package com.printezisn.moviestore.movieservice.movie.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.common.models.PagedResult;
import com.printezisn.moviestore.movieservice.movie.controllers.MovieController;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.movieservice.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.movieservice.movie.services.MovieService;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Contains unit tests for the movie controller
 */
public class MovieControllerTest {

	private static final String TEST_TITLE = "Test Title";
	private static final String TEST_DESCRIPTION = "Test Description";
	private static final double TEST_RATING = 9;
	private static final int TEST_RELEASE_YEAR = 1988;
	private static final int TEST_TOTAL_LIKES = 5;
	private static final String TEST_CREATOR = "test_creator";
	
	@Mock
	private MovieService movieService;
	
	@Mock
	private MessageSource messageSource;
	
	private MovieController movieController;
	
	private MockMvc mockMvc;
	
	/**
	 * Initializes the test class
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenReturn("Message");
		
		movieController = new MovieController(movieService, messageSource);
		
		mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();
	}
	
	/**
	 * Tests if movies are searched successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_searchMovies_success() throws Exception {
		final Optional<String> text = Optional.of("test");
		final Optional<Integer> pageNumber = Optional.of(1);
		final Optional<String> sortField = Optional.of("sort");
		final boolean isAscending = true;
		final int totalPages = 5;
		final String url = String.format("/movie/search?text=%s&page=%d&sort=%s&asc=%s", text.get(),
			pageNumber.get(), sortField.get(), isAscending);
		
		final MovieDto movieDto = createMovie();
		final PagedResult<MovieDto> pagedResult = new PagedResult<>(Arrays.asList(movieDto),
			pageNumber.get(), totalPages, sortField.get(), isAscending);
		
		when(movieService.searchMovies(text, pageNumber, sortField, isAscending)).thenReturn(pagedResult);
		
		final ResultActions resultActions = mockMvc.perform(get(url))
			.andExpect(status().isOk())
			.andExpect(jsonPath("pageNumber").value(pageNumber.get()))
			.andExpect(jsonPath("totalPages").value(totalPages))
			.andExpect(jsonPath("sortField").value(sortField.get()))
			.andExpect(jsonPath("ascending").value(isAscending))
			.andExpect(jsonPath("entries").isArray())
			.andExpect(jsonPath("entries[0]").exists())
			.andExpect(jsonPath("entries[1]").doesNotExist());
		expectMovieValues(resultActions, movieDto.getId(), Optional.of("entries[0]"));
	}
	
	/**
	 * Tests the scenario in which the movie is not found
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getMovie_notFound() throws Exception {
		final UUID id = UUID.randomUUID();
		
		when(movieService.getMovie(id)).thenThrow(new MovieNotFoundException());
		
		mockMvc.perform(get("/movie/get/" + id)).andExpect(status().isNotFound());
	}
	
	/**
	 * Tests the scenario in which the movie is found
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_getMovie_found() throws Exception {
		final MovieDto movieDto = createMovie();
		
		when(movieService.getMovie(movieDto.getId())).thenReturn(movieDto);
		
		final ResultActions resultActions = mockMvc.perform(get("/movie/get/" + movieDto.getId()))
			.andExpect(status().isOk());
		expectMovieValues(resultActions, movieDto.getId(), Optional.empty());
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_createMovie_validationErrors() throws Exception {
		final MovieDto movieDto = new MovieDto();
		final ObjectMapper objectMapper = new ObjectMapper();
		
		movieDto.setTitle(TEST_TITLE);
		
		final String requestContent = objectMapper.writeValueAsString(movieDto);
		
		mockMvc.perform(post("/movie/new")
			.content(requestContent)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	/**
	 * Tests the scenario in which the movie is created successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_createMovie_success() throws Exception {
		final MovieDto movieDto = createMovie();
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		when(movieService.createMovie(movieDto)).thenReturn(movieDto);
		
		final String requestContent = objectMapper.writeValueAsString(movieDto);
		
		final ResultActions resultActions = mockMvc.perform(post("/movie/new")
			.content(requestContent)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
		expectMovieValues(resultActions, movieDto.getId(), Optional.of("result"));			
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_updatedMovie_validationErrors() throws Exception {
		final MovieDto movieDto = new MovieDto();
		final ObjectMapper objectMapper = new ObjectMapper();
		
		movieDto.setTitle(TEST_TITLE);
		
		final String requestContent = objectMapper.writeValueAsString(movieDto);
		
		mockMvc.perform(post("/movie/update")
			.content(requestContent)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	/**
	 * Tests the scenario in which the movie is not found
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_updateMovie_notFound() throws Exception {
		final MovieDto movieDto = createMovie();
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		when(movieService.updateMovie(movieDto)).thenThrow(new MovieNotFoundException());
		
		final String requestContent = objectMapper.writeValueAsString(movieDto);
		
		mockMvc.perform(post("/movie/update")
			.content(requestContent)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());		
	}
	
	/**
	 * Tests the scenario in which the movie is updated successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_updateMovie_success() throws Exception {
		final MovieDto movieDto = createMovie();
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		when(movieService.updateMovie(movieDto)).thenReturn(movieDto);
		
		final String requestContent = objectMapper.writeValueAsString(movieDto);
		
		final ResultActions resultActions = mockMvc.perform(post("/movie/update")
			.content(requestContent)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
		expectMovieValues(resultActions, movieDto.getId(), Optional.of("result"));			
	}
	
	/**
	 * Tests the scenario in which the movie is deleted successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_deleteMovie_success() throws Exception {
		final UUID id = UUID.randomUUID();
		
		mockMvc.perform(get("/movie/delete/" + id))
			.andExpect(status().isOk());
		
		verify(movieService).deleteMovie(id);
	}
	
	/**
	 * Tests the scenario in which the movie is not found
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_likeMovie_notFound() throws Exception {
		final UUID movieId = UUID.randomUUID();
		final String user = "test_user";
		final String url = String.format("/movie/like/%s/%s", movieId, user);
		
		when(movieService.likeMovie(movieId, user)).thenThrow(new MovieNotFoundException());
		
		mockMvc.perform(get(url)).andExpect(status().isNotFound());
	}
	
	/**
	 * Tests if the movie like is added successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_likeMovie_success() throws Exception {
		final MovieDto movieDto = createMovie();
		final String user = "test_user";
		final String url = String.format("/movie/like/%s/%s", movieDto.getId(), user);
		
		when(movieService.likeMovie(movieDto.getId(), user)).thenReturn(movieDto);
		
		final ResultActions resultActions = mockMvc.perform(get(url))
			.andExpect(status().isOk());
		expectMovieValues(resultActions, movieDto.getId(), Optional.empty());
	}
	
	/**
	 * Tests the scenario in which the movie is not found
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_unlikeMovie_notFound() throws Exception {
		final UUID movieId = UUID.randomUUID();
		final String user = "test_user";
		final String url = String.format("/movie/unlike/%s/%s", movieId, user);
		
		when(movieService.unlikeMovie(movieId, user)).thenThrow(new MovieNotFoundException());
		
		mockMvc.perform(get(url)).andExpect(status().isNotFound());
	}
	
	/**
	 * Tests if the movie like is removed successfully
	 * 
	 * @throws Exception
	 */
	@Test
	public void test_unlikeMovie_success() throws Exception {
		final MovieDto movieDto = createMovie();
		final String user = "test_user";
		final String url = String.format("/movie/unlike/%s/%s", movieDto.getId(), user);
		
		when(movieService.unlikeMovie(movieDto.getId(), user)).thenReturn(movieDto);
		
		final ResultActions resultActions = mockMvc.perform(get(url))
			.andExpect(status().isOk());
		expectMovieValues(resultActions, movieDto.getId(), Optional.empty());
	}
	
	/**
	 * Expects the returned values of a movie 
	 * 
	 * @param resultActions The expectation result actions
	 * @param id The id of the movie
	 * @param parentPropertyPath The path to the parent property of the values (Optional)
	 * @return The new expectation result actions
	 * @throws Exception
	 */
	private ResultActions expectMovieValues(final ResultActions resultActions, final UUID id, final Optional<String> parentPropertyPath)
		throws Exception {
		
		return resultActions
			.andExpect(status().isOk())
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "id")).value(id.toString()))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "title")).value(TEST_TITLE))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "description")).value(TEST_DESCRIPTION))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "rating")).value(TEST_RATING))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "releaseYear")).value(TEST_RELEASE_YEAR))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "totalLikes")).value(TEST_TOTAL_LIKES))
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "creationTimestamp")).exists())
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "updateTimestamp")).exists())
			.andExpect(jsonPath(getPropertyPath(parentPropertyPath, "creator")).value(TEST_CREATOR));
	}
	
	/**
	 * Returns the path to a property
	 * 
	 * @param parentPropertyPath The path to the parent property
	 * @param propertyName The name of the property
	 * @return The path to the property
	 */
	private String getPropertyPath(final Optional<String> parentPropertyPath, final String propertyName) {
		return parentPropertyPath.isPresent()
			? String.format("%s.%s", parentPropertyPath.get(), propertyName)
			: propertyName;
	}
	
	/**
	 * Creates a MovieDto object
	 * 
	 * @return The created MovieDto object
	 */
	private MovieDto createMovie() {
		final MovieDto movieDto = new MovieDto();
		
		movieDto.setId(UUID.randomUUID());
		movieDto.setTitle(TEST_TITLE);
		movieDto.setDescription(TEST_DESCRIPTION);
		movieDto.setRating(TEST_RATING);
		movieDto.setReleaseYear(TEST_RELEASE_YEAR);
		movieDto.setTotalLikes(TEST_TOTAL_LIKES);
		movieDto.setCreationTimestamp(Instant.now());
		movieDto.setUpdateTimestamp(Instant.now());
		movieDto.setCreator(TEST_CREATOR);
		
		return movieDto;
	}
}
