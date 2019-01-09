package com.printezisn.moviestore.website.configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.printezisn.moviestore.website.account.services.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * The class with security configuration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String REMEMBER_ME_COOKIE_NAME = ".MOVIESTORE";
    private static final int REMEMBER_ME_VALIDITY_SECONDS = (int) ChronoUnit.SECONDS.between(LocalDateTime.now(),
        LocalDateTime.now().plusMonths(3));

    private final AccountAuthenticationProvider accountAuthenticationProvider;
    private final AccountService accountService;

    /**
     * Configures the permissions
     */
    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .formLogin()
                .loginPage("/auth/login")
                .permitAll()
            .and()
            .rememberMe()
                .tokenValiditySeconds(REMEMBER_ME_VALIDITY_SECONDS)
                .rememberMeCookieName(REMEMBER_ME_COOKIE_NAME)
            .and()
                .logout()
                .logoutSuccessUrl("/")
                .logoutUrl("/auth/logout")
                .permitAll()
            .and()
            .exceptionHandling()
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/account/changePassword"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/movie/new"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/movie/edit"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/movie/edit/*"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/movie/delete"))
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/auth/login"),
                    new AntPathRequestMatcher("/movie/delete/*"))
                .defaultAuthenticationEntryPointFor(
                    new Http403ForbiddenEntryPoint(),
                    new AntPathRequestMatcher("/movie/like/**"))
                .defaultAuthenticationEntryPointFor(
                    new Http403ForbiddenEntryPoint(),
                    new AntPathRequestMatcher("/movie/unlike/**"))
            .and()
            .authorizeRequests()
                .antMatchers("/account/changePassword").authenticated()
                .antMatchers("/movie/new").authenticated()
                .antMatchers("/movie/edit").authenticated()
                .antMatchers("/movie/edit/*").authenticated()
                .antMatchers("/movie/delete").authenticated()
                .antMatchers("/movie/delete/*").authenticated()
                .antMatchers("/movie/like/**").authenticated()
                .antMatchers("/movie/unlike/**").authenticated()
            .and()
            .authorizeRequests()
                .anyRequest().permitAll();
    }

    /**
     * Configures the authentication manager
     */
    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(accountAuthenticationProvider);
        auth.userDetailsService(accountService);
    }
}
