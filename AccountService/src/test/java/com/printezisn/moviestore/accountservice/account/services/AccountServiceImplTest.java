package com.printezisn.moviestore.accountservice.account.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.printezisn.moviestore.accountservice.account.dto.AccountDto;
import com.printezisn.moviestore.accountservice.account.entities.Account;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.accountservice.account.mappers.AccountMapper;
import com.printezisn.moviestore.accountservice.account.repositories.AccountRepository;

/**
 * Contains unit tests for the AccountServiceImpl class
 */
public class AccountServiceImplTest {

	private static final String TEST_USERNAME = "test_username";
	private static final String TEST_EMAIL_ADDRESS = "test_email_address@email.com";
	private static final String TEST_PASSWORD = "1234";
	
	@Mock
	private AccountRepository accountRepository;
	
	@Mock
	private AccountMapper accountMapper;
	
	@Mock
	private MessageSource messageSource;
	
	private AccountServiceImpl accountService;
	
	/**
	 * Sets up the prerequisites for the unit tests
	 */
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(Object[].class), Mockito.any(Locale.class))).thenReturn("Message");
		
		accountService = new AccountServiceImpl(accountRepository, accountMapper, messageSource);
	}
	
	/**
	 * Tests the scenario in which the account is found, when an account id is provided
	 */
	@Test
	public void test_getAccount_withId_found() throws AccountException {
		final UUID id = UUID.randomUUID();
		final Account account = new Account();
		account.setId(id.toString());
		
		final AccountDto accountDto = new AccountDto();
		
		Mockito.when(accountRepository.findById(id.toString())).thenReturn(Optional.of(account));
		Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
		
		final Optional<AccountDto> result = accountService.getAccount(id);
		
		assertTrue(result.isPresent());
		assertEquals(accountDto, result.get());
	}
	
	/**
	 * Tests the scenario in which the account is not found, when an account id is provided
	 */
	@Test
	public void test_getAccount_withId_notFound() throws AccountException {
		final UUID id = UUID.randomUUID();
		
		Mockito.when(accountRepository.findById(id.toString())).thenReturn(Optional.empty());
		
		final Optional<AccountDto> result = accountService.getAccount(id);
		
		assertFalse(result.isPresent());
	}
	
	/**
	 * Tests the scenario in which the account is found, when a username and password are provided
	 */
	@Test
	public void test_getAccount_withAuth_found() throws AccountException {
		final Account account = new Account();
		account.setUsername(TEST_USERNAME);
		account.setPasswordSalt(BCrypt.gensalt());
		account.setPassword(BCrypt.hashpw(TEST_PASSWORD, account.getPasswordSalt()));
		
		final AccountDto accountDto = new AccountDto();
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(account));
		Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
		
		final Optional<AccountDto> result = accountService.getAccount(TEST_USERNAME, TEST_PASSWORD);
		
		assertTrue(result.isPresent());
		assertEquals(accountDto, result.get());
	}
	
	/**
	 * Tests the scenario in which the account is not found, when a username and password are provided
	 */
	@Test
	public void test_getAccount_withAuth_notFound() throws AccountException {
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
		
		final Optional<AccountDto> result = accountService.getAccount(TEST_USERNAME, TEST_PASSWORD);
		
		assertFalse(result.isPresent());
	}
	
	/**
	 * Tests the scenario in which the account is not found because an invalid password was provided
	 */
	@Test
	public void test_getAccount_withAuth_invalidPassword() throws AccountException {
		final Account account = new Account();
		account.setUsername(TEST_USERNAME);
		account.setPasswordSalt(BCrypt.gensalt());
		account.setPassword(BCrypt.hashpw(TEST_PASSWORD, account.getPasswordSalt()));
		
		final AccountDto accountDto = new AccountDto();
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(account));
		Mockito.when(accountMapper.accountToAccountDto(account)).thenReturn(accountDto);
		
		final Optional<AccountDto> result = accountService.getAccount(TEST_USERNAME, "12345");
		
		assertFalse(result.isPresent());
	}
	
	/**
	 * Tests the scenario in which the username already exists
	 */
	@Test
	public void test_createAccount_usernameExists() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setUsername(TEST_USERNAME);
		accountDto.setEmailAddress(TEST_EMAIL_ADDRESS);
		accountDto.setPassword(TEST_PASSWORD);
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(new Account()));
		
		try {
			accountService.createAccount(accountDto);
			
			fail();
		}
		catch(final AccountValidationException ex) {
			
		}
	}
	
	/**
	 * Tests the scenario in which the email address already exists
	 */
	@Test
	public void test_createAccount_emailAddressExists() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setUsername(TEST_USERNAME);
		accountDto.setEmailAddress(TEST_EMAIL_ADDRESS);
		accountDto.setPassword(TEST_PASSWORD);
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
		Mockito.when(accountRepository.findByEmailAddress(TEST_EMAIL_ADDRESS)).thenReturn(Optional.of(new Account()));
		
		try {
			accountService.createAccount(accountDto);
			
			fail();
		}
		catch(final AccountValidationException ex) {
			
		}
	}
	
	/**
	 * Tests the scenario in which the account is created successfully
	 */
	@Test
	public void test_createAccount_successfulSave() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setUsername(TEST_USERNAME);
		accountDto.setEmailAddress(TEST_EMAIL_ADDRESS);
		accountDto.setPassword(TEST_PASSWORD);
		
		final Account account = new Account();
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
		Mockito.when(accountRepository.findByEmailAddress(TEST_EMAIL_ADDRESS)).thenReturn(Optional.empty());
		Mockito.when(accountMapper.accountDtoToAccount(accountDto)).thenReturn(account);
		
		accountService.createAccount(accountDto);
		
		Mockito.verify(accountRepository).save(account);
		
		assertNotNull(accountDto.getId());
		assertNotNull(accountDto.getPasswordSalt());
		assertNotNull(accountDto.getCreationTimestamp());
		assertNotNull(accountDto.getUpdateTimestamp());
	}
	
	/**
	 * Tests the scenario in which the account repository throws a DuplicateKeyException
	 */
	@Test
	public void test_createAccount_duplicateKeyException() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setUsername(TEST_USERNAME);
		accountDto.setEmailAddress(TEST_EMAIL_ADDRESS);
		accountDto.setPassword(TEST_PASSWORD);
		
		final Account account = new Account();
		
		Mockito.when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
		Mockito.when(accountRepository.findByEmailAddress(TEST_EMAIL_ADDRESS)).thenReturn(Optional.empty());
		Mockito.when(accountMapper.accountDtoToAccount(accountDto)).thenReturn(account);
		Mockito.when(accountRepository.save(account)).thenThrow(DuplicateKeyException.class);
		
		try {
			accountService.createAccount(accountDto);
			
			fail();
		}
		catch(final AccountValidationException ex) {
			
		}
	}
	
	/**
	 * Tests the scenario in which the account is not found
	 */
	@Test
	public void test_updateAccount_accountNotFound() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setId(UUID.randomUUID());
		
		Mockito.when(accountRepository.findById(accountDto.getId().toString())).thenReturn(Optional.empty());
		
		try {
			accountService.updateAccount(accountDto);
			
			fail();
		}
		catch(final AccountNotFoundException ex) {
			
		}
	}
	
	/**
	 * Tests the scenario in which the account is updated successfully
	 */
	@Test
	public void test_updateAccount_successfulSave() throws AccountException {
		final AccountDto accountDto = new AccountDto();
		accountDto.setId(UUID.randomUUID());
		accountDto.setPassword(TEST_PASSWORD);
		
		final Account account = Mockito.mock(Account.class);
		
		Mockito.when(accountRepository.findById(accountDto.getId().toString())).thenReturn(Optional.of(account));
		Mockito.when(account.getPasswordSalt()).thenReturn(BCrypt.gensalt());
		
		accountService.updateAccount(accountDto);
		
		Mockito.verify(accountRepository).save(account);
		Mockito.verify(account, Mockito.never()).setId(Mockito.anyString());
		Mockito.verify(account, Mockito.never()).setUsername(Mockito.anyString());
		Mockito.verify(account, Mockito.never()).setEmailAddress(Mockito.anyString());
		Mockito.verify(account).setPassword(Mockito.anyString());
		Mockito.verify(account).setPasswordSalt(Mockito.anyString());
		Mockito.verify(account, Mockito.never()).setCreationTimestamp(Mockito.anyString());
		Mockito.verify(account).setUpdateTimestamp(Mockito.anyString());
	}
	
	/**
	 * Tests the scenario in which the account is not found
	 */
	@Test
	public void test_deleteAccount_accountNotFound() throws AccountException {
		final UUID id = UUID.randomUUID();
		
		Mockito.when(accountRepository.findById(id.toString())).thenReturn(Optional.empty());
		
		try {
			accountService.deleteAccount(id);
			
			fail();
		}
		catch(final AccountNotFoundException ex) {
			
		}
	}
	
	/**
	 * Tests the scenario in which the account is deleted successfully
	 */
	@Test
	public void test_deleteAccount_successfulDelete() throws AccountException {
		final UUID id = UUID.randomUUID();
		final Account account = new Account();
		
		Mockito.when(accountRepository.findById(id.toString())).thenReturn(Optional.of(account));
		
		accountService.deleteAccount(id);
		
		Mockito.verify(accountRepository).delete(account);
	}
}
