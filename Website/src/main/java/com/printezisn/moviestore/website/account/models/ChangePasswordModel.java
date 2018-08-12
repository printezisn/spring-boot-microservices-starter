package com.printezisn.moviestore.website.account.models;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.printezisn.moviestore.common.RegexLibrary;

import lombok.Data;

/**
 * Model class for the change password operation
 */
@Data
public class ChangePasswordModel {

    @NotEmpty(message = "message.changePassword.error.currentPasswordRequired")
    private String currentPassword;

    @NotEmpty(message = "message.changePassword.error.newPasswordRequired")
    @Size(max = 250, message = "message.changePassword.error.newPasswordMaxLength")
    @Pattern(regexp = RegexLibrary.PASSWORD_REGEX, message = "message.changePassword.error.newPasswordFormat")
    private String newPassword;
}
