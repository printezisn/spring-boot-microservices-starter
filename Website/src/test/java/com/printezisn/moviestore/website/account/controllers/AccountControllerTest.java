package com.printezisn.moviestore.website.account.controllers;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
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

import org.assertj.core.util.Lists;
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
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.exceptions.AccountPersistenceException;
import com.printezisn.moviestore.website.account.models.ChangePasswordModel;
import com.printezisn.moviestore.website.account.services.AccountService;

/**
 * Contains unit tests for the account controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountControllerTest {

    private static final String TEST_USERNAME = "test_username";
    private static final String TEST_PASSWORD = "T3stPA$$";
    private static final String TEST_NEW_PASSWORD = "T3stPA$$2";
    private static final String TEST_EMAIL_ADDRESS = "test_email";
    private static final String VALIDATION_ERROR_MESSAGE = "Test error.";
    private static final String MESSAGE = "Test message.";
    private static final String TEST_AUTHENTICATED_USER = "test_authenticated_user";

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Mock
    private AccountService accountService;

    @Mock
    private MessageSource messageSource;

    private AppUtils appUtils;

    private AccountController accountController;

    private MockMvc mockMvc;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(messageSource.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn(MESSAGE);

        appUtils = new AppUtils(messageSource);

        accountController = new AccountController(accountService, appUtils);

        mockMvc = MockMvcBuilders
            .standaloneSetup(accountController)
            .apply(springSecurity(springSecurityFilterChain))
            .build();
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
        mockMvc.perform(post("/account/register")
            .with(csrf()))
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
            .with(csrf())
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
            .with(csrf())
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
            .with(csrf())
            .param("username", inputAccountDto.getUsername())
            .param("password", inputAccountDto.getPassword())
            .param("emailAddress", inputAccountDto.getEmailAddress()))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests if the change password page is rendered successfully
     */
    @Test
    public void test_changePassword_get_success() throws Exception {
        mockMvc.perform(get("/account/changePassword")
            .with(user(TEST_AUTHENTICATED_USER)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("model"))
            .andExpect(view().name("account/changePassword"));
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_changePassword_get_unauthorized() throws Exception {
        mockMvc.perform(get("/account/changePassword"))
            .andExpect(status().is3xxRedirection());
    }

    /**
     * Tests that only authorized access is allowed
     */
    @Test
    public void test_changePassword_post_unauthorized() throws Exception {
        mockMvc.perform(post("/account/changePassword")
            .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    /**
     * Tests if validation errors are returned when there there are incorrect fields
     */
    @Test
    public void test_changePassword_post_validationErrors() throws Exception {
        mockMvc.perform(post("/account/changePassword")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("currentPassword", TEST_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("model"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("account/changePassword"));
    }

    /**
     * Tests if the correct errors are returned when the service returns errors
     */
    @Test
    public void test_changePassword_post_serviceErrors() throws Exception {
        final AccountResultModel result = new AccountResultModel();
        result.setErrors(Arrays.asList(VALIDATION_ERROR_MESSAGE));

        final ChangePasswordModel changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setCurrentPassword(TEST_PASSWORD);
        changePasswordModel.setNewPassword(TEST_NEW_PASSWORD);

        when(accountService.changePassword(TEST_AUTHENTICATED_USER, changePasswordModel))
            .thenReturn(result);

        mockMvc.perform(post("/account/changePassword")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("currentPassword", TEST_PASSWORD)
            .param("newPassword", TEST_NEW_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("model"))
            .andExpect(model().attribute("errors", hasItem(VALIDATION_ERROR_MESSAGE)))
            .andExpect(view().name("account/changePassword"));
    }

    /**
     * Tests if the correct errors are returned when the account is not
     * authenticated
     */
    @Test
    public void test_changePassword_post_authenticationException() throws Exception {
        final ChangePasswordModel changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setCurrentPassword(TEST_PASSWORD);
        changePasswordModel.setNewPassword(TEST_NEW_PASSWORD);

        when(accountService.changePassword(TEST_AUTHENTICATED_USER, changePasswordModel))
            .thenThrow(new AccountNotValidatedException());

        mockMvc.perform(post("/account/changePassword")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("currentPassword", TEST_PASSWORD)
            .param("newPassword", TEST_NEW_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("model"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("account/changePassword"));
    }

    /**
     * Tests if the correct errors are returned when a general exception is thrown
     */
    @Test
    public void test_changePassword_post_generalException() throws Exception {
        final ChangePasswordModel changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setCurrentPassword(TEST_PASSWORD);
        changePasswordModel.setNewPassword(TEST_NEW_PASSWORD);

        when(accountService.changePassword(TEST_AUTHENTICATED_USER, changePasswordModel))
            .thenThrow(new RuntimeException());

        mockMvc.perform(post("/account/changePassword")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("currentPassword", TEST_PASSWORD)
            .param("newPassword", TEST_NEW_PASSWORD))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("model"))
            .andExpect(model().attribute("errors", hasItem(MESSAGE)))
            .andExpect(view().name("account/changePassword"));
    }

    /**
     * Tests if the user is redirected when the operation is successful
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test_changePassword_post_success() throws Exception {
        final AccountResultModel result = new AccountResultModel();
        result.setResult(new AccountDto());
        result.setErrors(Collections.emptyList());

        final ChangePasswordModel changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setCurrentPassword(TEST_PASSWORD);
        changePasswordModel.setNewPassword(TEST_NEW_PASSWORD);

        when(accountService.changePassword(TEST_AUTHENTICATED_USER, changePasswordModel))
            .thenReturn(result);

        mockMvc.perform(post("/account/changePassword")
            .with(csrf())
            .with(user(TEST_AUTHENTICATED_USER))
            .param("currentPassword", TEST_PASSWORD)
            .param("newPassword", TEST_NEW_PASSWORD))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("notifications", hasItems()))
            .andExpect(redirectedUrl("/"));
    }
}
