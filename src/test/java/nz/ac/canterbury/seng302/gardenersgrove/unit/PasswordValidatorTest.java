package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PasswordChangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordValidatorTest {
    
    private User testUser;
    
        @BeforeEach
        void setup() {
            testUser = new User("John", "Doe", "john.doe@example.com", "currentPassword", "1990-01-01", null);
        }
    
        @Test
        void testValidPassword() {
            String validPassword = "Abcd1234!";
            assertTrue(PasswordChangeValidator.isValidPassword(validPassword, testUser));
        }


        @ParameterizedTest
        @CsvSource({
                "Abcd1234!john",                    // contains first name
                "Abcd1234!doe",                     // contains last name
                "Abcd1234!john.doe@example.com",    // contains email
                "Abcd1234!1990-01-01",              // contains date of birth
                "Abc123!",                          // too short
                "Abcd1234",                         // no symbols
                "Abcd!@#$",                         // no digits
                "ABCD1234!",                        // no lowercase
                "abcd1234!"                         // no uppercase
        })
        void testInvalidPassword(String invalidPassword) {
            assertFalse(PasswordChangeValidator.isValidPassword(invalidPassword, testUser));
        }
}
