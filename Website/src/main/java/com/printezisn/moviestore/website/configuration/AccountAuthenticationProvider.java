package com.printezisn.moviestore.website.configuration;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.services.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * The authentication provider
 */
@Component
@RequiredArgsConstructor
public class AccountAuthenticationProvider implements AuthenticationProvider {

	private final AccountService accountService;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
		final String username = authentication.getName();
		final String password = authentication.getCredentials().toString();
		
		try {
			final UserDetails userDetails = accountService.authenticate(username, password);

			return new UsernamePasswordAuthenticationToken(userDetails, null, new ArrayList<>());
		}
		catch(final AccountNotValidatedException ex) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(final Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
