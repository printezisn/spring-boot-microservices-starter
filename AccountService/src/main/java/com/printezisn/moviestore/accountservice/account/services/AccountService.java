package com.printezisn.moviestore.accountservice.account.services;

import java.util.Optional;

import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.common.dto.account.AccountDto;

/**
 * The service layer for accounts
 */
public interface AccountService {

    /**
     * Returns an account
     * 
     * @param username
     *            The username of the account
     * @return The account
     */
    Optional<AccountDto> getAccount(final String username);

    /**
     * Returns an account
     * 
     * @param username
     *            The username of the account
     * @param password
     *            The password of the account
     * @return The account
     */
    Optional<AccountDto> getAccount(final String username, final String password);

    /**
     * Creates a new account
     * 
     * @param accountDto
     *            The model of the account
     * @return The model of the new account
     * @throws AccountValidationException
     *             Validation exception
     */
    AccountDto createAccount(final AccountDto accountDto) throws AccountValidationException;

    /**
     * Updates an account
     * 
     * @param accountDto
     *            THe model of the account
     * @return The model of the updated account
     * @throws AccountNotFoundException
     *             Exception indicating that the account was not found
     */
    AccountDto updateAccount(final AccountDto accountDto) throws AccountNotFoundException;

    /**
     * Deletes an account
     * 
     * @param id
     *            The username of the account
     * @return True if the operation is successful, otherwise false
     */
    void deleteAccount(final String username);
}
