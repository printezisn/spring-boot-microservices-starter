package com.printezisn.moviestore.website.account.models;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * The model class for authenticated users
 */
@SuppressWarnings("serial")
@Data
@RequiredArgsConstructor
public class AuthenticatedUser implements UserDetails {

    private final String username;
    private final String password;
    private final String emailAddress;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
