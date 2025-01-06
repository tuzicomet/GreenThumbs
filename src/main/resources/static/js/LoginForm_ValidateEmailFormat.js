/**
 * Javascript file adapted from User_ValidateDetails.js to handle error messages for only the email field on the
 * login page specifically. Main difference is that this version does not change the style of the input field,
 * and error messages are only shown when the submit button is pressed.
 * AllForms_ChangeValidityStyles.js is required alongside this script, for showErrorPopUp & removeErrorPopUp.
 */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginform');
    const emailField = document.getElementById('email');
    const emailError = document.getElementById('emailError')?.value;

    /**
     * Function which handles displaying/removing the email error message popup.
     */
    function handleEmailErrorDisplay() {
        if (!validateEmail()) { // If email is not valid
            // If there is no error message popup already, show it on the email field
            if (!document.querySelector('.error-popup')) {
                showErrorPopUp(emailField, emailError);
            }
        } else {
            removeErrorPopUp(emailField); // if email is valid, remove popup error message
        }
    }

    /**
     * Validates the Email field. Modified from User_ValidateDetails to not set style.
     * @returns {boolean} whether the Email field is valid.
     */
    function validateEmail() {
        const email = emailField.value.trim(); // Get the email value from the email field
        const emailPattern = /^(?=.{6,255}$)[A-Za-z0-9]+([._\-][A-Za-z0-9]+)*@[A-Za-z0-9\-]+(\.[A-Za-z0-9\-]+)*\.[A-Za-z]{2,}$/;
        return emailPattern.test(email) // Returns true if the email matches the pattern, false otherwise
    }

    // Event listener to check for the submit button being pressed
    form.addEventListener('submit', (event) => {
        // if email format is not valid, prevent the form from being submitted and display the error message
        if (!validateEmail()) {
            event.preventDefault();
            handleEmailErrorDisplay();
        }
    });
});
