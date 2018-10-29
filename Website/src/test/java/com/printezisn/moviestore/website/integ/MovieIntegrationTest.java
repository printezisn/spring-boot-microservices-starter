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
}
