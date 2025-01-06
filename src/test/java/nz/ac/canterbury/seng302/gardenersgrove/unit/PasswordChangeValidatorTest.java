package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.validation.PasswordChangeValidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordChangeValidatorTest {

    User testUserFull = new User("test", "user", "test@gmail.com", "Password1@", "09/05/2003", null);
    User testUserWithNulls = new User("test", "", "test@gmail.com", "Password1@", null, null);

    @Test
    void validatePassword_ValidPassword_isValid(){
        assertTrue(PasswordChangeValidator.isValidPassword("JhgkjhGJHgG11#@", testUserFull));
        assertTrue(PasswordChangeValidator.isValidPassword("JhgkjhGJHgG11#@", testUserWithNulls));
    }
    @Test
    void validatePassword_ContainsFirstName_isInvalid(){
        assertFalse(PasswordChangeValidator.isValidPassword("JhgkjtestgG11#@", testUserFull));
        assertFalse(PasswordChangeValidator.isValidPassword("JhgkjtestgG11#@", testUserWithNulls));
    }
    @Test
    void validatePassword_ContainsLastName_isInvalidWhereLastNameExists(){
        assertFalse(PasswordChangeValidator.isValidPassword("JhgkjusergG11#@", testUserFull));
        assertTrue(PasswordChangeValidator.isValidPassword("JhgkjusergG11#@", testUserWithNulls));
    }
    @Test
    void validatePassword_ContainsDOB_isInvalidWhereDOBExists(){
        assertFalse(PasswordChangeValidator.isValidPassword("09/05/2003gG11#@", testUserFull));
        assertTrue(PasswordChangeValidator.isValidPassword("09/05/2003gG11#@", testUserWithNulls));
    }
    @Test
    void validatePassword_TooShort_isInvalid(){
        assertFalse(PasswordChangeValidator.isValidPassword("Dsaf@1", testUserFull));
        assertFalse(PasswordChangeValidator.isValidPassword("Dsaf@1", testUserWithNulls));
    }
    @Test
    void validatePassword_NoCaps_isInvalid(){
        assertFalse(PasswordChangeValidator.isValidPassword("password1@", testUserFull));
        assertFalse(PasswordChangeValidator.isValidPassword("password1@", testUserWithNulls));
    }
    @Test
    void validatePassword_NoSpecialCharacters_isInvalid(){
        assertFalse(PasswordChangeValidator.isValidPassword("Password11", testUserFull));
        assertFalse(PasswordChangeValidator.isValidPassword("Password11", testUserWithNulls));
    }
    @Test
    void validatePassword_Numbers_isInvalid(){
        assertFalse(PasswordChangeValidator.isValidPassword("Password#@", testUserFull));
        assertFalse(PasswordChangeValidator.isValidPassword("Password#@", testUserWithNulls));
    }
}
