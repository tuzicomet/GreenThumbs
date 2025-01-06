package nz.ac.canterbury.seng302.gardenersgrove.validation;

import nz.ac.canterbury.seng302.gardenersgrove.entity.AbstractUser;

import java.util.regex.Pattern;

public class PasswordChangeValidator {
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern LOWER_CASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPER_CASE_PATTERN = Pattern.compile("[A-Z]");

    /**
     * Overwrites the default constructor, avoids an illegal state where FormValidation is initialized.
     */
    private PasswordChangeValidator() {
        throw new IllegalStateException("PasswordChangeValidator is a static class. It should never be initialized.");
    }

    /**
    * Checks if the provided password is valid according to the following rules:
    * The password must not contain the user's first name, last name, email, or date of birth.
    * The password must be at least 8 characters long.
    * The password must contain at least one special character.
    * The password must contain at least one digit.
    * The password must contain at least one lowercase letter.
    * The password must contain at least one uppercase letter.
    * @param password the password to validate
    * @param user the user for whom the password is being validated
    * @return true if the password is valid, false otherwise
    */
    public static boolean isValidPassword(String password, AbstractUser user) {
        String checkPassword = password.toLowerCase();
        String firstName = user.getFirstName().toLowerCase();
        String lastName = user.getLastName().toLowerCase();
        String email = user.getEmail().toLowerCase();
        String dateOfBirth = user.getDateOfBirth();
    
        if (dateOfBirth == null) {
            if (checkPassword.contains(firstName) ||
                    (checkPassword.contains(lastName) && !lastName.isEmpty()) ||
                checkPassword.contains(email)) {
                return false;
            }
        } else {
            if (checkPassword.contains(firstName) ||
                checkPassword.contains(lastName) ||
                checkPassword.contains(email) ||
                checkPassword.contains(dateOfBirth)) {
                return false;
            }
        }

        if (password.length() < 8) {
            return false;
        }

        boolean hasSpecialChar = SPECIAL_CHAR_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasLowerCase = LOWER_CASE_PATTERN.matcher(password).find();
        boolean hasUpperCase = UPPER_CASE_PATTERN.matcher(password).find();
        return hasSpecialChar && hasDigit && hasLowerCase && hasUpperCase;
    }
}