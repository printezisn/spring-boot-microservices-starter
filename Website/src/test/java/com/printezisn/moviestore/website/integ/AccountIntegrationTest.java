package com.printezisn.moviestore.website.integ;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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

import com.printezisn.moviestore.common.dto.account.AccountDto;

/**
 * Contains integration tests for the account controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
public class AccountIntegrationTest {
	
	private static final String TEST_USERNAME = "test_username_%s";
	private static final String TEST_EMAIL_ADDRESS = "test_email_%s@email.com";
	private static final String TEST_PASSWORD = "T3stPA$$";
	
	@Autowired
	private MockMvc mockMvc;
	
	/**
	 * Tests if the account registration page is rendered successfully
	 */
	@Test
	public void test_register_get_success() throws Exception {
		mockMvc.perform(get("/account/register"))
			.andExpect(status().isOk())
			.andExpect(view().name("account/register"));
	}
	
	/**
	 * Tests if the account registration is successful
	 */
	@Test
	public void test_register_post_success() throws Exception {
		final String randomString = UUID.randomUUID().toString();
		final AccountDto inputAccountDto = new AccountDto();
		inputAccountDto.setUsername(String.format(TEST_USERNAME, randomString));
		inputAccountDto.setPassword(TEST_PASSWORD);
		inputAccountDto.setEmailAddress(String.format(TEST_EMAIL_ADDRESS, randomString));
		
		mockMvc.perform(post("/account/register")
			.with(csrf())
			.param("username", inputAccountDto.getUsername())
			.param("password", inputAccountDto.getPassword())
			.param("emailAddress", inputAccountDto.getEmailAddress()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/"));
		
		mockMvc.perform(post("/account/register")
			.with(csrf())
			.param("username", inputAccountDto.getUsername())
			.param("password", inputAccountDto.getPassword())
			.param("emailAddress", inputAccountDto.getEmailAddress()))
			.andExpect(status().isOk())
			.andExpect(view().name("account/register"))
			.andExpect(model().attributeExists("errors"));
	}
}
