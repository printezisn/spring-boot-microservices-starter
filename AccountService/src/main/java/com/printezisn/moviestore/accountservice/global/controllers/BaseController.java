package com.printezisn.moviestore.accountservice.global.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.printezisn.moviestore.accountservice.global.models.Result;


/**
 * The base class for every controller
 */
public class BaseController {

	/**
	 * Returns a result object that contains errors from the binding result
	 * 
	 * @param bindingResult The binding result
	 * @param messageSource The source of localized messages
	 * @return The result object
	 */
	protected <T> Result<T> getErrorResult(final BindingResult bindingResult, final MessageSource messageSource, final String... excludedFields) {
		final List<String> excludedFieldsList = Arrays.asList(excludedFields);
		final Result<T> errorResult = new Result<>();
		
		final List<String> errors = bindingResult.getAllErrors()
			.stream()
			.filter(error -> {
				if(!(error instanceof FieldError)) {
					return true;
				}
				
				final FieldError fieldError = (FieldError)error;
				
				return !excludedFieldsList.contains(fieldError.getField());
			})
			.map(error -> messageSource.getMessage(error.getDefaultMessage(), null, LocaleContextHolder.getLocale()))
			.collect(Collectors.toList());
		
		errorResult.setErrors(errors);
		
		return errorResult;
	}
}
