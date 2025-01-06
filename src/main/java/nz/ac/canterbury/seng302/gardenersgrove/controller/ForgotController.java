package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

import static nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator.emailValidation;

@Controller
public class ForgotController {

    private static final Logger LOG = LoggerFactory.getLogger(ForgotController.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private MessageSource messageSource;

    public ForgotController(UserService userService, MailService mailService, VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.mailService = mailService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Handles get requests for the forgot page, will be called when the user
     * selects the forgot password link on the login page.
     *
     * @return thymeleaf forgot
     */
    @GetMapping(value = "/forgot")
    public String forgot(Model model) {
        LOG.info("/forgot");

        // Set default values for `user` and `isUserVerified`
        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);

        LOG.info("Return forgot");

        return "forgotTemplate";
    }


    /**
     * Sends an email to the provided address if it exists in the database.
     * Performs basic validation on the email and displays a message
     * whether it is successful or not.
     *
     * @param email    email of the user
     * @param model    representation of the email for thymeleaf
     * @return thymeleaf forgot
     */
    @PostMapping(value = "/forgot")
    public String submitForm(@RequestParam(name = "email") String email, Model model, HttpServletRequest request) {

        model.addAttribute("user", null);
        model.addAttribute("isUserVerified", false);
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        boolean emailError = emailValidation(email, model, messageSource, locale);
        model.addAttribute("email", email);

        if (emailError) {
            return "forgotTemplate";
        } else {
            AbstractUser user = userService.getUserByEmail(email);
            if (user != null && verificationTokenService.findByUser(user) == null) {
                String token = userService.createPasswordResetTokenForUser(user);

                // Email the user with a token to reset their password
                // Send locale to ensure that the email is in the correct language
                mailService.sendResetTokenEmail(email, token, locale);
            }
            String confirmationMessage = messageSource.getMessage("message.emailSentToAddress", null, locale);
            model.addAttribute("confirmation", confirmationMessage);
            return "forgotTemplate";
        }
    }



}