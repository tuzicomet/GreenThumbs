/**
 * A function which clears the stored login inputs (email only, password is not stored)
 * from sessionStorage
 */
function clearLoginInputsFromStorage() {
    sessionStorage.removeItem("savedEmail");
}

clearLoginInputsFromStorage()