package com.printezisn.moviestore.website.account.controllers;

import java.util.Arrays;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.printezisn.moviestore.common.controllers.BaseController;
import com.printezisn.moviestore.common.dto.account.AccountDto;
import com.printezisn.moviestore.common.models.account.AccountResultModel;
import com.printezisn.moviestore.website.Constants.MessageKeys;
import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.account.services.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * The controller class associated with accounts
 */
@Controller
@RequiredArgsConstructor
public class AccountController extends BaseController {

	private final AccountService accountService;
	private final MessageSource messageSource;
	
	/**
	 * Renders the register page
	 * 
	 * @param model The page model
	 * @return The register page view
	 */
	@GetMapping("/account/register")
	public String register(final Model model) {
		setCurrentPage(model, PageConstants.REGISTER_PAGE);
		model.addAttribute("account", new AccountDto());
		
		return "account/register";
	}
	
	/**
	 * Creates a new account
	 * 
	 * @param accountDto The account to create
	 * @param model The register page model view
	 * @return A redirect to the home page if the operation is successful, otherwise the register page view
	 */
	@PostMapping("/account/register")
	public String register(@ModelAttribute final AccountDto accountDto, final Model model) {
		if(accountDto == null) {
			setCurrentPage(model, PageConstants.REGISTER_PAGE);
			model.addAttribute("account", new AccountDto());
			return "account/register";
		}
		
		try {
			final AccountResultModel result = accountService.createAccount(accountDto);
			if(!result.getErrors().isEmpty()) {
				setCurrentPage(model, PageConstants.REGISTER_PAGE);
				model.addAttribute("account", accountDto);
				model.addAttribute("errors", result.getErrors());
				
				return "account/register";
			}
		}
		catch(final Exception ex) {
			setCurrentPage(model, PageConstants.REGISTER_PAGE);
			model.addAttribute("account", accountDto);
			model.addAttribute("errors", Arrays.asList(getUnexpectedErrorMessage()));
			
			return "account/register";
		}
		
		return "redirect:/";
	}
	
	/**
	 * Returns a localized message for unexpected errors
	 * 
	 * @return The localized message
	 */
	private String getUnexpectedErrorMessage() {
		return messageSource.getMessage(MessageKeys.UNEXPECTED_ERROR_MESSAGE_KEY, null, LocaleContextHolder.getLocale());
	}
}
