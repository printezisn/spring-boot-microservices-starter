package com.printezisn.moviestore.common.dto.account;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

/**
 * The data transfer object for authenticating accounts
 */
@Data
public class AuthDto {

    @NotEmpty(message = "message.account.error.usernameRequired")
    private String username;

    @NotEmpty(message = "message.account.error.passwordRequired")
    private String password;

    private boolean rememberMe;
}
