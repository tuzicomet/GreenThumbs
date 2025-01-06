package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

@Component
public class UserInformationValidator {
    private final UserService userService;
    public static final String NAME_REGEX = "(?=.*[\\p{L}\\p{M}].*)[\\p{L}\\p{M} \\-']+";
    public static final String EMAIL_REGEX = "[A-Za-z\\d]+([._\\-][A-Za-z\\d]+)*+@[A-Za-z\\d\\-]+(\\.[A-Za-z\\d\\-]+){0,100}\\.[A-Za-z]{2,}";
    public static final String PASSWORD_REGEX = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_])(?=\\S+$).{8,}";

    @Autowired
    public UserInformationValidator(UserService userService) {
        this.userService = userService;
    }

    /**
     * Check's whether the first name is valid as determined by the given AC's
     *
     * @param firstName The given first name that is being validated
     * @param model The model of the thymeleaf page for errors.
     * @param reg Boolean indicating whether the request came from the registration page.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean firstNameValidation(String firstName, Model model, Boolean reg,
                                              MessageSource messageSource, Locale locale) {
        boolean hasError = false;
        if (reg) {
            if (firstName.isEmpty() || firstName.isBlank() || !firstName.matches(NAME_REGEX)) {
                String errorMessage = messageSource.getMessage("error.firstNameEmptyAndInvalidChars", null, locale);
                model.addAttribute("firstNameError", errorMessage);
                hasError = true;
            }
        } else {
            if (firstName.isEmpty() || firstName.isBlank()) {
                String errorMessage = messageSource.getMessage("error.firstNameEmpty", null, locale);
                model.addAttribute("firstNameError", errorMessage);
                hasError = true;
            } else if (!firstName.matches(NAME_REGEX)) {
                String errorMessage = messageSource.getMessage("error.firstNameInvalidChars", null, locale);
                model.addAttribute("firstNameError", errorMessage);
                hasError = true;
            }
        }

        if (firstName.length() > 64) {
            String errorMessage = messageSource.getMessage("error.firstNameTooLong", null, locale);
            model.addAttribute("firstNameError", errorMessage);
            hasError = true;
        }
        return hasError;

    }

    /**
     * Check's whether the last name is valid as determined by the given AC's
     *
     * @param lastName The given last name that is being validated
     * @param model The model of the thymeleaf page for errors.
     * @param reg Boolean indicating whether the request came from the registration page.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean lastNameValidation(String lastName, Model model, Boolean reg,
                                             MessageSource messageSource, Locale locale) {
        boolean hasError = false;
        if (reg) {
            if (lastName.isEmpty() || lastName.isBlank() || !lastName.matches(NAME_REGEX)) {
                String errorMessage = messageSource.getMessage("error.lastNameEmptyAndInvalidChars", null, locale);
                model.addAttribute("lastNameError", errorMessage);
                hasError = true;
            }
        } else {
            if (lastName.isEmpty() || lastName.isBlank()) {
                String errorMessage = messageSource.getMessage("error.lastNameEmpty", null, locale);
                model.addAttribute("lastNameError", errorMessage);
                hasError = true;
            } else if (!lastName.matches(NAME_REGEX)) {
                String errorMessage = messageSource.getMessage("error.lastNameInvalidChars", null, locale);
                model.addAttribute("lastNameError", errorMessage);
                hasError = true;
            }
        }

        if (lastName.length() > 64) {
            String errorMessage = messageSource.getMessage("error.lastNameTooLong", null, locale);
            model.addAttribute("lastNameError", errorMessage);
            hasError = true;
        }
        return hasError;
    }

    /**
     * Check's whether the email is valid as determined by the given AC's
     *
     * @param email The given email that is being validated
     * @param model The model of the thymeleaf page for errors.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean emailValidation(String email, Model model,
                                          MessageSource messageSource, Locale locale) {
        if (email.length() > 255) {
            String errorMessage = messageSource.getMessage("error.emailTooLong", null, locale);
            model.addAttribute("emailError", errorMessage);
            return true;
        }
        if (!email.matches(EMAIL_REGEX)) {
            String errorMessage = messageSource.getMessage("error.emailFormat", null, locale);
            model.addAttribute("emailError", errorMessage);
            return true;
        }
        return false;
    }

    /**
     * Check's whether the date of birth is valid as determined by the given AC's
     *
     * @param dateOfBirth The given date of birth that is being validated
     * @param model The model of the thymeleaf page for errors.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean dobValidation(String dateOfBirth, Model model,
                                        MessageSource messageSource, Locale locale) {

        // if Date of birth is either null or empty, then allow it
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return false;
        }

        // Check the format explicitly before parsing
        if (!dateOfBirth.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String errorMessage = messageSource.getMessage("error.dateFormat", null, locale);
            model.addAttribute("dobError", errorMessage);
            return true;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        SimpleDateFormat invalidDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        LocalDate dob;
        try {
            dob = LocalDate.parse(dateOfBirth, formatter);
            // Makes sure a day like 31/9/23 doesn't roll over to 1/10/23, instead throws an error.
            invalidDateFormatter.setLenient(false);
            invalidDateFormatter.parse(dateOfBirth);
        } catch (DateTimeParseException e) { // Catches completely invalid dates
            String errorMessage = messageSource.getMessage("error.dateFormat", null, locale);
            model.addAttribute("dobError", errorMessage);
            return true;

        } catch (ParseException e) { // Catches roll-over dates, i.e. 31/9/23
            String errorMessage = messageSource.getMessage("error.dateDoesNotExist", null, locale);
            model.addAttribute("dobError", errorMessage);
            return true;
        }

        LocalDate today = LocalDate.now();
        Period period = Period.between(dob, today);
        int age = period.getYears();

        if (age < 13) {
            String errorMessage = messageSource.getMessage("error.notOldEnough", null, locale);
            model.addAttribute("dobError", errorMessage);
            return true;
        }
        else if (age >= 121) {
            String errorMessage = messageSource.getMessage("error.tooOld", null, locale);
            model.addAttribute("dobError", errorMessage);
            return true;
        }

        return false;
    }


    /**
     * Check's whether the passwords match.
     *
     * @param password The given password
     * @param confirmPassword The given confirm password
     * @param model The model for the thymeleaf page.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean passwordsMatch(String password, String confirmPassword, Model model,
                                         MessageSource messageSource, Locale locale) {
        if (password == null || !password.equals(confirmPassword)) {
            String errorMessage = messageSource.getMessage("error.passwordDontMatch", null, locale);
            model.addAttribute("passwordError", errorMessage);
            return true;
        }
        return false;
    }

    /**
     * Check's whether the password is strong enough.
     *
     * @param password The given password that is being validated
     * @param model The model of the thymeleaf page for errors.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public static boolean validatePasswordStrength(String password, Model model,
                                                   MessageSource messageSource, Locale locale) {
        if (!password.matches(PASSWORD_REGEX)) {
            String errorMessage = messageSource.getMessage("error.passwordStrength", null, locale);
            model.addAttribute("passwordStrengthError", errorMessage);
            return true;
        }
        return false;
    }

    /**
     * Check's whether the email exists within the database.
     *
     * @param email The given email that is being validated
     * @param current The current email
     * @param model The model of the thymeleaf page for errors.
     * @param messageSource MessageSource for retrieving error messages.
     * @param locale The locale to be used for localization of error messages.
     * @return boolean indicating whether there is an error.
     */
    public boolean validateEmailExists(String email, Model model, String current,
                                       MessageSource messageSource, Locale locale) {

        if (current != null) {
            if (userService.getUserByEmail(email) != null && !Objects.equals(email, current)) {
                String errorMessage = messageSource.getMessage("error.emailInUse", null, locale);
                model.addAttribute("emailError", errorMessage);
                return true;
            }
        } else {
            if (userService.getUserByEmail(email) != null) {
                String errorMessage = messageSource.getMessage("error.emailInUse", null, locale);
                model.addAttribute("emailError", errorMessage);
                return true;
            }
        }
        return false;
    }

}
