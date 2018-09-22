package com.printezisn.moviestore.website.account.controllers;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.Notification;
import com.printezisn.moviestore.common.models.Notification.NotificationTypes;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.account.exceptions.AccountNotValidatedException;
import com.printezisn.moviestore.website.account.models.ChangePasswordModel;
import com.printezisn.moviestore.website.account.services.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * The controller class associated with accounts
 */
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AppUtils appUtils;

    /**
     * Renders the register page
     * 
     * @param model
     *            The register page model
     * @return The register page view
     */
    @GetMapping("/account/register")
    public String register(final Model model) {
        return getRegisterPage(model, new AccountDto(), Collections.emptyList());
    }

    /**
     * Creates a new account
     * 
     * @param redirectAttributes
     *            The redirect attributes
     * @param accountDto
     *            The account to create
     * @param model
     *            The register page model
     * @return A redirect to the home page if the operation is successful, otherwise
     *         the register page view
     */
    @PostMapping("/account/register")
    public String register(final RedirectAttributes redirectAttributes, @ModelAttribute final AccountDto accountDto,
        final Model model) {

        if (accountDto == null) {
            return getRegisterPage(model, new AccountDto(), Collections.emptyList());
        }

        try {
            final AccountResultModel result = accountService.createAccount(accountDto);
            if (!result.getErrors().isEmpty()) {
                return getRegisterPage(model, accountDto, result.getErrors());
            }
        }
        catch (final Exception ex) {
            return getRegisterPage(model, accountDto, appUtils.getUnexpectedErrorMessageAsList());
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationTypes.SUCCESS, appUtils.getMessage("message.registerSuccess")));

        return "redirect:/";
    }

    /**
     * Fills model values and returns the register page view
     * 
     * @param model
     *            The register page view model
     * @param accountDto
     *            The account associated with the registration
     * @param errors
     *            The list of errors to display
     * @return The register page view
     */
    private String getRegisterPage(final Model model, final AccountDto accountDto, final List<String> errors) {
        appUtils.setCurrentPage(model, PageConstants.REGISTER_PAGE);
        model.addAttribute("account", accountDto);
        model.addAttribute("errors", errors);

        return "account/register";
    }

    /**
     * Renders the change password page
     * 
     * @param model
     *            The change password page model
     * @return The change password page view
     */
    @GetMapping("/account/changePassword")
    public String changePassword(final Model model) {
        return getChangePasswordPage(model, new ChangePasswordModel(), Collections.emptyList());
    }

    /**
     * Changes the password of an account
     * 
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param changePasswordModel
     *            The model instance used for the change password operation
     * @param model
     *            The register page model view
     * @return A redirect to the home page if the operation is successful, otherwise
     *         the register page view
     */
    @PostMapping("/account/changePassword")
    public String changePassword(
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        @ModelAttribute @Valid final ChangePasswordModel changePasswordModel,
        final BindingResult bindingResult,
        final Model model) {

        // Checks if there are validation errors
        final List<String> errors = appUtils.getModelErrors(bindingResult);
        if (!errors.isEmpty()) {
            return getChangePasswordPage(model, changePasswordModel, errors);
        }

        try {
            // Invokes the operation and checks if there are validation errors
            final AccountResultModel result = accountService.changePassword(authentication.getName(),
                changePasswordModel);
            if (!result.getErrors().isEmpty()) {
                return getChangePasswordPage(model, changePasswordModel, result.getErrors());
            }
        }
        catch (final AccountNotValidatedException ex) {
            // Checks if the current password is valid
            return getChangePasswordPage(model, changePasswordModel,
                appUtils.getMessages("message.changePassword.invalidCurrentPassword"));
        }
        catch (final Exception ex) {
            return getChangePasswordPage(model, changePasswordModel, appUtils.getUnexpectedErrorMessageAsList());
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationTypes.SUCCESS, appUtils.getMessage("message.changePasswordSuccess")));

        return "redirect:/";
    }

    /**
     * Fills model values and returns the change password page view
     * 
     * @param model
     *            The register page view model
     * @param changePasswordModel
     *            The model instance associated with the change password operation
     * @param errors
     *            The list of errors to display
     * @return The change password page view
     */
    private String getChangePasswordPage(final Model model, final ChangePasswordModel changePasswordModel,
        final List<String> errors) {

        appUtils.setCurrentPage(model, PageConstants.CHANGE_PASSWORD_PAGE);
        model.addAttribute("model", (changePasswordModel != null)
            ? changePasswordModel
            : new ChangePasswordModel());
        model.addAttribute("errors", errors);

        return "account/changePassword";
    }
}
