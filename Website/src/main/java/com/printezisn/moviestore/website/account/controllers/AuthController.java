package com.printezisn.moviestore.website.account.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.website.Constants.PageConstants;

import lombok.RequiredArgsConstructor;

/**
 * The controller class associated with authentication
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    public final AppUtils appUtils;

    /**
     * Renders the login page
     * 
     * @param model
     *            The page model
     * @return The login page view
     */
    @GetMapping("/auth/login")
    public String login(final Model model) {
        appUtils.setCurrentPage(model, PageConstants.LOGIN_PAGE);

        return "auth/login";
    }
}
