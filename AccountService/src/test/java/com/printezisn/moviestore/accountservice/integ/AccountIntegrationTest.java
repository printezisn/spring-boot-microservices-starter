package com.printezisn.moviestore.accountservice.integ;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;


/**
 * Contains integration tests for accounts
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@TestPropertySource("classpath:application-test.properties")
public class AccountIntegrationTest {

	private static final String TEST_USERNAME = "test_username_%s";
	private static final String TEST_EMAIL_ADDRESS = "test_email_%s@email.com";
	private static final String TEST_PASSWORD = "T3stPA$$";
	
	@Autowired
	private MockMvc mockMvc;
	
	/**
	 * Tests the scenario in which an account is not found
	 */
	@Test
	public void test_getAccount_notFound() throws Exception {
		mockMvc.perform(get("/account/get/invalid_username"))
			.andExpect(status().isNotFound());
	}
	
	/**
	 * Tests the scenario in which an account is found
	 */
	@Test
	public void test_getAccount_found() throws Exception {
		final AccountDto accountDto = createAccount();
		
		mockMvc.perform(get("/account/get/" + accountDto.getUsername()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("username").value(accountDto.getUsername()));
	}
	
	/**
	 * Tests the scenario in which the authentication fails
	 */
	@Test
	public void test_authenticate_fail() throws Exception {
		final AccountDto accountDto = createAccount();
		
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(accountDto.getUsername());
		authDto.setPassword("1234");
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/auth").content(objectMapper.writeValueAsString(authDto)).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	/**
	 * Tests the scenario in which the authentication succeeds
	 */
	@Test
	public void test_authenticate_success() throws Exception {
		final AccountDto accountDto = createAccount();
		
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(accountDto.getUsername());
		authDto.setPassword(TEST_PASSWORD);
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/auth").content(objectMapper.writeValueAsString(authDto)).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_createAccount_validationErrors() throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/new").content(objectMapper.writeValueAsString(new AccountDto())).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	/**
	 * Tests the scenario in which an account is found
	 */
	@Test
	public void test_createAccount_success() throws Exception {
		createAccount();
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_updateAccount_validationErrors() throws Exception {
		final AccountDto accountDto = createAccount();
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto)).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isBadRequest());
	}
	
	/**
	 * Tests the scenario in which the account is not found
	 */
	@Test
	public void test_updateAccount_notFound() throws Exception {
		final AccountDto accountDto = createAccount();
		accountDto.setUsername("invalid_username");
		accountDto.setPassword(TEST_PASSWORD);
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto)).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNotFound());
	}
	
	/**
	 * Tests the scenario in which the account is updated successfully
	 */
	@Test
	public void test_updateAccount_success() throws Exception {
		final AccountDto accountDto = createAccount();
		accountDto.setPassword(TEST_PASSWORD);
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		
		mockMvc.perform(post("/account/update").content(objectMapper.writeValueAsString(accountDto)).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
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
	 * Creates an account
	 * 
	 * @return The created account
	 */
	private AccountDto createAccount() throws Exception {
		final String randomString = UUID.randomUUID().toString();
		
		final AccountDto accountDto = new AccountDto();
		accountDto.setUsername(String.format(TEST_USERNAME, randomString));
		accountDto.setEmailAddress(String.format(TEST_EMAIL_ADDRESS, randomString));
		accountDto.setPassword(TEST_PASSWORD);
		
		final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
		final String content = objectMapper.writeValueAsString(accountDto);
		
		final String responseString = mockMvc.perform(post("/account/new").content(content).contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		
		return objectMapper.readValue(responseString, AccountResultModel.class).getResult();
	}
}
