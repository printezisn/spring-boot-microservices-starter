package com.printezisn.moviestore.accountservice.integ;

import static org.junit.Assert.assertEquals;

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

import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;


/**
 * Contains integration tests for accounts
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath:application-test.properties")
public class AccountIntegrationTest {

	private static final String TEST_USERNAME = "test_username_%s";
	private static final String TEST_EMAIL_ADDRESS = "test_email_%s@email.com";
	private static final String TEST_PASSWORD = "T3stPA$$";
	
	@LocalServerPort
	private int localServerPort;
	
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	/**
	 * Tests the scenario in which an account is not found
	 */
	@Test
	public void test_getAccount_notFound() {
		final String getUrl = getActionUrl("account/get/" + UUID.randomUUID());
		final ResponseEntity<AccountDto> getResult = testRestTemplate.getForEntity(getUrl, AccountDto.class);
		
		assertEquals(HttpStatus.NOT_FOUND, getResult.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which an account is found
	 */
	@Test
	public void test_getAccount_found() {
		final AccountDto accountDto = createAccount();
		
		final String getUrl = getActionUrl("account/get/" + accountDto.getId());
		
		final AccountDto foundAccountDto = testRestTemplate.getForObject(getUrl, AccountDto.class);
		
		assertEquals(accountDto.getId(), foundAccountDto.getId());
	}
	
	/**
	 * Tests the scenario in which the authentication fails
	 */
	@Test
	public void test_authenticate_fail() {
		final AccountDto accountDto = createAccount();
		
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(accountDto.getUsername());
		authDto.setPassword("1234");
		
		final String authUrl = getActionUrl("account/auth");
		
		final ResponseEntity<AccountResultModel> result = testRestTemplate.postForEntity(authUrl, authDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the authentication succeeds
	 */
	@Test
	public void test_authenticate_success() {
		final AccountDto accountDto = createAccount();
		
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(accountDto.getUsername());
		authDto.setPassword(TEST_PASSWORD);
		
		final String authUrl = getActionUrl("account/auth");
		
		final ResponseEntity<AccountResultModel> result = testRestTemplate.postForEntity(authUrl, authDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.OK, result.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_createAccount_validationErrors() {
		final String createUrl = getActionUrl("account/new");
		final ResponseEntity<AccountResultModel> createResult = testRestTemplate.postForEntity(createUrl, new AccountDto(), AccountResultModel.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, createResult.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which an account is found
	 */
	@Test
	public void test_createAccount_success() {
		createAccount();
	}
	
	/**
	 * Tests the scenario in which there are validation errors
	 */
	@Test
	public void test_updateAccount_validationErrors() {
		final AccountDto accountDto = createAccount();
		
		final String updateUrl = getActionUrl("account/update");
		final ResponseEntity<AccountResultModel> createResult = testRestTemplate.postForEntity(updateUrl, accountDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.BAD_REQUEST, createResult.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the account is not found
	 */
	@Test
	public void test_updateAccount_notFound() {
		final AccountDto accountDto = createAccount();
		accountDto.setPassword(TEST_PASSWORD);
		accountDto.setId(UUID.randomUUID());
		
		final String updateUrl = getActionUrl("account/update");
		final ResponseEntity<AccountResultModel> createResult = testRestTemplate.postForEntity(updateUrl, accountDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.NOT_FOUND, createResult.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the account is updated successfully
	 */
	@Test
	public void test_updateAccount_success() {
		final AccountDto accountDto = createAccount();
		accountDto.setPassword(TEST_PASSWORD);
		
		final String updateUrl = getActionUrl("account/update");
		final ResponseEntity<AccountResultModel> createResult = testRestTemplate.postForEntity(updateUrl, accountDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.OK, createResult.getStatusCode());
	}
	
	/**
	 * Tests the scenario in which the account is deleted successfully
	 */
	@Test
	public void test_deleteAccount_success() {
		final AccountDto accountDto = createAccount();
		final String deleteUrl = getActionUrl("account/delete/" + accountDto.getId());
		final ResponseEntity<?> deleteResult = testRestTemplate.getForEntity(deleteUrl, String.class);
		
		assertEquals(HttpStatus.OK, deleteResult.getStatusCode());
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
		accountDto.setEmailAddress(String.format(TEST_EMAIL_ADDRESS, randomString));
		accountDto.setPassword(TEST_PASSWORD);
		
		final String createUrl = getActionUrl("account/new");
		final ResponseEntity<AccountResultModel> createResult = testRestTemplate.postForEntity(createUrl, accountDto, AccountResultModel.class);
		
		assertEquals(HttpStatus.OK, createResult.getStatusCode());
		
		return createResult.getBody().getResult();
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
