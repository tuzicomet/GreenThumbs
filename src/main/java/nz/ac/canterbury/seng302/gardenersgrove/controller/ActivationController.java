package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * Controller for the account activation page, which handles getting the page, and submitting an activation token.
 */
@Controller
public class ActivationController {
    private static final Logger LOG = LoggerFactory.getLogger(ActivationController.class);
    private final VerificationTokenService verificationTokenService;
    private final UserService userService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    public ActivationController(VerificationTokenService verificationTokenService, UserService userService) {
        this.verificationTokenService = verificationTokenService;
        this.userService = userService;
    }

    /**
     * Gets the account activation form page.
     * @param model representation of attributes to be passed through thymeleaf
     * @param user the user that is currently logged in, and needing to be activated
     * @return the activate page template
     */
    @GetMapping(value = "/activate")
    public String activate(Model model, @ModelAttribute("user") AbstractUser user) {
        LOG.info("/activate");

        LOG.info("Return activate");

        model.addAttribute("user", user);
        model.addAttribute("isUserVerified", user.isEnabled());
        model.addAttribute("wrongToken", false);
        model.addAttribute("expiredToken", false);
        model.addAttribute("userEmail", user.getEmail());
        return "activateTemplate";
    }

    /**
     *
     * @param token the token that has been entered into the form.
     * @param userEmail the email of the user that is currently logged in, and needing to be activated
     * @param model representation of attributes to be passed through thymeleaf
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return redirection to the appropriate page if successful, otherwise return the activation form with a flag for token error
     */
    @PostMapping("/activate")
    public String submitForm(@RequestParam(name = "token", defaultValue = "") String token,
                             @RequestParam(name = "userEmail") String userEmail,
                             Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        AbstractUser user = userService.getUserByEmail(userEmail);
        VerificationToken tokenObject = verificationTokenService.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("isUserVerified", user.isEnabled());
        if(tokenObject == null || tokenObject.getExpiryDate().getTime() < System.currentTimeMillis()){
            model.addAttribute("userEmail", userEmail);
            model.addAttribute("tokenError", true);
            return "activateTemplate";
        }
        String correctToken = tokenObject.getToken();

        if(token.strip().equals(correctToken)){
            //if the token is correct, enable the user and delete the token.
            userService.enableUser(user.getUserId());
            verificationTokenService.deleteVerificationToken(tokenObject);

            // Get the no account activated message and add it as a flash attribute
            String accountActivatedMessage = messageSource.getMessage("message.accountActivated", null, locale);
            redirectAttributes.addFlashAttribute("message", accountActivatedMessage);
            return "redirect:/login";
        } else{
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("tokenError", true);
            return "activateTemplate";
        }
    }
}
