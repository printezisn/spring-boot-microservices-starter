package com.printezisn.moviestore.common.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
    @RequiredArgsConstructor
    @Getter
    public static enum NotificationType {
        INFO("info"),
        SUCCESS("success"),
        WARNING("warning"),
        ERROR("error");

        private final String name;
    }

    private final NotificationType type;
    private String title;
    private final String message;
}
