/**
 * Sets the style of the given input field to be invalid
 * @param input the field to update the style for
 */
function setStyleInvalid(input) {
    input.classList.remove("isValid");
    input.classList.add("hasError");
}

/**
 * Sets the style of the given input field to be valid
 * @param input the field to update the style for
 */
function setStyleValid(input) {
    input.classList.remove("hasError");
    input.classList.add("isValid");
}

/**
 * Sets the style of the given input field to be default
 * @param input the field to update the style for
 */
function setStyleDefault(input) {
    input.classList.remove("hasError");
    input.classList.remove("isValid");
}

/**
 * Shows the given message in an error pop-up on the given input element
 * @param inputElement the input element to add the pop-up to
 * @param errorMessage the message to show in the pop-up
 */
function showErrorPopUp(inputElement, errorMessage) {
    if (!inputElement) return;
    removeErrorPopUp(inputElement)
    const errorSpan = document.createElement("span");
    errorSpan.classList.add("error-popup");
    errorSpan.innerText = errorMessage;
    inputElement.parentNode.insertBefore(errorSpan, inputElement.nextSibling);
    errorSpan.style.top = inputElement.offsetTop + inputElement.offsetHeight + "px";
    errorSpan.style.left = inputElement.offsetLeft + "px";
    errorSpan.style.maxWidth = inputElement.getBoundingClientRect().width + "px";
}

/**
 * Removes the pop-up error message from the given input element
 * @param inputElement the input element to remove the error pop-up from
 */
function removeErrorPopUp(inputElement) {
    const errorSpan = inputElement.nextSibling;
    if (errorSpan && errorSpan.classList && errorSpan.classList.contains('error-popup')) {
        errorSpan.parentNode.removeChild(errorSpan);
    }
}