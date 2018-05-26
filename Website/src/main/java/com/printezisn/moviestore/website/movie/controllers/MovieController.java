package com.printezisn.moviestore.website.movie.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.printezisn.moviestore.common.controllers.BaseController;
import com.printezisn.moviestore.website.Constants;

/**
 * The controller class associated with movies
 */
@Controller
public class MovieController extends BaseController {

	/**
	 * Renders the home page
	 * 
	 * @param model The page model
	 * @return The home page view
	 */
	@GetMapping("/")
	public String index(final Model model) {
		setCurrentPage(model, Constants.PageConstants.HOME_PAGE);
		
		return "movie/index";
	}
}
