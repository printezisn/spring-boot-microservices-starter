package com.printezisn.moviestore.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.models.Notification;

import lombok.RequiredArgsConstructor;

/**
 * Utility class with helpful methods for all the application layers
 */
@Component
@RequiredArgsConstructor
public class AppUtils {

    private static final String CURRENT_PAGE_MODEL_PROPERTY = "currentPage";
    private static final String NOTIFICATION_LIST_ATTRIBUTE = "notifications";

    private final MessageSource messageSource;

    /**
     * Returns a localized message with a fallback
     * 
     * @param messageKey
     *            The key identifier of the message
     * @return The localized message or its fallback if it doesn't exist
     */
    private String getMessageWithDefault(final String messageKey) {
        try {
            return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
        }
        catch (final NoSuchMessageException ex) {
            return messageKey;
        }
    }

    /**
     * Returns a list of errors from the binding result
     * 
     * @param bindingResult
     *            The binding result
     * @param excludedFields
     *            The fields to exclude (errors on these fields are not counted)
     * @return The list of errors found
     */
    public List<String> getModelErrors(final BindingResult bindingResult, final String... excludedFields) {
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
            .map(error -> {
                final FieldError fieldError = (FieldError) error;
                switch (fieldError.getCode()) {
                    case "typeMismatch":
                        return "message.error.typeMismatch." + fieldError.getField();
                    default:
                        return fieldError.getDefaultMessage();
                }
            })
            .map(this::getMessageWithDefault)
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
    public void setCurrentPage(final Model model, final String currentPage) {
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
    public void addNotification(final RedirectAttributes redirectAttributes, final Notification notification) {
        List<Notification> notifications = new LinkedList<>();

        if (redirectAttributes.getFlashAttributes().containsKey(NOTIFICATION_LIST_ATTRIBUTE)) {
            notifications = (List<Notification>) redirectAttributes.getFlashAttributes()
                .get(NOTIFICATION_LIST_ATTRIBUTE);
        }

        notifications.add(notification);
        redirectAttributes.addFlashAttribute(NOTIFICATION_LIST_ATTRIBUTE, notifications);
    }

    /**
     * Returns a localized message, for unexpected errors, in a list
     * 
     * @return The localized message in a list
     */
    public List<String> getUnexpectedErrorMessageAsList() {
        return getMessages("message.error.unexpectedError");
    }

    /**
     * Returns a localized message for unexpected errors
     * 
     * @return The localized message
     */
    public String getUnexpectedErrorMessage() {
        return getMessage("message.error.unexpectedError");
    }

    /**
     * Returns localized messages
     * 
     * @param messageKeys
     *            The key identifiers of the messages
     * @return The localized messages
     */
    public List<String> getMessages(final String... messageKeys) {
        return Arrays.stream(messageKeys)
            .map(this::getMessage)
            .collect(Collectors.toList());
    }

    /**
     * Returns a localized message
     * 
     * @param messageKey
     *            The key identifier of the message
     * @return The localized message
     */
    public String getMessage(final String messageKey) {
        return messageSource.getMessage(messageKey, null, LocaleContextHolder.getLocale());
    }

    /**
     * Checks if a URL is appropriate for a return URL and returns it. If it's not
     * appropriate, it return a fallback URL
     * 
     * @param url
     *            The URL that is candidate for return URL
     * @param fallback
     *            The fallback URL
     * @return The appropriate return URL
     */
    public String getReturnUrl(final String url, final String fallback) {
        if (url == null || url.isBlank()) {
            return fallback;
        }

        try {
            final URI uri = new URI(url);
            if (uri.isAbsolute()) {
                return fallback;
            }

            return url;
        }
        catch (final URISyntaxException ex) {
            return fallback;
        }
    }

    /**
     * Returns the URL of a request, containing only the path and query string
     * 
     * @param request
     *            The HTTP servlet request
     * @return The request URL
     */
    public String getLocalUrl(final HttpServletRequest request) {
        if (request.getQueryString() == null || request.getQueryString().isBlank()) {
            return request.getRequestURI();
        }

        return request.getRequestURI() + "?" + request.getQueryString();
    }
}
