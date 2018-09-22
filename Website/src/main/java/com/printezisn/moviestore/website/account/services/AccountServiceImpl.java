package com.printezisn.moviestore.website.account.services;

import java.util.ArrayList;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.exceptions.AccountPersistenceException;
import com.printezisn.moviestore.website.account.models.AuthenticatedUser;
import com.printezisn.moviestore.website.account.models.ChangePasswordModel;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The implementation of the account service
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String GET_URL = "%s/account/get/%s?lang=%s";
    private static final String AUTHENTICATE_URL = "%s/account/auth?lang=%s";
    private static final String CREATE_URL = "%s/account/new?lang=%s";
    private static final String UPDATE_URL = "%s/account/update?lang=%s";

    private final ServiceProperties serviceProperties;

    private final RestTemplate restTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails authenticate(final String username, final String password)
        throws AccountAuthenticationException, AccountNotValidatedException {

        final String url = String.format(AUTHENTICATE_URL, serviceProperties.getAccountServiceUrl(),
            LocaleContextHolder.getLocale().getLanguage());
        final AuthDto authDto = new AuthDto();
        authDto.setUsername(username);
        authDto.setPassword(password);

        final ResponseEntity<AccountResultModel> response;

        try {
            response = restTemplate.postForEntity(url, authDto, AccountResultModel.class);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while authenticating account %s: %s", username,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountAuthenticationException(errorMessage, ex);
        }

        if (response.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
            throw new AccountNotValidatedException();
        }

        final AccountDto accountDto = response.getBody().getResult();

        return new AuthenticatedUser(
            accountDto.getUsername(),
            password,
            accountDto.getEmailAddress(),
            new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final String url = String.format(GET_URL, serviceProperties.getAccountServiceUrl(), username,
            LocaleContextHolder.getLocale().getLanguage());

        final ResponseEntity<AccountResultModel> response;

        try {
            response = restTemplate.getForEntity(url, AccountResultModel.class);
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while loading account %s: %s", username,
                ex.getMessage());

            log.error(errorMessage, ex);
            throw new UsernameNotFoundException(errorMessage, ex);
        }

        if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new UsernameNotFoundException(String.format("Account %s was not found.", username));
        }

        final AccountDto accountDto = response.getBody().getResult();

        return new AuthenticatedUser(
            accountDto.getUsername(),
            accountDto.getPassword(),
            accountDto.getEmailAddress(),
            new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResultModel createAccount(final AccountDto accountDto) {
        final String url = String.format(CREATE_URL, serviceProperties.getAccountServiceUrl(),
            LocaleContextHolder.getLocale().getLanguage());

        try {
            return restTemplate.postForEntity(url, accountDto, AccountResultModel.class).getBody();
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while creating account %s: %s",
                accountDto.getUsername(), ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResultModel changePassword(final String username, final ChangePasswordModel changePasswordModel)
        throws AccountNotValidatedException {

        final String url = String.format(UPDATE_URL, serviceProperties.getAccountServiceUrl(),
            LocaleContextHolder.getLocale().getLanguage());

        try {
            final AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticate(username,
                changePasswordModel.getCurrentPassword());

            final AccountDto accountDto = new AccountDto();
            accountDto.setUsername(authenticatedUser.getUsername());
            accountDto.setEmailAddress(authenticatedUser.getEmailAddress());
            accountDto.setPassword(changePasswordModel.getNewPassword());

            final ResponseEntity<AccountResultModel> result = restTemplate.postForEntity(url, accountDto,
                AccountResultModel.class);
            if (result.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new AccountNotValidatedException();
            }

            return result.getBody();
        }
        catch (final AccountNotValidatedException ex) {
            throw ex;
        }
        catch (final Exception ex) {
            final String errorMessage = String.format("An error occured while changing password for account %s: %s.",
                username, ex.getMessage());

            log.error(errorMessage, ex);
            throw new AccountPersistenceException(errorMessage, ex);
        }
    }
}
