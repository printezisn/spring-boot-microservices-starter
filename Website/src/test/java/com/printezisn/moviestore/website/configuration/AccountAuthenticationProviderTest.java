package com.printezisn.moviestore.website.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.models.AuthenticatedUser;
import com.printezisn.moviestore.website.account.services.AccountService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

/**
 * Contains unit tests for the AccountAuthenticationProvider class
 */
public class AccountAuthenticationProviderTest {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String EMAIL_ADDRESS = "email";
	
	@Mock
	private AccountService accountService;
	
	@Mock
	private Authentication authentication;
	
	private AccountAuthenticationProvider accountAuthenticationProvider;
	
	/**
	 * Initializes the test class
	 */
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		when(authentication.getName()).thenReturn(USERNAME);
		when(authentication.getCredentials()).thenReturn(PASSWORD);
		
		accountAuthenticationProvider = new AccountAuthenticationProvider(accountService);
	}
	
	/**
	 * Tests the scenario in which the authentication is successful
	 */
	@Test
	public void test_authenticate_success() throws AccountAuthenticationException, AccountNotValidatedException {
		final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
			USERNAME, PASSWORD, EMAIL_ADDRESS, new ArrayList<>());
		
		when(accountService.authenticate(USERNAME, PASSWORD)).thenReturn(authenticatedUser);
		
		final UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)
			accountAuthenticationProvider.authenticate(authentication);
		final AuthenticatedUser result = (AuthenticatedUser) token.getPrincipal();
		
		assertEquals(USERNAME, result.getUsername());
		assertEquals(PASSWORD, result.getPassword());
		assertEquals(EMAIL_ADDRESS, result.getEmailAddress());
	}
	
	/**
	 * Tests the scenario in which the authentication fails
	 */
	@Test
	public void test_authenticate_fail() throws AccountAuthenticationException, AccountNotValidatedException {
		when(accountService.authenticate(USERNAME, PASSWORD)).thenThrow(new AccountNotValidatedException());
		
		final Authentication result = accountAuthenticationProvider.authenticate(authentication);
		
		assertNull(result);
	}
	
	/**
	 * Tests the scenario in which the authentication throws an exception
	 */
	@Test(expected = AccountAuthenticationException.class)
	public void test_authenticate_exception() throws AccountAuthenticationException, AccountNotValidatedException {
		when(accountService.authenticate(USERNAME, PASSWORD)).thenThrow(new AccountAuthenticationException("test"));
		accountAuthenticationProvider.authenticate(authentication);
	}
}
