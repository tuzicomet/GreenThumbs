package nz.ac.canterbury.seng302.gardenersgrove.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator.*;

/**
 * Controller class for handling registration requests.
 * This controller manages displaying and submitting the registration form.
 */
@Controller
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);
    private final UserService userService;
    private final UserInformationValidator userInformationValidator;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private static AuthenticationManager authenticationManager;

    @Autowired
    private MessageSource messageSource;

    private final SecurityContextRepository securityContextRepository;
    private final MailService mailService;

    /**
     * Constructor for the RegistrationController class.
     * @param userService               The UserService instance for managing user-related operations.
     * @param userInformationValidator  Validator class containing methods to validate inputs
     * @param securityContextRepository Used for securitycontext persistence (eg remembering a user is authenticated)
     */
    @Autowired
    public RegisterController(UserService userService, UserInformationValidator userInformationValidator,
                              SecurityContextRepository securityContextRepository, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userInformationValidator = userInformationValidator;
        this.securityContextRepository = securityContextRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;

    }

    /**
     * Handles the GET request for the registration page.
     * Logs the request and response status.
     * Returns the view name for rendering the registration form.
     * @return The view name to be rendered.
     */
    @GetMapping(value = "/register")
    public String register(Model model) {
        LOG.info("/register");

        // Set default values for `user` and `isUserVerified`
        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);

        LOG.info("Return register");

        return "registerTemplate";
    }


    /**
     * Handles the POST request for canceling the registration process.
     * @return The view name to be rendered.
     */
    @PostMapping(value = "/register", params = "cancel")
    public String onCancel() {
        LOG.info("/homepage");

        LOG.info("Return home page");

        // return the user to the homepage
        return "homepageTemplate";
    }


    /**
     * Handles the registration form submission.
     * Validates form inputs and creates a new user if inputs are valid.
     * Redirects to the user's profile page upon successful registration.
     * @param firstName             The first name of the user.
     * @param lastName              The last name of the user (optional).
     * @param noSurname             Indicates if the user has no surname.
     * @param email                 The email address of the user.
     * @param password              The password chosen by the user.
     * @param confirmPassword       Confirmation of the chosen password.
     * @param dateOfBirth           The date of birth of the user.
     * @param model                 The Spring MVC model object for storing attributes.
     * @param request               The HTTP request object.
     * @param response              The HTTP response object.
     * @param redirectAttributes    The attributes for the text on the page.
     * @return                      The view name to be rendered.
     */
    @PostMapping(value = "/register", params = "submit")
    public String submitForm(@RequestParam(name = "firstName") String firstName,
                             @RequestParam(name = "lastName", required = false) String lastName,
                             @RequestParam(name = "noSurname", defaultValue = "false") boolean noSurname,
                             @RequestParam(name = "email") String email,
                             @RequestParam(name = "password") String password,
                             @RequestParam(name = "confirmPassword") String confirmPassword,
                             @RequestParam(name = "dateOfBirth", required = false) String dateOfBirth,
                             Model model, HttpServletRequest request, HttpServletResponse response,  RedirectAttributes redirectAttributes) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String todayStr = today.format(formatter);


        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);

        LOG.info("Form submitted");
        if (noSurname) {
            lastName = "";
        }

        // Check if date of birth is an empty string
        if (dateOfBirth.isEmpty()) {
            // if so, convert it to null (as empty string cannot be parsed to DATE in database)
            dateOfBirth = null;
        }
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        boolean firstNameError = firstNameValidation(firstName, model, true, messageSource, locale);
        boolean lastNameError = !noSurname && lastNameValidation(lastName, model, true, messageSource, locale);
        boolean emailError = emailValidation(email, model, messageSource, locale);
        boolean dobError = dobValidation(dateOfBirth, model, messageSource, locale);
        boolean passwordError = passwordsMatch(password, confirmPassword, model, messageSource, locale);
        boolean passwordStrengthError = validatePasswordStrength(password, model, messageSource, locale);
        boolean emailExistsError = userInformationValidator.validateEmailExists(email, model, null, messageSource, locale);

        // Adds attributes to the model, so registration form can be rendered with user data.
        model.addAttribute("noSurname", noSurname);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("noSurname", noSurname);
        model.addAttribute("email", email);
        model.addAttribute("today", todayStr);
        model.addAttribute("dateOfBirth", dateOfBirth);

        // If there were any errors, return the user back to the register page (with error messages)
        if (firstNameError || lastNameError || emailError || dobError ||
                passwordError || passwordStrengthError || emailExistsError) {
            return "registerTemplate";
        }

        // Replace user object with the entered values (and their password hashed)
        User user = new User(firstName, lastName, email.toLowerCase(),
                passwordEncoder.encode(password), dateOfBirth, null);

        // give the user the "ROLE_USER" authority
        user.grantAuthority("ROLE_USER");

        // add the user to the database
        userService.addUser(user);

        // Email the client with an email to confirm their registration and activate their account
        mailService.sendRegistrationEmail(user, locale);


        redirectAttributes.addFlashAttribute("user", user);
        return "redirect:/activate";
    }



}

