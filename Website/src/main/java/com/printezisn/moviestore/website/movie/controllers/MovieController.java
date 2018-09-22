package com.printezisn.moviestore.website.movie.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.website.Constants.PageConstants;

import lombok.RequiredArgsConstructor;

/**
 * The controller class associated with movies
 */
@Controller
@RequiredArgsConstructor
public class MovieController {

    public final AppUtils appUtils;

    /**
     * Renders the home page
     * 
     * @param model
     *            The page model
     * @return The home page view
     */
    @GetMapping("/")
    public String index(final Model model) {
        appUtils.setCurrentPage(model, PageConstants.HOME_PAGE);

        return "movie/index";
    }
}
