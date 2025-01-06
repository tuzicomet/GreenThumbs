package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Integration tests to check that User Information Validators act as expected,
 * and use the exact text expected for their error messages.
 */
@SpringBootTest
class UserInformationValidatorErrorMessageTests {

    @Autowired
    private MessageSource messageSource;

    @Mock
    private Model model;

    private Locale locale;

    @BeforeEach
    void setUp() {
        // Set locale to english (will only be testing the english error messages)
        locale = Locale.ENGLISH;
    }

    // First Name error message tests
    @Test
    void validateFirstName_FirstNameIsEmpty_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.firstNameValidation("", model, true, messageSource, locale));
        // Error message expected to be used, as specified by the ACs
        String expectedMessage = "First name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        // Check that the addAttribute method on the model is called with the firstNameError
        // attribute, and that the error message given with it is the exact one we expect
        verify(model).addAttribute("firstNameError", expectedMessage);
    }

    @Test
    void validateFirstName_FirstNameIsTooLong_CorrectErrorMessageGiven() {
        // instead of making a very long string, can just do "a".repeat(65), which ensures
        // it is the exact length we want
        assertTrue(UserInformationValidator.firstNameValidation("a".repeat(65), model, true, messageSource, locale));
        String expectedMessage = "First name must be 64 characters long or less";
        verify(model).addAttribute("firstNameError", expectedMessage);
    }

    @Test
    void validateFirstName_FirstNameHasNumbers_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.firstNameValidation("John123", model, true, messageSource, locale));
        String expectedMessage = "First name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        verify(model).addAttribute(eq("firstNameError"), eq(expectedMessage));
    }

    @Test
    void validateFirstName_FirstNameHasIllegalSpecialCharacters_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.firstNameValidation("John4$$", model, true, messageSource, locale));
        String expectedMessage = "First name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        verify(model).addAttribute(eq("firstNameError"), eq(expectedMessage));
    }

    // Last Name error message tests
    @Test
    void validateLastName_LastNameIsEmpty_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.lastNameValidation("", model, true, messageSource, locale));
        String expectedMessage = "Last name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        verify(model).addAttribute("lastNameError", expectedMessage);
    }

    @Test
    void validateLastName_LastNameIsTooLong_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.lastNameValidation("a".repeat(65), model, true, messageSource, locale));
        String expectedMessage = "Last name must be 64 characters long or less";
        verify(model).addAttribute("lastNameError", expectedMessage);
    }

    @Test
    void validateLastName_LastNameHasNumbers_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.lastNameValidation("Doe45", model, true, messageSource, locale));
        String expectedMessage = "Last name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        verify(model).addAttribute(eq("lastNameError"), eq(expectedMessage));
    }

    @Test
    void validateLastName_LastNameHasIllegalSpecialCharacters_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.lastNameValidation("Doe^", model, true, messageSource, locale));
        String expectedMessage = "Last name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter";
        verify(model).addAttribute(eq("lastNameError"), eq(expectedMessage));
    }

    // Email error message tests
    @Test
    void validateEmail_EmailNotInValidFormat_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.emailValidation("john.doe", model, messageSource, locale));
        String expectedMessage = "Email address must be in the form 'jane@doe.nz'";
        verify(model).addAttribute("emailError", expectedMessage);
    }

    // Date of Birth error message tests
    @Test
    void validateDateOfBirth_DateNotInValidFormat_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.dobValidation("31021990", model, messageSource, locale));
        String expectedMessage = "Date is not in valid format, (DD/MM/YYYY)";
        verify(model).addAttribute("dobError", expectedMessage);
    }

    @Test
    void validateDateOfBirth_DateOfBirthUnder13_CorrectErrorMessageGiven() {
        String underageDob = LocalDate.now().minusYears(12).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertTrue(UserInformationValidator.dobValidation(underageDob, model, messageSource, locale));
        String expectedMessage = "You must be 13 years or older to create an account";
        verify(model).addAttribute("dobError", expectedMessage);
    }

    // Passwords Match error message tests
    @Test
    void validatePasswordsMatch_Mismatch_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.passwordsMatch("password", "Password", model, messageSource, locale));
        String expectedMessage = "Passwords do not match";
        verify(model).addAttribute("passwordError", expectedMessage);
    }

    // Password Strength error message tests
    @Test
    void validatePasswordStrength_WeakPassword_CorrectErrorMessageGiven() {
        assertTrue(UserInformationValidator.validatePasswordStrength("weak", model, messageSource, locale));
        String expectedMessage = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character";
        verify(model).addAttribute("passwordStrengthError", expectedMessage);
    }
}