package com.printezisn.moviestore.website.integ;

import static org.junit.Assert.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;

/**
 * Contains integration tests for the auth controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class AuthIntegrationTest {

    private static final String TEST_USERNAME = "test_username_%s";
    private static final String TEST_EMAIL_ADDRESS = "test_email_%s@email.com";
    private static final String TEST_PASSWORD = "T3stPA$$";

    @Autowired
    private ServiceProperties serviceProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Tests if the login page is rendered successfully
     */
    @Test
    public void test_login_renderSuccess() throws Exception {
        mockMvc.perform(get("/auth/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }

    /**
     * Tests if the login fails when the credentials are incorrect
     */
    @Test
    public void test_login_authenticationFail() throws Exception {
        mockMvc.perform(formLogin("/auth/login")
            .user("username", "invalid_username")
            .password("password", "invalid_password"))
            .andExpect(unauthenticated());
    }

    /**
     * Tests if the login succeeds when the credentials are correct
     */
    @Test
    public void test_login_success() throws Exception {
        final AccountDto accountDto = createAccount();

        mockMvc.perform(formLogin("/auth/login")
            .user("username", accountDto.getUsername())
            .password("password", TEST_PASSWORD))
            .andExpect(authenticated());
    }

    /**
     * Tests if the logout is successful
     */
    @Test
    public void test_logout_success() throws Exception {
        mockMvc.perform(logout("/auth/logout"))
            .andExpect(unauthenticated());
    }

    /**
     * Creates an account
     * 
     * @return The created account
     */
    private AccountDto createAccount() {
        final String randomString = UUID.randomUUID().toString();
        final AccountDto accountDto = new AccountDto();

        accountDto.setUsername(String.format(TEST_USERNAME, randomString));
        accountDto.setPassword(TEST_PASSWORD);
        accountDto.setEmailAddress(String.format(TEST_EMAIL_ADDRESS, randomString));

        final String url = getAccountServiceActionUrl("/account/new");
        final ResponseEntity<AccountResultModel> response = restTemplate.postForEntity(url, accountDto,
            AccountResultModel.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody().getResult();
    }

    /**
     * Returns the URL to an account service action
     * 
     * @param action
     *            The action
     * @return The action URL
     */
    private String getAccountServiceActionUrl(final String action) {
        return String.format("%s%s", serviceProperties.getAccountServiceUrl(), action);
    }
}
