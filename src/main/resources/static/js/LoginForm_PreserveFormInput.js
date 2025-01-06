/**
 * Script to prevent input fields from being cleared after unsuccessful login attempts
 */
function preserveFormInput() {
    // Currently only email is preserved, as storing the password poses a security risk
    // Thus, passwords will still be cleared with each login attempt

    // The resource below was used for SessionStorage, used to save and load the inputs from
    // sessionStorage is preferred here over localStorage as it will only store
    // data temporarily, until the tab is closed, which is better for security.
    // https://www.geeksforgeeks.org/localstorage-and-sessionstorage-web-storage-apis/

    // Get the email input field
    var emailInput = document.getElementById("email");

    // Check if there are already saved values in localStorage
    var savedEmail = sessionStorage.getItem("savedEmail");
    // If saved values exist, then use them to pre-populate the input fields
    if (savedEmail) {
        emailInput.value = savedEmail;
    }

    // Each time either input field is updated, save its value to sessionStorage
    emailInput.addEventListener("change", function() {
        sessionStorage.setItem("savedEmail", emailInput.value);
    });
}

preserveFormInput();