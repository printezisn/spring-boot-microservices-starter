package com.printezisn.moviestore.accountservice.account.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.printezisn.moviestore.accountservice.account.entities.Account;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountPersistenceException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.accountservice.account.mappers.AccountMapper;
import com.printezisn.moviestore.accountservice.account.repositories.AccountRepository;
import com.printezisn.moviestore.common.dto.account.AccountDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the service layer for accounts
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountRepository accountRepository;
	private final AccountMapper accountMapper;
	
	private final MessageSource messageSource;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<AccountDto> getAccount(final UUID id)
		throws AccountPersistenceException {
		
		try {
			final Optional<Account> account = accountRepository.findById(id.toString());
		
			return account.isPresent()
				? Optional.of(accountMapper.accountToAccountDto(account.get()))
				: Optional.empty();
		}
		catch(final Exception ex) {
			log.error("An error occurred: " + ex.getMessage(), ex);
			throw new AccountPersistenceException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<AccountDto> getAccount(final String username, final String password)
		throws AccountPersistenceException {
		
		try {
			final Optional<Account> account = accountRepository.findByUsername(username);
			if(!account.isPresent()) {
				return Optional.empty();
			}
		
			final String hashedPassword = BCrypt.hashpw(password, account.get().getPasswordSalt());
		
			return account.get().getPassword().equals(hashedPassword)
				? Optional.of(accountMapper.accountToAccountDto(account.get()))
				: Optional.empty();
		}
		catch(final Exception ex) {
			log.error("An error occurred: " + ex.getMessage(), ex);
			throw new AccountPersistenceException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountDto createAccount(final AccountDto accountDto)
		throws AccountPersistenceException, AccountValidationException {
		
		// The username and email address must be unique
		if(accountRepository.findByUsername(accountDto.getUsername()).isPresent()) {
			throw new AccountValidationException(getMessage("usernameExists"));
		}
		if(accountRepository.findByEmailAddress(accountDto.getEmailAddress()).isPresent()) {
			throw new AccountValidationException(getMessage("emailAddressExists"));
		}
		
		accountDto.setId(UUID.randomUUID());
		accountDto.setPasswordSalt(BCrypt.gensalt());
		accountDto.setPassword(BCrypt.hashpw(accountDto.getPassword(), accountDto.getPasswordSalt()));
		accountDto.setCreationTimestamp(Instant.now());
		accountDto.setUpdateTimestamp(Instant.now());
		
		final Account account = accountMapper.accountDtoToAccount(accountDto);
		
		try {
			accountRepository.save(account);
		}
		catch(final DuplicateKeyException ex) {
			throw new AccountValidationException(getMessage("usernameOrEmailAddressExists"));
		}
		catch(final Exception ex) {
			log.error("An error occurred: " + ex.getMessage(), ex);
			throw new AccountPersistenceException(ex);
		}
			
		return accountMapper.accountToAccountDto(account);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountDto updateAccount(AccountDto accountDto)
		throws AccountPersistenceException, AccountNotFoundException {
		
		final Account account = accountRepository.findById(accountDto.getId().toString()).orElse(null);
		if(account == null) {
			throw new AccountNotFoundException();
		}
		
		account.setPasswordSalt(BCrypt.gensalt());
		account.setPassword(BCrypt.hashpw(accountDto.getPassword(), account.getPasswordSalt()));
		account.setUpdateTimestamp(Instant.now().toString());
		
		try {
			accountRepository.save(account);
		}
		catch(final Exception ex) {
			log.error("An error occurred: " + ex.getMessage(), ex);
			throw new AccountPersistenceException(ex);
		}
		
		return accountMapper.accountToAccountDto(account);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteAccount(UUID id) throws AccountPersistenceException {
		try {
			accountRepository.deleteById(id.toString());
		}
		catch(final Exception ex) {
			log.error("An error occurred: " + ex.getMessage(), ex);
			throw new AccountPersistenceException(ex);
		}
	}
	
	/**
	 * Returns a localized message
	 * 
	 * @param key The message key 
	 * @return The localized message
	 */
	private String getMessage(final String key) {
		return messageSource.getMessage("message.account." + key, null, LocaleContextHolder.getLocale());
	}
}
