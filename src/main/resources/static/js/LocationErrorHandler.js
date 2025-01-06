/**
 * Show an error message pop-up for the location field if an error exists.
 */
function showLocationErrors() {
    const locationError = document.getElementById('locationError')?.value;

    if (locationError && locationError !== "") {
        setStyleInvalid(document.getElementById('location'))
        showErrorPopUp(document.getElementById('location'), locationError);
    } else {
        setStyleDefault(document.getElementById('location'))
    }
}

