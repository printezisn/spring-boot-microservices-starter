package com.printezisn.moviestore.website.account.services;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.account.exceptions.AccountAuthenticationException;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.models.AuthenticatedUser;
import com.printezisn.moviestore.website.configuration.properties.ServiceProperties;

import lombok.RequiredArgsConstructor;

/**
 * The implementation of the account service
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
	
	private static final String GET_URL = "%s/account/get/%s";
	private static final String AUTHENTICATE_URL = "%s/account/auth";
	
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
		
		try {
			final ResponseEntity<AccountResultModel> response = restTemplate.postForEntity(url, authDto, AccountResultModel.class);		
			final AccountDto accountDto = response.getBody().getResult();
			
			return new AuthenticatedUser(
				accountDto.getUsername(),
				password,
				accountDto.getEmailAddress(),
				new ArrayList<>());
		}
		catch(final HttpClientErrorException ex) {
			if(ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
				throw new AccountNotValidatedException();
			}
			
			throw new AccountAuthenticationException("User was not authenticated.", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		final String url = String.format(GET_URL, serviceProperties.getAccountServiceUrl(), username);
		
		try {
			final ResponseEntity<AccountResultModel> response = restTemplate.getForEntity(url, AccountResultModel.class);		
			final AccountDto accountDto = response.getBody().getResult();
			
			return new AuthenticatedUser(
				accountDto.getUsername(),
				accountDto.getPassword(),
				accountDto.getEmailAddress(),
				new ArrayList<>());
		}
		catch(final Exception ex) {
			throw new UsernameNotFoundException(username);
		}
	}
}
