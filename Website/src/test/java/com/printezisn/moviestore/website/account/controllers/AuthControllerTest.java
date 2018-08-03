package com.printezisn.moviestore.website.account.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Contains unit tests for the auth controller
 */
public class AuthControllerTest {

    private AuthController authController;

    private MockMvc mockMvc;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        authController = new AuthController();

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    /**
     * Tests if the login page is rendered successfully
     */
    @Test
    public void test_login_success() throws Exception {
        mockMvc.perform(get("/auth/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }
}
