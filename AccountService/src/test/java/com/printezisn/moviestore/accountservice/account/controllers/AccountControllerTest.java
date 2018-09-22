package com.printezisn.moviestore.accountservice.account.controllers;

import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.any;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.accountservice.account.services.AccountService;
import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;

/**
 * Contains unit tests for the account controller
 */
public class AccountControllerTest {

    private static final String TEST_USERNAME = "test_username";
    private static final String TEST_EMAIL_ADDRESS = "test_email_address@email.com";
    private static final String TEST_PASSWORD = "T3stPA$$";

    @Mock
    private AccountService accountService;

    @Mock
    private MessageSource messageSource;

    private AppUtils appUtils;

    private AccountController accountController;

    private MockMvc mockMvc;

    /**
     * Sets up the prerequisites for the unit tests
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(messageSource.getMessage(anyString(), any(Object[].class), any(Locale.class))).thenReturn("Message");

        appUtils = new AppUtils(messageSource);

        accountController = new AccountController(accountService, appUtils);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    /**
     * Tests the scenario in which the account is not found
     */
    @Test
    public void test_getAccount_notFound() throws Exception {
        when(accountService.getAccount(TEST_USERNAME)).thenReturn(Optional.empty());

        mockMvc.perform(get("/account/get/" + TEST_USERNAME))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the scenario in which the account is found
     */
    @Test
    public void test_getAccount_found() throws Exception {
        final AccountDto accountDto = createAccount();
        when(accountService.getAccount(accountDto.getUsername())).thenReturn(Optional.of(accountDto));

        mockMvc.perform(get("/account/get/" + accountDto.getUsername()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("username").value(TEST_USERNAME))
            .andExpect(jsonPath("emailAddress").value(TEST_EMAIL_ADDRESS));
    }

    /**
     * Tests the scenario in which there are validation errors
     */
    @Test
    public void test_authenticate_validationErrors() throws Exception {
        final AuthDto authDto = new AuthDto();
        authDto.setUsername(TEST_USERNAME);

        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc
            .perform(post("/account/auth/").content(objectMapper.writeValueAsString(authDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which authentication fails
     */
    @Test
    public void test_authenticate_fail() throws Exception {
        final AuthDto authDto = new AuthDto();
        authDto.setUsername(TEST_USERNAME);
        authDto.setPassword(TEST_PASSWORD);

        final ObjectMapper objectMapper = new ObjectMapper();

        when(accountService.getAccount(TEST_USERNAME, TEST_PASSWORD)).thenReturn(Optional.empty());

        mockMvc
            .perform(post("/account/auth/").content(objectMapper.writeValueAsString(authDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which authentication succeeds
     */
    @Test
    public void test_authenticate_success() throws Exception {
        final AuthDto authDto = new AuthDto();
        authDto.setUsername(TEST_USERNAME);
        authDto.setPassword(TEST_PASSWORD);

        final ObjectMapper objectMapper = new ObjectMapper();
        final AccountDto accountDto = createAccount();

        when(accountService.getAccount(TEST_USERNAME, TEST_PASSWORD)).thenReturn(Optional.of(accountDto));

        mockMvc
            .perform(post("/account/auth/").content(objectMapper.writeValueAsString(authDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("result.username").value(TEST_USERNAME))
            .andExpect(jsonPath("result.emailAddress").value(TEST_EMAIL_ADDRESS));
    }

    /**
     * Tests the scenario in which there are validation errors
     */
    @Test
    public void test_createAccount_validationErrors() throws Exception {
        final AccountDto accountDto = new AccountDto();
        final ObjectMapper objectMapper = new ObjectMapper();

        accountDto.setUsername(TEST_USERNAME);

        mockMvc
            .perform(post("/account/new").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which the account is not created
     */
    @Test
    public void test_createAccount_fail() throws Exception {
        final AccountDto accountDto = createAccount();
        final ObjectMapper objectMapper = new ObjectMapper();

        when(accountService.createAccount(accountDto)).thenThrow(new AccountValidationException("Test error."));

        mockMvc
            .perform(post("/account/new").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which the account is created successfully
     */
    @Test
    public void test_createAccount_success() throws Exception {
        final AccountDto accountDto = createAccount();
        final ObjectMapper objectMapper = new ObjectMapper();

        when(accountService.createAccount(accountDto)).thenReturn(accountDto);

        mockMvc
            .perform(post("/account/new").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("result.username").value(TEST_USERNAME))
            .andExpect(jsonPath("result.emailAddress").value(TEST_EMAIL_ADDRESS));
    }

    /**
     * Tests the scenario in which there are validation errors
     */
    @Test
    public void test_updateAccount_validationErrors() throws Exception {
        final AccountDto accountDto = new AccountDto();
        final ObjectMapper objectMapper = new ObjectMapper();

        mockMvc
            .perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    /**
     * Tests the scenario in which the account is not found
     */
    @Test
    public void test_updateAccount_notFound() throws Exception {
        final AccountDto accountDto = createAccount();
        final ObjectMapper objectMapper = new ObjectMapper();

        when(accountService.updateAccount(accountDto)).thenThrow(new AccountNotFoundException());

        mockMvc
            .perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    /**
     * Tests the scenario in which the account is updated successfully
     */
    @Test
    public void test_updateAccount_success() throws Exception {
        final AccountDto accountDto = createAccount();
        final ObjectMapper objectMapper = new ObjectMapper();

        when(accountService.updateAccount(accountDto)).thenReturn(accountDto);

        mockMvc
            .perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("result.username").value(TEST_USERNAME))
            .andExpect(jsonPath("result.emailAddress").value(TEST_EMAIL_ADDRESS));
    }

    /**
     * Tests the scenario in which the account is deleted successfully
     */
    @Test
    public void test_deleteAccount_success() throws Exception {
        final AccountDto accountDto = createAccount();

        mockMvc.perform(get("/account/delete/" + accountDto.getUsername()))
            .andExpect(status().isOk());
    }

    /**
     * Creates and returns a new account
     * 
     * @return The created account
     */
    private AccountDto createAccount() {
        final AccountDto accountDto = new AccountDto();

        accountDto.setUsername(TEST_USERNAME);
        accountDto.setEmailAddress(TEST_EMAIL_ADDRESS);
        accountDto.setPassword(TEST_PASSWORD);

        return accountDto;
    }
}
