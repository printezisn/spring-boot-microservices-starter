package com.printezisn.moviestore.website.account.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.printezisn.moviestore.common.controllers.BaseController;
import com.printezisn.moviestore.website.Constants.PageConstants;

/**
 * The controller class associated with authentication
 */
@Controller
public class AuthController extends BaseController {

    /**
     * Renders the login page
     * 
     * @param model
     *            The page model
     * @return The login page view
     */
    @GetMapping("/auth/login")
    public String login(final Model model) {
        setCurrentPage(model, PageConstants.LOGIN_PAGE);

        return "auth/login";
    }
}
