package com.printezisn.moviestore.website.account.services;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.models.ChangePasswordModel;

/**
 * The interface of the account service
 */
public interface AccountService extends UserDetailsService {

    /**
     * Authenticates a user
     * 
     * @param username
     *            The username
     * @param password
     *            The password
     * @return The details of the account
     * @throws AccountAuthenticationException
     *             Exception thrown when there is an authentication error
     * @throws AccountNotValidatedException
     *             Exception thrown when the account is not authenticated
     */
    UserDetails authenticate(final String username, final String password)
        throws AccountAuthenticationException, AccountNotValidatedException;

    /**
     * Creates a new account
     * 
     * @param accountDto
     *            The model of the new account
     * @return The created account
     */
    AccountResultModel createAccount(final AccountDto accountDto);

    /**
     * Changes the password for an account
     * 
     * @param username
     *            The username of the account to change password for
     * @param changePasswordModel
     *            The model instance used for the change password operation
     * @return The updated account
     * @throws AccountNotValidatedException
     *             Exception thrown when the account is not authenticated with the
     *             current password
     */
    AccountResultModel changePassword(final String username, final ChangePasswordModel changePasswordModel)
        throws AccountNotValidatedException;
}
