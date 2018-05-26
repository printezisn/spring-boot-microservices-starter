package com.printezisn.moviestore.website.account.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.models.AuthenticatedUser;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Contains unit tests for the AccountServiceImpl class
 */
public class AccountServiceImplTest {

	private static final String ACCOUNT_SERVICE_URL = "http://localhost";
	private static final String ACCOUNT_AUTH_PATH = "/account/auth";
	private static final String ACCOUNT_GET_PATH = "/account/get/%s";
	
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String EMAIL_ADDRESS = "email";
	
	@Mock
	private ServiceProperties serviceProperties;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private ResponseEntity<AccountResultModel> response;
	
	private AccountServiceImpl accountService;
	
	/**
	 * Initializes the test class
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		accountService = new AccountServiceImpl(serviceProperties, restTemplate);
		
		when(serviceProperties.getAccountServiceUrl()).thenReturn(ACCOUNT_SERVICE_URL);
	}
	
	/**
	 * Tests the scenario in which the authentication is successful
	 */
	@Test
	public void test_authenticate_success() throws AccountAuthenticationException, AccountNotValidatedException {
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(USERNAME);
		authDto.setPassword(PASSWORD);
		
		final AccountResultModel expectedResult = new AccountResultModel();
		expectedResult.setResult(new AccountDto());
		expectedResult.getResult().setUsername(USERNAME);
		expectedResult.getResult().setEmailAddress(EMAIL_ADDRESS);
		
		when(response.getBody()).thenReturn(expectedResult);
		when(restTemplate.postForEntity(ACCOUNT_SERVICE_URL + ACCOUNT_AUTH_PATH, authDto, AccountResultModel.class))
			.thenReturn(response);
		
		final AuthenticatedUser result = (AuthenticatedUser) accountService.authenticate(USERNAME, PASSWORD);
		
		assertEquals(USERNAME, result.getUsername());
		assertEquals(PASSWORD, result.getPassword());
		assertEquals(EMAIL_ADDRESS, result.getEmailAddress());
	}
	
	/**
	 * Tests the scenario in which the authentication fails
	 */
	@Test(expected = AccountNotValidatedException.class)
	public void test_authenticate_fail() throws AccountAuthenticationException, AccountNotValidatedException {
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(USERNAME);
		authDto.setPassword(PASSWORD);
		
		when(restTemplate.postForEntity(ACCOUNT_SERVICE_URL + ACCOUNT_AUTH_PATH, authDto, AccountResultModel.class))
			.thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
		
		accountService.authenticate(USERNAME, PASSWORD);
	}
	
	/**
	 * Tests the scenario in which the authentication throws an exception
	 */
	@Test(expected = AccountAuthenticationException.class)
	public void test_authenticate_exception() throws AccountAuthenticationException, AccountNotValidatedException {
		final AuthDto authDto = new AuthDto();
		authDto.setUsername(USERNAME);
		authDto.setPassword(PASSWORD);
		
		when(restTemplate.postForEntity(ACCOUNT_SERVICE_URL + ACCOUNT_AUTH_PATH, authDto, AccountResultModel.class))
			.thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		accountService.authenticate(USERNAME, PASSWORD);
	}
	
	/**
	 * Tests the scenario in which the account is loaded successfully
	 */
	@Test
	public void test_loadUserByUsername_success() throws AccountAuthenticationException, AccountNotValidatedException {
		final AccountResultModel expectedResult = new AccountResultModel();
		expectedResult.setResult(new AccountDto());
		expectedResult.getResult().setUsername(USERNAME);
		expectedResult.getResult().setEmailAddress(EMAIL_ADDRESS);
		
		final String url = ACCOUNT_SERVICE_URL + String.format(ACCOUNT_GET_PATH, USERNAME);
		
		when(response.getBody()).thenReturn(expectedResult);
		when(restTemplate.getForEntity(url, AccountResultModel.class))
			.thenReturn(response);
		
		final AuthenticatedUser result = (AuthenticatedUser) accountService.loadUserByUsername(USERNAME);
		
		assertEquals(USERNAME, result.getUsername());
		assertEquals(EMAIL_ADDRESS, result.getEmailAddress());
	}
	
	/**
	 * Tests the scenario in which the account loading throws an exception
	 */
	@Test(expected = UsernameNotFoundException.class)
	public void test_loadUserByUsername_exception() throws UsernameNotFoundException {
		final String url = ACCOUNT_SERVICE_URL + String.format(ACCOUNT_GET_PATH, USERNAME);
		
		when(restTemplate.getForEntity(url, AccountResultModel.class))
			.thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		
		accountService.loadUserByUsername(USERNAME);
	}
}
