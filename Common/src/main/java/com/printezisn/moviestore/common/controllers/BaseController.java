package com.printezisn.moviestore.common.controllers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.models.Notification;

/**
 * The base class for every controller
 */
public class BaseController {

    private static final String CURRENT_PAGE_MODEL_PROPERTY = "currentPage";
    private static final String NOTIFICATION_LIST_ATTRIBUTE = "notifications";

    /**
     * Returns a list of errors from the binding result
     * 
     * @param bindingResult
     *            The binding result
     * @param messageSource
     *            The source of localized messages
     * @return The list of errors found
     */
    protected List<String> getModelErrors(final BindingResult bindingResult, final MessageSource messageSource,
        final String... excludedFields) {
        final List<String> excludedFieldsList = Arrays.asList(excludedFields);

        return bindingResult.getAllErrors()
            .stream()
            .filter(error -> {
                if (!(error instanceof FieldError)) {
                    return true;
                }

                final FieldError fieldError = (FieldError) error;

                return !excludedFieldsList.contains(fieldError.getField());
            })
            .map(error -> messageSource.getMessage(error.getDefaultMessage(), null, LocaleContextHolder.getLocale()))
            .collect(Collectors.toList());
    }

    /**
     * Sets the current page
     * 
     * @param model
     *            The page model
     * @param currentPage
     *            The current page
     */
    protected void setCurrentPage(final Model model, final String currentPage) {
        model.addAttribute(CURRENT_PAGE_MODEL_PROPERTY, currentPage);
    }

    /**
     * Adds a new notification, by using flash attributes
     * 
     * @param redirectAttributes
     *            The redirected attributes that contain flash attributes
     * @param notification
     *            The notification to add
     */
    @SuppressWarnings("unchecked")
    protected void addNotification(final RedirectAttributes redirectAttributes, final Notification notification) {
        List<Notification> notifications = new LinkedList<>();

        if (redirectAttributes.getFlashAttributes().containsKey(NOTIFICATION_LIST_ATTRIBUTE)) {
            notifications = (List<Notification>) redirectAttributes.getFlashAttributes()
                .get(NOTIFICATION_LIST_ATTRIBUTE);
        }

        notifications.add(notification);
        redirectAttributes.addFlashAttribute(NOTIFICATION_LIST_ATTRIBUTE, notifications);
    }
}
