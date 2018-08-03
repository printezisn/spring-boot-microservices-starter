package com.printezisn.moviestore.common;

/**
 * The class that contains common regular expressions
 */
public class RegexLibrary {

    /**
     * Regular expression for usernames
     */
    public static final String USERNAME_REGEX = "^[A-Za-z0-9_\\-]+$";

    /**
     * Regular expression for passwords
     */
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}";
}
