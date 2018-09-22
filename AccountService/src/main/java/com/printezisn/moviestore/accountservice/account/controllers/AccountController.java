package com.printezisn.moviestore.accountservice.account.controllers;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.printezisn.moviestore.accountservice.account.exceptions.AccountNotFoundException;
import com.printezisn.moviestore.accountservice.account.exceptions.AccountValidationException;
import com.printezisn.moviestore.accountservice.account.services.AccountService;
import com.printezisn.moviestore.common.controllers.BaseController;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.dto.account.AuthDto;
import com.printezisn.moviestore.common.models.Result;

import lombok.RequiredArgsConstructor;

/**
 * The controller that serves accounts
 */
@RestController
@RequiredArgsConstructor
public class AccountController extends BaseController {

    private final AccountService accountService;
    private final MessageSource messageSource;

    /**
     * Returns an account
     * 
     * @param username
     *            The username of the account
     * @return The account
     */
    @GetMapping(path = "/account/get/{username}")
    public ResponseEntity<?> getAccount(@PathVariable("username") final String username) {
        final Optional<AccountDto> account = accountService.getAccount(username);
        return account.isPresent()
            ? ResponseEntity.ok(account.get())
            : ResponseEntity.notFound().build();
    }

    /**
     * Authenticates an account based on a given username and password
     * 
     * @param authDto
     *            The authentication model
     * @param bindingResult
     *            The model binding result
     * @return The result of the operation
     */
    @PostMapping(path = "/account/auth")
    public ResponseEntity<?> authenticate(@Valid @RequestBody final AuthDto authDto,
        final BindingResult bindingResult) {

        final Result<AccountDto> errorResult = getErrorResult(bindingResult, messageSource);
        if (!errorResult.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(errorResult);
        }

        final Optional<AccountDto> account = accountService.getAccount(authDto.getUsername(), authDto.getPassword());

        return account.isPresent()
            ? ResponseEntity.ok(new Result<>(account))
            : ResponseEntity.badRequest().body(new Result<AccountDto>(getMessage("usernameOrPasswordInvalid")));
    }

    /**
     * Creates a new account
     * 
     * @param account
     *            The details of the account to create
     * @param bindingResult
     *            The model binding result
     * @return The result of the operation
     */
    @PostMapping(path = "/account/new")
    public ResponseEntity<?> createAccount(@Valid @RequestBody final AccountDto account,
        final BindingResult bindingResult) {

        final Result<AccountDto> errorResult = getErrorResult(bindingResult, messageSource, "id");
        if (!errorResult.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(errorResult);
        }

        try {
            final AccountDto createdAccount = accountService.createAccount(account);
            final Result<AccountDto> result = new Result<>(createdAccount);

            return ResponseEntity.ok(result);
        }
        catch (final AccountValidationException ex) {
            final Result<AccountDto> result = new Result<>(ex.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Updates an account
     * 
     * @param account
     *            The details of the account to create
     * @param bindingResult
     *            The model binding result
     * @return The result of the operation
     */
    @PostMapping(path = "/account/update")
    public ResponseEntity<?> updateAccount(@Valid @RequestBody final AccountDto account,
        final BindingResult bindingResult) {

        final Result<AccountDto> errorResult = getErrorResult(bindingResult, messageSource, "username", "emailAddress");
        if (!errorResult.getErrors().isEmpty()) {
            return ResponseEntity.badRequest().body(errorResult);
        }

        try {
            final AccountDto updatedAccount = accountService.updateAccount(account);
            final Result<AccountDto> result = new Result<>(updatedAccount);

            return ResponseEntity.ok(result);
        }
        catch (final AccountNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an account
     * 
     * @param username
     *            The username of the account
     * @return The result of the operation
     */
    @GetMapping(path = "/account/delete/{id}")
    public ResponseEntity<?> deleteAccount(@PathVariable("id") final String username) {
        accountService.deleteAccount(username);

        return ResponseEntity.ok().build();
    }

    /**
     * Returns a localized message
     * 
     * @param key
     *            The message key
     * @return The localized message
     */
    private String getMessage(final String key) {
        return messageSource.getMessage("message.account." + key, null, LocaleContextHolder.getLocale());
    }
}
