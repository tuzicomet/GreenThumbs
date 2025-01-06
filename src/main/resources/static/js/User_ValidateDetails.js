/**
 * Validates the First Name field.
 * @returns {boolean} whether the First Name field is valid.
 */
function validateFirstName() {
    const firstNameField = document.getElementById('firstName');
    if (!firstNameField) return false;
    let pattern = /^(?=.*[\p{L}\p{M}].*)[\p{L}\p{M} \-']+$/u;
    const firstName = firstNameField.value.trim();

    if (firstName === "" || firstName.length > 64 || !pattern.test(firstName)) {
        setStyleInvalid(firstNameField);
        return false;
    }
    setStyleValid(firstNameField);
    return true;
}

/**
 * Validates the Last Name field.
 * @returns {boolean} whether the Last Name field is valid.
 */
function validateLastName() {
    const noSurnameCheckbox = document.getElementById('noSurname');
    const lastNameField = document.getElementById('lastName');
    if (!lastNameField) return false;

    let pattern = /^(?=.*[\p{L}\p{M}].*)[\p{L}\p{M} \-']+$/u
    const lastName = lastNameField.value.trim();

    if (noSurnameCheckbox && noSurnameCheckbox.checked) {
        setStyleDefault(lastNameField);
        return true;
    }

    if (lastName === "" || lastName.length > 64 || !pattern.test(lastName)) {
        setStyleInvalid(lastNameField);
        return false;
    }
    setStyleValid(lastNameField);
    return true;
}

/**
 * Validates the Email field.
 * @returns {boolean} whether the Email field is valid.
 */
function validateEmail() {
    const emailField = document.getElementById('email');
    if (!emailField) return false;
    const email = emailField.value.trim();
    if (email === "" || !/^(?=.{6,255}$)[A-Za-z0-9]+([._\-][A-Za-z0-9]+)*@[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,}$/.test(email)) {
        setStyleInvalid(emailField);
        return false;
    }
    setStyleValid(emailField);
    return true;
}

/**
 * Does the validation for the password field.
 */
function validatePassword() {
    const passwordField = document.getElementById('password');
    if (!passwordField) return false;
    const password = passwordField.value;

    const passwordPattern = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[\W_])(?=\S+$).{8,}$/;
    if (!passwordPattern.test(password)) {
        setStyleInvalid(passwordField);
        return false;
    } else {
        setStyleValid(passwordField);
        return true;
    }
}

/**
 * Validates that the two password fields are equal on the register user form.
 * If not, the confirmPassword field is set to invalid and the validity message is set.
 * @returns {boolean} whether or not the passwords are equal
 */
function validatePasswordsMatch() {
    const passwordField = document.getElementById('password');
    const confirmPasswordField = document.getElementById('confirmPassword');
    if (!passwordField || !confirmPasswordField) return false;

    if (confirmPasswordField.value === "") {
        setStyleInvalid(confirmPasswordField);
        return false;
    }
    if (passwordField.value !== confirmPasswordField.value) {
        setStyleInvalid(confirmPasswordField);
        return false;
    }
    setStyleValid(confirmPasswordField);
    return true;
}

/**
 * Validates that the date of birth is valid, setting the validity styling as necessary.
 * @returns {boolean} whether the date of birth is valid.
 */
function validateDateOfBirth() {
    const dateOfBirthField = document.getElementById('dateOfBirth');

    const DATE_REGEX = /^\d{2}\/\d{2}\/\d{4}$/;

    if (!dateOfBirthField) return false;

    const dateOfBirthValue = dateOfBirthField.value;

    // Check if the field is empty
    if (dateOfBirthValue === "") {
        setStyleDefault(dateOfBirthField);
        return true;
    }

    // Check if the date format is correct and if it is a valid date
    if (!DATE_REGEX.test(dateOfBirthValue)) {
        setStyleInvalid(dateOfBirthField);
        return false;
    }

    // Switches DD/MM/YYYY to yyyy-MM-dd before parsing, otherwise conversion issues
    // e.g. 01/01/0000 gets parsed as 01/01/2000. I love Javascript!!!!!!!!!!!!
    const dateFormatted = dateOfBirthValue.split('/').reverse().join('-')
    const dateOfBirth = new Date(dateFormatted);

    // If the date is invalid (i.e. nonexistent month)
    if (isNaN(dateOfBirth.getTime())) {
        setStyleInvalid(dateOfBirthField);
        return false;
    }

    // Handle highlighting for invalid dates overflowing to the next month
    // 31/04/2000 turns into 31/05/2000 when parsed, below logic detect this overflow.
    const splitDate = dateOfBirthValue.split('/');
    const month = parseInt(splitDate[1], 10) - 1; // months (but not days) start at 0. JavaScript goated moment
    if (dateOfBirth.getMonth() !== month){
        setStyleInvalid(dateOfBirthField);
        return false;
    }

    // Ensure the date of birth is within valid range
    const today = new Date();
    const minValidDate = new Date(today.getFullYear() - 121, today.getMonth(), today.getDate() + 1);
    const maxValidDate = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate() + 1);

    if (dateOfBirth > today || dateOfBirth > maxValidDate || dateOfBirth < minValidDate) {
        setStyleInvalid(dateOfBirthField);
        return false;
    }

    setStyleValid(dateOfBirthField);
    return true;
}

/**
 * Validates all input fields
 */
function validateAll() {
    validateFirstName();
    validateLastName();
    validateEmail();
    validatePassword();
    validatePasswordsMatch();
    validateDateOfBirth();
}

/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllErrors() {
    const firstNameError = document.getElementById('firstNameError')?.value;
    const lastNameError = document.getElementById('lastNameError')?.value;
    const emailError = document.getElementById('emailError')?.value;
    const passwordStrengthError = document.getElementById('passwordStrengthError')?.value;
    const passwordError = document.getElementById('passwordError')?.value;
    const dobError = document.getElementById('dobError')?.value;

    if (firstNameError) {
        showErrorPopUp(document.getElementById('firstName'), firstNameError);
    }
    if (lastNameError) {
        showErrorPopUp(document.getElementById('lastName'), lastNameError);
    }
    if (emailError) {
        showErrorPopUp(document.getElementById('email'), emailError);
    }
    if (passwordStrengthError) {
        showErrorPopUp(document.getElementById('password'), passwordStrengthError);
    }
    if (passwordError) {
        showErrorPopUp(document.getElementById('confirmPassword'), passwordError);
    }
    if (dobError) {
        showErrorPopUp(document.getElementById('dateOfBirth'), dobError);
    }
}

/**
 * Shows all of the possible password errors at once, is called at load in HTML file.
 */
function showPasswordChangeErrors(){
    const incorrectError = document.getElementById('incorrectError')?.value;
    const matchError = document.getElementById('matchError')?.value;
    const strengthError = document.getElementById('strengthError')?.value;

    if (incorrectError && incorrectError.endsWith("incorrect")) {
        showErrorPopUp(document.getElementById('oldPassword'), incorrectError);
    }
    if (matchError) {
        showErrorPopUp(document.getElementById('confirmPassword'), matchError);
    }
    if (strengthError) {
        showErrorPopUp(document.getElementById('password'), strengthError);
    }

}
