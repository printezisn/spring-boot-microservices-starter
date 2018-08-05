package com.printezisn.moviestore.website.account.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Locale;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountPersistenceException;
import com.printezisn.moviestore.website.account.services.AccountService;

/**
 * Contains unit tests for the account controller
 */
public class AccountControllerTest {

    private static final String TEST_USERNAME = "test_username";
    private static final String TEST_PASSWORD = "test_password";
    private static final String TEST_EMAIL_ADDRESS = "test_email";
    private static final String VALIDATION_ERROR_MESSAGE = "Test error.";
    private static final String MESSAGE = "Test message.";

    @Mock
    private AccountService accountService;

    @Mock
    private MessageSource messageSource;

    private AccountController accountController;

    private MockMvc mockMvc;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(messageSource.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn(MESSAGE);

        accountController = new AccountController(accountService, messageSource);

        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    /**
     * Tests if the register page is rendered successfully
     */
    @Test
    public void test_register_get_success() throws Exception {
        mockMvc.perform(get("/account/register"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(view().name("account/register"));
    }

    /**
     * Tests if the correct view is returned when there is no model set
     */
    @Test
    public void test_register_post_noModel() throws Exception {
        mockMvc.perform(post("/account/register"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(view().name("account/register"));
    }

    /**
     * Tests if the correct view is returned when there is a validation error
     */
    @Test
    public void test_register_post_validationErrors() throws Exception {
        final AccountResultModel result = new AccountResultModel();
        result.setResult(new AccountDto());
        result.setErrors(Arrays.asList(VALIDATION_ERROR_MESSAGE));

        final AccountDto inputAccountDto = new AccountDto();
        inputAccountDto.setUsername(TEST_USERNAME);
        inputAccountDto.setPassword(TEST_PASSWORD);
        inputAccountDto.setEmailAddress(TEST_EMAIL_ADDRESS);

        when(accountService.createAccount(inputAccountDto)).thenReturn(result);

        mockMvc.perform(post("/account/register")
            .param("username", inputAccountDto.getUsername())
            .param("password", inputAccountDto.getPassword())
            .param("emailAddress", inputAccountDto.getEmailAddress()))
            .andExpect(status().isOk())
            .andExpect(view().name("account/register"))
            .andExpect(model().attribute("account", inputAccountDto))
            .andExpect(model().attribute("errors", hasItem(VALIDATION_ERROR_MESSAGE)));
    }

    /**
     * Tests if the correct view is returned when there is an exception
     */
    @Test
    public void test_register_post_exception() throws Exception {
        final AccountDto inputAccountDto = new AccountDto();
        inputAccountDto.setUsername(TEST_USERNAME);
        inputAccountDto.setPassword(TEST_PASSWORD);
        inputAccountDto.setEmailAddress(TEST_EMAIL_ADDRESS);

        when(accountService.createAccount(inputAccountDto))
            .thenThrow(AccountPersistenceException.class);

        mockMvc.perform(post("/account/register")
            .param("username", inputAccountDto.getUsername())
            .param("password", inputAccountDto.getPassword())
            .param("emailAddress", inputAccountDto.getEmailAddress()))
            .andExpect(status().isOk())
            .andExpect(view().name("account/register"))
            .andExpect(model().attribute("account", inputAccountDto))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)));
    }

    /**
     * Tests if the correct page is shown when the operation is successful
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_register_post_success() throws Exception {
        final AccountResultModel result = new AccountResultModel();
        result.setResult(new AccountDto());
        result.setErrors(Lists.emptyList());

        final AccountDto inputAccountDto = new AccountDto();
        inputAccountDto.setUsername(TEST_USERNAME);
        inputAccountDto.setPassword(TEST_PASSWORD);
        inputAccountDto.setEmailAddress(TEST_EMAIL_ADDRESS);

        when(accountService.createAccount(inputAccountDto)).thenReturn(result);

        mockMvc.perform(post("/account/register")
            .param("username", inputAccountDto.getUsername())
            .param("password", inputAccountDto.getPassword())
            .param("emailAddress", inputAccountDto.getEmailAddress()))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }
}
