package com.printezisn.moviestore.website.movie.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.Notification;
import com.printezisn.moviestore.common.models.Notification.NotificationType;
import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.movie.services.MovieService;

import lombok.RequiredArgsConstructor;

/**
 * The controller class associated with movies
 */
@Controller
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final AppUtils appUtils;

    /**
     * Renders the home page
     * 
     * @param text
     *            The text used to search movies
     * @param pageNumber
     *            The page number of the results to view
     * @param sortField
     *            The sorting field for the displayed movies
     * @param isAscending
     *            Indicates if the sorting is ascending or descending
     * @param model
     *            The page model
     * @return The home page view
     */
    @GetMapping("/")
    public String index(
        @RequestParam(value = "text", defaultValue = "") final String text,
        @RequestParam(value = "page", defaultValue = "0") final int pageNumber,
        @RequestParam(value = "sort", defaultValue = "") final String sortField,
        @RequestParam(value = "asc", defaultValue = "true") final boolean isAscending,
        final Model model) {

        appUtils.setCurrentPage(model, PageConstants.HOME_PAGE);

        final MoviePagedResultModel result = movieService.searchMovies(text, pageNumber, sortField, isAscending);

        model.addAttribute("entries", result.getEntries());
        model.addAttribute("page", result.getPageNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("sortField", result.getSortField());

        return "movie/index";
    }

    /**
     * Renders the create movie page
     * 
     * @param model
     *            The page model
     * @return The create page view
     */
    @GetMapping("/movie/new")
    public String createMovie(final Model model) {
        return getCreateMoviePage(model, new MovieDto(), Collections.emptyList());
    }

    /**
     * Creates a new movie
     * 
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param movieDto
     *            The model instance used for the create movie operation
     * @param model
     *            The create movie page model view
     * @return A redirect to the home page if the operation is successful, otherwise
     *         the create movie page view
     */
    @PostMapping("/movie/new")
    public String createMovie(
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        @ModelAttribute @Valid final MovieDto movieDto,
        final BindingResult bindingResult,
        final Model model) {

        final List<String> errors = appUtils.getModelErrors(bindingResult, "id", "creator");
        if (!errors.isEmpty()) {
            return getCreateMoviePage(model, movieDto, errors);
        }

        try {
            movieDto.setCreator(authentication.getName());

            final MovieResultModel result = movieService.createMovie(movieDto);
            if (result.hasErrors()) {
                return getCreateMoviePage(model, movieDto, result.getErrors());
            }
        }
        catch (final Exception ex) {
            return getCreateMoviePage(model, movieDto, appUtils.getUnexpectedErrorMessageAsList());
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationType.SUCCESS, appUtils.getMessage("message.createMovieSuccess")));

        return "redirect:/";
    }

    /**
     * Fills model values and returns the create movie page view
     * 
     * @param model
     *            The create movie page view model
     * @param movieDto
     *            The model instance associated with the create movie operation
     * @param errors
     *            The list of errors to display
     * @return The create movie page view
     */
    private String getCreateMoviePage(final Model model, final MovieDto movieDto, final List<String> errors) {
        appUtils.setCurrentPage(model, PageConstants.NEW_MOVIE_PAGE);

        final int currentYear = LocalDate.now().getYear();
        final List<Integer> years = IntStream
            .range(currentYear - 100, currentYear + 1)
            .boxed()
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());

        model.addAttribute("movie", movieDto);
        model.addAttribute("errors", errors);
        model.addAttribute("years", years);

        return "movie/create";
    }
}
