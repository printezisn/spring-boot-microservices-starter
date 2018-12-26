package com.printezisn.moviestore.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.models.Notification;
import com.printezisn.moviestore.common.models.Notification.NotificationType;

/**
 * Contains unit tests for the AppUtils class
 */
public class AppUtilsTest {
    private static final String CURRENT_PAGE_MODEL_PROPERTY = "currentPage";
    private static final String NOTIFICATION_LIST_ATTRIBUTE = "notifications";

    @Mock
    private MessageSource messageSource;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Captor
    private ArgumentCaptor<List<Notification>> notificationListCaptor;

    private Map<String, Object> flashAttributes;

    private AppUtils appUtils;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        LocaleContextHolder.setLocale(Locale.ENGLISH);

        flashAttributes = new HashMap<>();
        when(redirectAttributes.getFlashAttributes()).thenAnswer(i -> flashAttributes);

        appUtils = new AppUtils(messageSource);
    }

    /**
     * Tests that field error messages are returned correctly
     */
    @Test
    public void test_getModelErrors_fieldErrors() {
        final List<ObjectError> fieldErrors = Arrays.asList(
            new FieldError("", "field1", "test", true, new String[] { "code" }, new Object[0], "message1"),
            new FieldError("", "field2", "test", true, new String[] { "code" }, new Object[0], "message2"));

        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        fieldErrors
            .forEach(fieldError -> when(messageSource.getMessage(fieldError.getDefaultMessage(), null, Locale.ENGLISH))
                .thenReturn(fieldError.getDefaultMessage()));

        final List<String> errors = appUtils.getModelErrors(bindingResult);

        assertEquals(fieldErrors.size(), errors.size());
        fieldErrors.forEach(fieldError -> assertTrue(errors.contains(fieldError.getDefaultMessage())));
    }

    /**
     * Tests that error messages for excluded fields are not taken into account
     */
    @Test
    public void test_getModelErrors_excludedField() {
        final List<ObjectError> fieldErrors = Arrays.asList(
            new FieldError("", "field1", "test", true, new String[] { "code" }, new Object[0], "message1"),
            new FieldError("", "field2", "test", true, new String[] { "code" }, new Object[0], "message2"));

        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        fieldErrors
            .forEach(fieldError -> when(messageSource.getMessage(fieldError.getDefaultMessage(), null, Locale.ENGLISH))
                .thenReturn(fieldError.getDefaultMessage()));

        final List<String> errors = appUtils.getModelErrors(bindingResult, "field1");

        assertEquals(1, errors.size());
        assertTrue(errors.contains("message2"));
    }

    /**
     * Tests that error messages for type mismatch are returned correctly
     */
    @Test
    public void test_getModelErrors_typeMismatch() {
        final List<ObjectError> fieldErrors = Arrays.asList(
            new FieldError("", "field1", "test", true, new String[] { "typeMismatch" }, new Object[0], "message1"),
            new FieldError("", "field2", "test", true, new String[] { "typeMismatch" }, new Object[0], "message2"));

        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        fieldErrors.forEach(error -> {
            final FieldError fieldError = (FieldError) error;
            final String messageKey = "message.error.typeMismatch." + fieldError.getField();

            when(messageSource.getMessage(messageKey, null, Locale.ENGLISH)).thenReturn(messageKey);
        });

        final List<String> errors = appUtils.getModelErrors(bindingResult);

        assertEquals(2, errors.size());
        assertTrue(errors.contains("message.error.typeMismatch.field1"));
        assertTrue(errors.contains("message.error.typeMismatch.field2"));
    }

    /**
     * Tests that setCurrentPage successfully fills the appropriate property
     */
    @Test
    public void test_setCurrentPage_success() {
        appUtils.setCurrentPage(model, "test page");

        verify(model).addAttribute(CURRENT_PAGE_MODEL_PROPERTY, "test page");
    }

    /**
     * Tests that the notification is added successfully when there is no list
     * currently
     */
    @Test
    public void test_addNotification_withEmptyList() {
        final Notification notification = new Notification(NotificationType.INFO, "message");

        appUtils.addNotification(redirectAttributes, notification);

        verify(redirectAttributes).addFlashAttribute(eq(NOTIFICATION_LIST_ATTRIBUTE), notificationListCaptor.capture());

        assertEquals(1, notificationListCaptor.getValue().size());
        assertTrue(notificationListCaptor.getValue().contains(notification));
    }

    /**
     * Tests that the notification is added successfully when there is already a
     * list of notifications
     */
    @Test
    public void test_addNotification_withNonEmptyList() {
        final Notification newNotification = new Notification(NotificationType.INFO, "message");
        final Notification currentNotification = new Notification(NotificationType.INFO, "other message");

        flashAttributes.put(NOTIFICATION_LIST_ATTRIBUTE, new LinkedList<>(Arrays.asList(currentNotification)));

        appUtils.addNotification(redirectAttributes, newNotification);

        verify(redirectAttributes).addFlashAttribute(eq(NOTIFICATION_LIST_ATTRIBUTE), notificationListCaptor.capture());

        assertEquals(2, notificationListCaptor.getValue().size());
        assertTrue(notificationListCaptor.getValue().contains(newNotification));
        assertTrue(notificationListCaptor.getValue().contains(currentNotification));
    }

    /**
     * Tests that the correct message is returned
     */
    @Test
    public void test_getMessage_success() {
        when(messageSource.getMessage("test message", null, Locale.ENGLISH)).thenReturn("test message result");

        final String result = appUtils.getMessage("test message");

        assertEquals("test message result", result);
    }

    /**
     * Tests that the correct messages are returned
     */
    @Test
    public void test_getMessages_success() {
        when(messageSource.getMessage("test message 1", null, Locale.ENGLISH)).thenReturn("test message result 1");
        when(messageSource.getMessage("test message 2", null, Locale.ENGLISH)).thenReturn("test message result 2");

        final List<String> result = appUtils.getMessages("test message 1", "test message 2");

        assertEquals(2, result.size());
        assertTrue(result.contains("test message result 1"));
        assertTrue(result.contains("test message result 2"));
    }

    /**
     * Tests that the correct unexpected error message is returned
     */
    @Test
    public void test_getUnexpectedErrorMessageAsList_success() {
        when(messageSource.getMessage("message.error.unexpectedError", null, Locale.ENGLISH))
            .thenReturn("error message");

        final List<String> result = appUtils.getUnexpectedErrorMessageAsList();

        assertEquals(1, result.size());
        assertTrue(result.contains("error message"));
    }

    /**
     * Tests that the correct unexpected error message is returned
     */
    @Test
    public void test_getUnexpectedErrorMessage_success() {
        when(messageSource.getMessage("message.error.unexpectedError", null, Locale.ENGLISH))
            .thenReturn("error message");

        final String result = appUtils.getUnexpectedErrorMessage();

        assertEquals("error message", result);
    }

    /**
     * Tests the scenario in which the input URL is null
     */
    @Test
    public void test_getReturnURL_nullURL() {
        final String result = appUtils.getReturnUrl(null, "fallback");

        assertEquals("fallback", result);
    }

    /**
     * Tests the scenario in which the input URL is blank
     */
    @Test
    public void test_getReturnURL_blankURL() {
        final String result = appUtils.getReturnUrl(" ", "fallback");

        assertEquals("fallback", result);
    }

    /**
     * Tests the scenario in which the input URL is invalid
     */
    @Test
    public void test_getReturnURL_invalidURL() {
        final String result = appUtils.getReturnUrl("http:////////", "fallback");

        assertEquals("fallback", result);
    }

    /**
     * Tests the scenario in which the input URL is absolute
     */
    @Test
    public void test_getReturnURL_absoluteURL() {
        final String result = appUtils.getReturnUrl("http://www.absolute.com/", "fallback");

        assertEquals("fallback", result);
    }

    /**
     * Tests the scenario in which the input URL is relative
     */
    @Test
    public void test_getReturnURL_relativeURL() {
        final String result = appUtils.getReturnUrl("/movie/name", "fallback");

        assertEquals("/movie/name", result);
    }

    /**
     * Tests the scenario in which the query string is null
     */
    @Test
    public void test_getLocalUrl_nullQueryString() {
        when(httpServletRequest.getRequestURI()).thenReturn("/path");

        final String result = appUtils.getLocalUrl(httpServletRequest);

        assertEquals("/path", result);
    }

    /**
     * Tests the scenario in which the query string is blank
     */
    @Test
    public void test_getLocalUrl_blankQueryString() {
        when(httpServletRequest.getRequestURI()).thenReturn("/path");
        when(httpServletRequest.getQueryString()).thenReturn("  ");

        final String result = appUtils.getLocalUrl(httpServletRequest);

        assertEquals("/path", result);
    }

    /**
     * Tests the scenario in which the request URL contains a query string too
     */
    @Test
    public void test_getLocalUrl_withQueryString() {
        when(httpServletRequest.getRequestURI()).thenReturn("/path");
        when(httpServletRequest.getQueryString()).thenReturn("param=true");

        final String result = appUtils.getLocalUrl(httpServletRequest);

        assertEquals("/path?param=true", result);
    }
}
