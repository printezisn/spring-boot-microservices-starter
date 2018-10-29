package com.printezisn.moviestore.accountservice.account.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import com.printezisn.moviestore.accountservice.account.entities.Account;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountPersistenceException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.accountservice.account.mappers.AccountMapper;
import com.printezisn.moviestore.accountservice.account.repositories.AccountRepository;
import com.printezisn.moviestore.common.AppUtils;
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
    private final AppUtils appUtils;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AccountDto> getAccount(final String username) {
        try {
            final Optional<Account> account = accountRepository.findById(username);

            return account.isPresent()
                ? Optional.of(accountMapper.accountToAccountDto(account.get()))
                : Optional.empty();
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while reading account %s: %s", username,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AccountDto> getAccount(final String username, final String password) {
        try {
            final Optional<Account> account = accountRepository.findById(username);
            if (!account.isPresent()) {
                return Optional.empty();
            }

            final String hashedPassword = BCrypt.hashpw(password, account.get().getPasswordSalt());

            return account.get().getPassword().equals(hashedPassword)
                ? Optional.of(accountMapper.accountToAccountDto(account.get()))
                : Optional.empty();
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while reading account %s: %s", username,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDto createAccount(final AccountDto accountDto) throws AccountValidationException {
        // The username and email address must be unique
        if (accountRepository.findById(accountDto.getUsername()).isPresent()) {
            throw new AccountValidationException(appUtils.getMessage("message.account.usernameExists"));
        }
        if (accountRepository.findByEmailAddress(accountDto.getEmailAddress()).isPresent()) {
            throw new AccountValidationException(appUtils.getMessage("message.account.emailAddressExists"));
        }

        accountDto.setPasswordSalt(BCrypt.gensalt());
        accountDto.setPassword(BCrypt.hashpw(accountDto.getPassword(), accountDto.getPasswordSalt()));
        accountDto.setCreationTimestamp(Instant.now());
        accountDto.setUpdateTimestamp(Instant.now());

        final Account account = accountMapper.accountDtoToAccount(accountDto);

        try {
            accountRepository.save(account);
        }
        catch (final DuplicateKeyException ex) {
            throw new AccountValidationException(appUtils.getMessage("message.account.usernameOrEmailAddressExists"));
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while creating a new account: %s",
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }

        return accountMapper.accountToAccountDto(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountDto updateAccount(final AccountDto accountDto) throws AccountNotFoundException {
        final Account account = accountRepository.findById(accountDto.getUsername()).orElse(null);
        if (account == null) {
            throw new AccountNotFoundException();
        }

        account.setPasswordSalt(BCrypt.gensalt());
        account.setPassword(BCrypt.hashpw(accountDto.getPassword(), account.getPasswordSalt()));
        account.setUpdateTimestamp(Instant.now().toEpochMilli());

        try {
            accountRepository.save(account);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while updating account %s: %s",
                account.getUsername(), ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }

        return accountMapper.accountToAccountDto(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccount(final String username) {
        try {
            accountRepository.deleteById(username);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while deleting account %s: %s", username,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }
    }
}
