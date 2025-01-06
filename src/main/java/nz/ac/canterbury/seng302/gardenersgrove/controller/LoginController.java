package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller class for handling requests to the login page
 * Actual login functionality is handled by Spring Security in /config/SecurityConfig.java
 */
@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    /**
     * Handles GET requests to the "/login" endpoint.
     * Renders the login page.
     *
     * @return The name of the login page template to be rendered.
     */
    @GetMapping(value = "/login")
    public String login(HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        LOG.info("/login");

        // Set default values for `user` and `isUserVerified` to avoid Thymeleaf parsing issues
        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);

        // Add redirect attributes if available
        model.addAttribute(redirectAttributes.getFlashAttributes());

        // If the session variables have the error attribute
        if (request.getSession().getAttribute("error") != null) {
            String errorType = request.getSession().getAttribute("error").toString();
            model.addAttribute("error", errorType);
            if ("accountDisabled".equals(errorType)) {
                model.addAttribute(
                        "accountDisabledDate",
                        request.getSession().getAttribute("accountDisabledDate")
                );
            }
        }

        return "loginTemplate";
    }



}
