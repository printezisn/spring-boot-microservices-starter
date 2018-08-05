package com.printezisn.moviestore.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Model used for sending notifications to the application front-end
 */
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Notification {

    /**
     * The available notification types
     */
    public static class NotificationTypes {
        public static final String INFO = "info";
        public static final String SUCCESS = "success";
        public static final String WARNING = "warning";
        public static final String ERROR = "error";
    }
    
    private final String type;
    private String title;
    private final String message;
}
