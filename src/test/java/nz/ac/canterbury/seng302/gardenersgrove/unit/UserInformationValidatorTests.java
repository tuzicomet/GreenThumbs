package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.validation.UserInformationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Very long and extensive test on the basic validators that do not require much mocking.
 * it's important to note that the validators return true if they fail, as that is how they are
 * implemented. Additionally, these tests do not check that the correct error message text is
 * used, only that the error message exists when and where it's supposed to.
 */
class UserInformationValidatorTests {

    @Mock
    private Model model;

    @Mock
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // If trying to get any message from messageSource, always return some example text
        // (Different integration test classes are used in order to test actual message text)
        when(messageSource.getMessage(any(), isNull(), eq(Locale.US)))
                .thenReturn("Error Message Text");
    }

    // FirstName Validation Tests
    @Test
    void firstNameValidationEmpty() {
        assertTrue(UserInformationValidator.firstNameValidation("", model, true, messageSource, Locale.US));
        // Verify that the corresponding model attribute is set with an error message
        verify(model).addAttribute(eq("firstNameError"), anyString());
    }

    @Test
    void firstNameValidationTooLong() {
        assertTrue(UserInformationValidator.firstNameValidation("a".repeat(65), model, true, messageSource, Locale.US));
        verify(model).addAttribute(eq("firstNameError"), anyString());
    }

    @Test
    void firstNameValidationValid() {
        assertFalse(UserInformationValidator.firstNameValidation("John", model, true, messageSource, Locale.US));
    }

    @Test
    void firstNameValidation_AccentedCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.firstNameValidation("Guðmundsdóttir", model, true, messageSource, Locale.US));
    }

    @Test
    void firstNameValidation_JapaneseCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.firstNameValidation("藤原美智子", model, true, messageSource, Locale.US));
    }

    @Test
    void firstNameValidation_CyrillicCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.firstNameValidation("Онтоман", model, true, messageSource, Locale.US));
    }

    @Test
    void firstNameValidationInvalidCharacters() {
        assertTrue(UserInformationValidator.firstNameValidation("John123", model, true, messageSource, Locale.US));
        verify(model).addAttribute(eq("firstNameError"), anyString());
    }

    // LastName Validation Tests
    @Test
    void lastNameValidationEmpty() {
        assertTrue(UserInformationValidator.lastNameValidation("", model, true, messageSource, Locale.US));
        verify(model).addAttribute(eq("lastNameError"), anyString());
    }

    @Test
    void lastNameValidationTooLong() {
        assertTrue(UserInformationValidator.lastNameValidation("a".repeat(65), model, true, messageSource, Locale.US));
        verify(model).addAttribute(eq("lastNameError"), anyString());
    }

    @Test
    void lastNameValidationValid() {
        assertFalse(UserInformationValidator.lastNameValidation("Doe", model, true, messageSource, Locale.US));
    }

    @Test
    void lastNameValidation_AccentedCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.lastNameValidation("Guðmundsdóttir", model, true, messageSource, Locale.US));
    }

    @Test
    void lastNameValidation_JapaneseCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.lastNameValidation("藤原美智子", model, true, messageSource, Locale.US));
    }

    @Test
    void lastNameValidation_CyrillicCharacters_ReturnsFalse() {
        assertFalse(UserInformationValidator.lastNameValidation("Онтоман", model, true, messageSource, Locale.US));
    }

    @Test
    void lastNameValidationInvalidCharacters() {
        assertTrue(UserInformationValidator.lastNameValidation("Doe$", model, true, messageSource, Locale.US));
        verify(model).addAttribute(eq("lastNameError"), anyString());
    }

    // Email Validation Tests
    @Test
    void emailValidationInvalidFormat() {
        assertTrue(UserInformationValidator.emailValidation("john.doe", model, messageSource, Locale.US));
        verify(model).addAttribute(eq("emailError"), anyString());
    }

    @Test
    void emailValidationValid() {
        assertFalse(UserInformationValidator.emailValidation("john.doe@example.com", model, messageSource, Locale.US));
    }

    // DOB Validation Tests
    @Test
    void dobValidationInvalidFormat() {
        assertTrue(UserInformationValidator.dobValidation("1990-10-02", model, messageSource, Locale.US));
        verify(model).addAttribute(eq("dobError"), anyString());
    }

    @Test
    void dobValidationNonexistentDate() {
        assertTrue(UserInformationValidator.dobValidation("31/02/1990", model, messageSource, Locale.US));
        verify(model).addAttribute(eq("dobError"), anyString());
    }

    @Test
    void dobValidationUnderage() {
        String underageDob = LocalDate.now().minusYears(12).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertTrue(UserInformationValidator.dobValidation(underageDob, model, messageSource, Locale.US));
        verify(model).addAttribute(eq("dobError"), anyString());
    }

    @Test
    void dobValidationValid() {
        String validDob = LocalDate.now().minusYears(20).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        assertFalse(UserInformationValidator.dobValidation(validDob, model, messageSource, Locale.US));
    }

    // Passwords Match Validation Tests
    @Test
    void passwordsMatchMismatch() {
        assertTrue(UserInformationValidator.passwordsMatch("password", "Password", model, messageSource, Locale.US));
        verify(model).addAttribute(eq("passwordError"), anyString());
    }

    @Test
    void passwordsMatchValid() {
        assertFalse(UserInformationValidator.passwordsMatch("password", "password", model, messageSource, Locale.US));
    }

    // Password Strength Validation Tests
    @Test
    void validatePasswordStrengthWeak() {
        assertTrue(UserInformationValidator.validatePasswordStrength("weak", model, messageSource, Locale.US));
        verify(model).addAttribute(eq("passwordStrengthError"), anyString());
    }

    @Test
    void validatePasswordStrengthValid() {
        assertFalse(UserInformationValidator.validatePasswordStrength("ValidPassword1!", model, messageSource, Locale.US));
    }
}
