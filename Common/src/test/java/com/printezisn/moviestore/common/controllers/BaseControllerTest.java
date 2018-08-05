package com.printezisn.moviestore.common.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.printezisn.moviestore.common.models.Notification.NotificationTypes;
import com.printezisn.moviestore.common.models.Result;

/**
 * Contains unit tests for the BaseController class
 */
public class BaseControllerTest {

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

    @Captor
    private ArgumentCaptor<List<Notification>> notificationListCaptor;

    private Map<String, Object> flashAttributes;

    private BaseController baseController;

    /**
     * Initializes the test class
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        LocaleContextHolder.setLocale(Locale.ENGLISH);

        flashAttributes = new HashMap<>();
        when(redirectAttributes.getFlashAttributes()).thenAnswer(i -> flashAttributes);

        baseController = new BaseController();
    }

    /**
     * Tests that field error messages are returned correctly
     */
    @Test
    public void test_getErrorResult_fieldErrors() {
        final List<ObjectError> fieldErrors = Arrays.asList(
            new FieldError("", "field1", "message1"),
            new FieldError("", "field2", "message2"));

        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        fieldErrors
            .forEach(fieldError -> when(messageSource.getMessage(fieldError.getDefaultMessage(), null, Locale.ENGLISH))
                .thenReturn(fieldError.getDefaultMessage()));

        final Result<Integer> result = baseController.getErrorResult(bindingResult, messageSource);

        assertEquals(fieldErrors.size(), result.getErrors().size());
        fieldErrors.forEach(fieldError -> assertTrue(result.getErrors().contains(fieldError.getDefaultMessage())));
    }

    /**
     * Tests that error messages for excluded fields are not taken into account
     */
    @Test
    public void test_getErrorResult_excludedField() {
        final List<ObjectError> fieldErrors = Arrays.asList(
            new FieldError("", "field1", "message1"),
            new FieldError("", "field2", "message2"));

        when(bindingResult.getAllErrors()).thenReturn(fieldErrors);
        fieldErrors
            .forEach(fieldError -> when(messageSource.getMessage(fieldError.getDefaultMessage(), null, Locale.ENGLISH))
                .thenReturn(fieldError.getDefaultMessage()));

        final Result<Integer> result = baseController.getErrorResult(bindingResult, messageSource, "field1");

        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().contains("message2"));
    }

    /**
     * Tests that setCurrentPage successfully fills the appropriate property
     */
    @Test
    public void test_setCurrentPage_success() {
        baseController.setCurrentPage(model, "test page");

        verify(model).addAttribute(CURRENT_PAGE_MODEL_PROPERTY, "test page");
    }

    /**
     * Tests that the notification is added successfully when there is no list
     * currently
     */
    @Test
    public void test_addNotification_withEmptyList() {
        final Notification notification = new Notification(NotificationTypes.INFO, "message");

        baseController.addNotification(redirectAttributes, notification);

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
        final Notification newNotification = new Notification(NotificationTypes.INFO, "message");
        final Notification currentNotification = new Notification(NotificationTypes.INFO, "other message");

        flashAttributes.put(NOTIFICATION_LIST_ATTRIBUTE, new LinkedList<>(Arrays.asList(currentNotification)));

        baseController.addNotification(redirectAttributes, newNotification);

        verify(redirectAttributes).addFlashAttribute(eq(NOTIFICATION_LIST_ATTRIBUTE), notificationListCaptor.capture());

        assertEquals(2, notificationListCaptor.getValue().size());
        assertTrue(notificationListCaptor.getValue().contains(newNotification));
        assertTrue(notificationListCaptor.getValue().contains(currentNotification));
    }
}
