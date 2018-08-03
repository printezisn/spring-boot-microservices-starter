package com.printezisn.moviestore.website.account.services;

import java.util.ArrayList;

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

    private static final String GET_URL = "%s/account/get/%s";
    private static final String AUTHENTICATE_URL = "%s/account/auth";
    private static final String CREATE_URL = "%s/account/new";

    private final ServiceProperties serviceProperties;

    private final RestTemplate restTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails authenticate(final String username, final String password)
        throws AccountAuthenticationException, AccountNotValidatedException {

        final String url = String.format(AUTHENTICATE_URL, serviceProperties.getAccountServiceUrl());
        final AuthDto authDto = new AuthDto();
        authDto.setUsername(username);
        authDto.setPassword(password);

        ResponseEntity<AccountResultModel> response;

        try {
            response = restTemplate.postForEntity(url, authDto, AccountResultModel.class);
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new AccountAuthenticationException("User was not authenticated.", ex);
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
        final String url = String.format(GET_URL, serviceProperties.getAccountServiceUrl(), username);

        ResponseEntity<AccountResultModel> response;

        try {
            response = restTemplate.getForEntity(url, AccountResultModel.class);
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new UsernameNotFoundException(username);
        }

        if (response.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new UsernameNotFoundException(username);
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
    public AccountResultModel createAccount(final AccountDto accountDto)
        throws AccountPersistenceException {

        final String url = String.format(CREATE_URL, serviceProperties.getAccountServiceUrl());

        try {
            return restTemplate.postForEntity(url, accountDto, AccountResultModel.class).getBody();
        }
        catch (final Exception ex) {
            log.error("An error occurred: " + ex.getMessage(), ex);
            throw new AccountPersistenceException();
        }
    }
}
