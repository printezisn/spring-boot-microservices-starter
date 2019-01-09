package com.printezisn.moviestore.website.movie.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.printezisn.moviestore.common.AppUtils;
import com.printezisn.moviestore.common.dto.movie.MovieDto;
import com.printezisn.moviestore.common.models.Notification;
import com.printezisn.moviestore.common.models.Notification.NotificationType;
import com.printezisn.moviestore.common.models.movie.MoviePagedResultModel;
import com.printezisn.moviestore.common.models.movie.MovieResultModel;
import com.printezisn.moviestore.website.Constants.PageConstants;
import com.printezisn.moviestore.website.movie.exceptions.MovieNotFoundException;
import com.printezisn.moviestore.website.movie.models.LikeStatus;
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
     * @param httpServletRequest
     *            The HTTP servlet request
     * @param model
     *            The page model
     * @return The home page view
     */
    @GetMapping("/")
    public String index(
        @RequestParam(value = "text", defaultValue = "") final String text,
        @RequestParam(value = "page", defaultValue = "0") final int pageNumber,
        @RequestParam(value = "sort", defaultValue = "") final String sortField,
        @RequestParam(value = "asc", defaultValue = "false") final boolean isAscending,
        final HttpServletRequest httpServletRequest,
        final Model model) {

        appUtils.setCurrentPage(model, PageConstants.HOME_PAGE);

        final MoviePagedResultModel result = movieService.searchMovies(text, pageNumber, sortField, isAscending);

        model.addAttribute("entries", result.getEntries());
        model.addAttribute("text", text);
        model.addAttribute("page", result.getPageNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("sortField", result.getSortField());
        model.addAttribute("isAscending", result.isAscending());

        final String currentUrl = URLEncoder.encode(appUtils.getLocalUrl(httpServletRequest), StandardCharsets.UTF_8);
        model.addAttribute("currentUrl", currentUrl);

        return "movie/index";
    }

    /**
     * Renders the movie details page
     * 
     * @param id
     *            The id of the movie
     * @param returnUrl
     *            The URL to return if the user wants to go back
     * @param redirectAttributes
     *            The redirect attributes
     * @param model
     *            The page model
     * @return The movie details page
     */
    @GetMapping("/movie/details/{id}")
    public String getMovie(
        @PathVariable("id") final UUID id,
        @RequestParam(value = "returnUrl", defaultValue = "") final String returnUrl,
        final RedirectAttributes redirectAttributes,
        final Model model) {

        try {
            final MovieDto result = movieService.getMovie(id);

            model.addAttribute("movie", result);
            model.addAttribute("returnUrl", appUtils.getReturnUrl(returnUrl, "/"));

            return "movie/details";
        }
        catch (final MovieNotFoundException ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getMessage("message.error.movieNotFound")));

            return "redirect:/";
        }
        catch (final Exception ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getUnexpectedErrorMessage()));

            return "redirect:/";
        }
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
        return getUpdateMoviePage(model, new MovieDto(), Collections.emptyList(), PageConstants.NEW_MOVIE_PAGE, null);
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
            return getUpdateMoviePage(model, movieDto, errors, PageConstants.NEW_MOVIE_PAGE, null);
        }

        try {
            movieDto.setCreator(authentication.getName());

            final MovieResultModel result = movieService.createMovie(movieDto);
            if (result.hasErrors()) {
                return getUpdateMoviePage(model, movieDto, result.getErrors(), PageConstants.NEW_MOVIE_PAGE, null);
            }
        }
        catch (final Exception ex) {
            return getUpdateMoviePage(model, movieDto, appUtils.getUnexpectedErrorMessageAsList(),
                PageConstants.NEW_MOVIE_PAGE, null);
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationType.SUCCESS, appUtils.getMessage("message.createMovieSuccess")));

        return "redirect:/";
    }

    /**
     * Renders the edit movie page
     * 
     * @param id
     *            The id of the movie to edit
     * @param returnUrl
     *            The URL to return if the user wants to go back
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param model
     *            The page model
     * @return The edit page view
     */
    @GetMapping("/movie/edit/{id}")
    public String editMovie(
        @PathVariable("id") final UUID id,
        @RequestParam(value = "returnUrl", defaultValue = "") final String returnUrl,
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        final Model model) {

        try {
            final MovieDto movieDto = movieService.getMovie(id);
            if (!movieService.isAuthorizedOnMovie(authentication.getName(), movieDto)) {
                appUtils.addNotification(redirectAttributes,
                    new Notification(NotificationType.ERROR,
                        appUtils.getMessage("message.error.movieEdit.notAuthorized")));

                return "redirect:/";
            }

            return getUpdateMoviePage(model, movieDto, Collections.emptyList(), PageConstants.EDIT_MOVIE_PAGE,
                returnUrl);
        }
        catch (final MovieNotFoundException ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getMessage("message.error.movieNotFound")));

            return "redirect:/";
        }
        catch (final Exception ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getUnexpectedErrorMessage()));

            return "redirect:/";
        }
    }

    /**
     * Updates a movie
     * 
     * @param request
     *            Information about the HTTP request
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param movieDto
     *            The model instance used for the edit movie operation
     * @param model
     *            The edit movie page model view
     * @return A redirect to the home page if the operation is successful, otherwise
     *         the edit movie page view
     */
    @PostMapping("/movie/edit")
    public String editMovie(
        final HttpServletRequest request,
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        @ModelAttribute @Valid final MovieDto movieDto,
        final BindingResult bindingResult,
        final Model model) {

        final String returnUrl = request.getParameter("returnUrl");

        final List<String> errors = appUtils.getModelErrors(bindingResult, "creator");
        if (!errors.isEmpty()) {
            return getUpdateMoviePage(model, movieDto, errors, PageConstants.EDIT_MOVIE_PAGE, returnUrl);
        }

        try {
            if (!movieService.isAuthorizedOnMovie(authentication.getName(), movieDto.getId())) {
                return getUpdateMoviePage(model, movieDto,
                    appUtils.getMessages("message.error.movieEdit.notAuthorized"), PageConstants.EDIT_MOVIE_PAGE,
                    returnUrl);
            }

            final MovieResultModel result = movieService.updateMovie(movieDto);
            if (result.hasErrors()) {
                return getUpdateMoviePage(model, movieDto, result.getErrors(), PageConstants.EDIT_MOVIE_PAGE,
                    returnUrl);
            }
        }
        catch (final MovieNotFoundException ex) {
            return getUpdateMoviePage(model, movieDto, appUtils.getMessages("message.error.movieNotFound"),
                PageConstants.EDIT_MOVIE_PAGE, returnUrl);
        }
        catch (final Exception ex) {
            return getUpdateMoviePage(model, movieDto, appUtils.getUnexpectedErrorMessageAsList(),
                PageConstants.EDIT_MOVIE_PAGE, returnUrl);
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationType.SUCCESS, appUtils.getMessage("message.updateMovieSuccess")));

        return String.format("redirect:/movie/details/%s?returnUrl=%s", movieDto.getId(),
            appUtils.getReturnUrl(returnUrl, "/"));
    }

    /**
     * Renders the movie delete page
     * 
     * @param id
     *            The id of the movie
     * @param returnUrl
     *            The URL to return if the user wants to go back
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param model
     *            The page model
     * @return The movie details page
     */
    @GetMapping("/movie/delete/{id}")
    public String deleteMovie(
        @PathVariable("id") final UUID id,
        @RequestParam(value = "returnUrl", defaultValue = "") final String returnUrl,
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        final Model model) {

        try {
            final MovieDto result = movieService.getMovie(id);
            if (!movieService.isAuthorizedOnMovie(authentication.getName(), result)) {
                appUtils.addNotification(redirectAttributes,
                    new Notification(NotificationType.ERROR,
                        appUtils.getMessage("message.error.movieDelete.notAuthorized")));

                return "redirect:/";
            }

            model.addAttribute("movie", result);
            model.addAttribute("returnUrl", appUtils.getReturnUrl(returnUrl, "/"));

            return "movie/delete";
        }
        catch (final MovieNotFoundException ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getMessage("message.error.movieNotFound")));

            return "redirect:/";
        }
        catch (final Exception ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getUnexpectedErrorMessage()));

            return "redirect:/";
        }
    }

    /**
     * Deletes a movie
     * 
     * @param redirectAttributes
     *            The redirect attributes
     * @param authentication
     *            Information about the current user
     * @param id
     *            The id of the movie
     * @param returnUrl
     *            The URL to return if the user wants to go back
     * @param model
     *            The delete movie page model view
     * @return A redirect to the home page if the operation is successful, otherwise
     *         the edit movie page view
     */
    @PostMapping("/movie/delete")
    public String deleteMovie(
        final RedirectAttributes redirectAttributes,
        final Authentication authentication,
        @RequestParam(value = "id") final UUID id,
        @RequestParam(value = "returnUrl", defaultValue = "") final String returnUrl,
        final Model model) {

        try {
            if (!movieService.isAuthorizedOnMovie(authentication.getName(), id)) {
                appUtils.addNotification(redirectAttributes,
                    new Notification(NotificationType.ERROR,
                        appUtils.getMessage("message.error.movieDelete.notAuthorized")));

                return "redirect:/";
            }

            movieService.deleteMovie(id);
        }
        catch (final MovieNotFoundException ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getMessage("message.error.movieNotFound")));

            return "redirect:/";
        }
        catch (final Exception ex) {
            appUtils.addNotification(redirectAttributes,
                new Notification(NotificationType.ERROR, appUtils.getUnexpectedErrorMessage()));

            return "redirect:/";
        }

        appUtils.addNotification(redirectAttributes,
            new Notification(NotificationType.SUCCESS, appUtils.getMessage("message.deleteMovieSuccess")));

        return "redirect:" + appUtils.getReturnUrl(returnUrl, "/");
    }

    /**
     * Returns the like status of a movie
     * 
     * @param authentication
     *            Information about the current user
     * @param id
     *            The id of the movie to check
     * @return The like status of the movie
     */
    @GetMapping("/movie/likestatus/{id}")
    @ResponseBody
    public ResponseEntity<LikeStatus> likeStatus(
        final Authentication authentication,
        @PathVariable("id") final UUID id) {

        try {
            final MovieDto movie = movieService.getMovie(id);
            final boolean hasLiked = (authentication != null && authentication.isAuthenticated())
                ? movieService.hasLiked(authentication.getName(), id)
                : false;

            return ResponseEntity.ok(new LikeStatus(movie.getTotalLikes(), hasLiked));
        }
        catch (final MovieNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
        catch (final Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a like to a movie for the current user
     * 
     * @param authentication
     *            Information about the current user
     * @param id
     *            The id of the movie to check
     * @return Response indicating if the operation was successful or not
     */
    @PostMapping("/movie/like")
    @ResponseBody
    public ResponseEntity<?> like(
        final Authentication authentication,
        @RequestParam("id") final UUID id) {

        try {
            movieService.likeMovie(authentication.getName(), id);

            return ResponseEntity.ok().build();
        }
        catch (final MovieNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
        catch (final Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Removes a like from a movie for the current user
     * 
     * @param authentication
     *            Information about the current user
     * @param id
     *            The id of the movie to check
     * @return Response indicating if the operation was successful or not
     */
    @PostMapping("/movie/unlike")
    @ResponseBody
    public ResponseEntity<?> unlike(
        final Authentication authentication,
        @RequestParam("id") final UUID id) {

        try {
            movieService.unlikeMovie(authentication.getName(), id);

            return ResponseEntity.ok().build();
        }
        catch (final MovieNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
        catch (final Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Fills model values and returns the page view to create or edit a movie
     * 
     * @param model
     *            The create movie page view model
     * @param movieDto
     *            The model instance associated with the create movie operation
     * @param errors
     *            The list of errors to display
     * @param currentPage
     *            The current page
     * @param returnUrl
     *            The URL to return if the user wants to go back
     * @return The create movie page view
     */
    private String getUpdateMoviePage(final Model model, final MovieDto movieDto, final List<String> errors,
        final String currentPage, final String returnUrl) {

        appUtils.setCurrentPage(model, currentPage);

        final int currentYear = LocalDate.now().getYear();
        final List<Integer> years = IntStream
            .range(currentYear - 100, currentYear + 1)
            .boxed()
            .sorted(Collections.reverseOrder())
            .collect(Collectors.toList());

        model.addAttribute("movie", movieDto);
        model.addAttribute("errors", errors);
        model.addAttribute("years", years);
        model.addAttribute("returnUrl", appUtils.getReturnUrl(returnUrl, "/"));

        if (currentPage.equals(PageConstants.NEW_MOVIE_PAGE)) {
            return "movie/create";
        }

        return "movie/edit";
    }
}
