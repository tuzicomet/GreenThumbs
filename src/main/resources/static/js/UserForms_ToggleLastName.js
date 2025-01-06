/**
 * Toggles the availability of the 'last name' field on user forms.
 */
let lastName ="";
function toggleLastNameField() {
    const checkBox = document.getElementById("noSurname");
    const lastNameInput = document.getElementById("lastName");

    // check if the elements exist first (i.e. are we on the edit page?)
    if (checkBox && lastNameInput) {
        if (lastNameInput.value) {
            lastName = lastNameInput.value;
        }
        if (checkBox.checked) {
            setStyleDefault(lastNameInput);
            lastNameInput.disabled = true;
            lastNameInput.value = "";
        } else {
            lastNameInput.disabled = false;
            lastNameInput.value = lastName
            validateLastName();
        }
    }
}
