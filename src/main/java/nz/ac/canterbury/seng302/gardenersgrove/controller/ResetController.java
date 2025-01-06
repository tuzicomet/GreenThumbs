package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;
import nz.ac.canterbury.seng302.gardenersgrove.entity.VerificationToken;
import nz.ac.canterbury.seng302.gardenersgrove.service.MailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.service.VerificationTokenService;
import nz.ac.canterbury.seng302.gardenersgrove.utility.LocaleUtils;
import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.Map;

@Controller
public class ResetController {


    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private MessageSource messageSource;

    public ResetController(UserService userService, VerificationTokenService verificationTokenService, MailService mailService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
        this.mailService = mailService;
    }

    /**
     * Takes a generated token with an expiry time of 10 minutes in the url
     * uses this token to either allow or disallow the user from accessing the
     * reset password page.
     *
     * @param token              The token that is given in the url for expiry
     * @param model              Representation of the page for thymeleaf
     * @param redirectAttributes Attributes to pass to the page which the user is
     *                           being redirected to
     * @param request            HttpServletRequest object containing information
     *                           about the request.
     * @return thymeleaf reset or login pages
     */
    @GetMapping("/reset")
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        Model model, RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {

        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken != null && verificationToken.getExpiryDate().after(new Timestamp(System.currentTimeMillis()))) {
            model.addAttribute("token", token);
            return "resetTemplate";
        } else {
            // Get the 'Reset password link has expired' message and add it as a flash attribute
            String message = messageSource.getMessage("resetPassword.linkExpired", null, locale);
            redirectAttributes.addFlashAttribute("messageExpire", message);
            return "redirect:/login";
        }
    }


    /**
     * Takes the two passwords from the fields, performs validation checks on them
     * ensures the token has not expired. If the token has expired it sends a get
     * request to reset which then redirects back to login. If the token is valid
     * it updates the password and deletes the token taking you to the login page.
     *
     * @param params   two params indicating the update password and confirm
     * @param model    representation of the page for thymeleaf
     * @param request  HttpServletRequest object which contains information about the request.
     *                 Used here to get the locale from the request.
     * @return thymeleaf reset or login
     */
    @PostMapping("/reset")
    public String processResetPassword(@RequestParam Map<String, String> params, Model model,
                                       HttpServletRequest request) {
        // Get the current locale from the request
        Locale locale = LocaleUtils.getLocaleFromSession(request);

        String token = params.get("token");
        String newPassword = params.get("password");
        String confirmPassword = params.get("confirmPassword");
        boolean strongPasswordError = UserInformationValidator.validatePasswordStrength(newPassword, model, messageSource, locale);
        boolean matchPasswordError = UserInformationValidator.passwordsMatch(newPassword, confirmPassword, model, messageSource, locale);
        
        if (strongPasswordError || matchPasswordError) {
            return "resetTemplate";
        }

        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        if (verificationToken == null || verificationToken.getExpiryDate().before(new Timestamp(System.currentTimeMillis()))) {
            // Get the 'Invalid or expired token' message and add it to model
            String message = messageSource.getMessage("message.invalidToken", null, locale);
            model.addAttribute("message", message);
            return "resetTemplate";
        } else {
            AbstractUser user = verificationToken.getUser();
            userService.updatePassword(user.getUserId(), passwordEncoder.encode(newPassword));
            verificationTokenService.deleteVerificationToken(verificationToken);

            // Email the user a confirmation of their password reset
            // Send locale to ensure that the email is in the correct language
            mailService.sendConfirmationEmail(user.getEmail(), locale);

            return "redirect:/login?resetSuccess";
        }
    }



}