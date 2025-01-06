/**
 * Validates the input in the name field and updates the style of the field accordingly
 */
function validateGardenName(){
    const nameField = document.getElementById('name');
    if (!nameField) return;
    const name = nameField.value.trim();
    const nameRegex = /^(?!\s+$)[\p{L}\p{M}\p{N} .,\-']{1,100}$/u;
    if (name === "" || !nameRegex.test(name)) {
        setStyleInvalid(nameField);
        return;
    }
    setStyleValid(nameField);
}

/**
 * Validates the input in the given location field and updates the style of the field accordingly
 * Allows the input to be empty
 */
function validateLocationField(field, isRequired){
    const locationRegex = /^(?!\s+$)[\p{L}\p{M}\p{N} .,'\-\/]{1,100}$/u;
    if (!field) return;
    const locationInput = field.value.trim();

    // Optional field - allowed to be empty, but if so, do not highlight
    if(!isRequired && locationInput === ""){
        setStyleDefault(field)
        return;
    }

    if (!locationRegex.test(locationInput)) {
        setStyleInvalid(field);
        return;
    }
    setStyleValid(field);
}

/**
 * Validates the input in the location postcode field and updates the style of the field accordingly
 */
function validateLocationPostcode(field){
    const postcodeRegex = /^[A-Za-z0-9\s-]{3,20}$/
    if (!field) return;
    const postcodeInput = field.value.trim();

    // Optional field - allowed to be empty, but if so, do not highlight
    if(postcodeInput === ""){
        setStyleDefault(field)
        return;
    }

    if (!postcodeRegex.test(postcodeInput)) {
        setStyleInvalid(field);
        return;
    }

    setStyleValid(field);
}

/**
 * Validation for the street field on creating a garden.
 * @param field the field to update the style for
 * @param isRequired whether it is required
 */
function validateStreetField(field, isRequired){
    const streetRegex = /^(?!\s+$)[0-9A-Za-zÀ-ÖØ-ÿ .,'\/\-]+$/u;
    if (!field) return;
    const locationInput = field.value.trim();

    // Optional field - allowed to be empty, but if so, do not highlight
    if(!isRequired && locationInput === ""){
        setStyleDefault(field)
        return;
    }

    if (!streetRegex.test(locationInput)) {
        setStyleInvalid(field);
        return;
    }
    setStyleValid(field);
}

/**
* Validates every location field
*/
function validateFullLocation(){
    validateLocationPostcode(document.getElementById("postcode"));
    validateLocationField(document.getElementById("city"), true);
    validateLocationField(document.getElementById("country"), true);
    validateLocationField(document.getElementById("suburb"), false);
    validateStreetField(document.getElementById("street"), false);
}

/**
 * Checks if the input is no more than 255 characters, matches the expected syntax, and is no greater than the size of the earth.
 * updates the style of the size field accordingly
 */
function validateSize(){
    const sizeField = document.getElementById("size");
    const input = sizeField.value;
    const sizeRegex = /^[0-9]+([.,][0-9]+)?$/;

    // Optional field - allowed to be empty, but if so, do not highlight
    if(input === ""){
        setStyleDefault(sizeField);
        return;
    }

    if(!sizeRegex.test(input)){
        setStyleInvalid(sizeField);
        return;
    }

    if(input.length>10){
        setStyleInvalid(sizeField);
        return;
    }

    // replace is required because we allow commas as decimal points, and parseFloat will not parse this format correctly
    const inputAsDecimal = input.replace(/,/g, '.')
    if(parseFloat(inputAsDecimal) < 0.1){
        setStyleInvalid(sizeField);
        return;
    }
    setStyleValid(sizeField);
}
/**
 * Validates the input in the description field, and updates the style of the field accordingly
 */
function validateGardenDescription() {
    let description = document.getElementById('description');
    let pattern = /^(?=.*\p{L}.*)([\s\S]{0,512}|)$/su;
    const currentLength = description.value.length;

    // Optional field - allowed to be empty, but if so, do not highlight
    if (description.value === "") {
        setStyleDefault(description)
        return;
    }

    if (currentLength === 0) {
        setStyleDefault(description);
        return;
    }

    if(!pattern.test(description.value)){
        setStyleInvalid(description);
        return;
    }

    setStyleValid(description);
    return;
}

/**
 * Function to highlight errors that come from the
 * back-end validation. The error messages themselves are already handled.
 * Currently, only the description field needs this, to catch the profanity error.
 */
function highlightBackEndErrors() {
    const descriptionError = document.getElementById('descriptionError')?.value;
    if (descriptionError && descriptionError !== "") {
        setStyleInvalid(document.getElementById("description"));
    }
}

/**
 * Validates all input fields
 */
function validateAllGarden() {
    validateGardenName();
    validateFullLocation();
    validateSize();
    validateGardenDescription();
}
/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllGardenErrors() {
    const nameError = document.getElementById('nameError')?.value;
    const countryError = document.getElementById('countryError')?.value;
    const cityError = document.getElementById("cityError")?.value;
    const suburbError = document.getElementById("suburbError")?.value;
    const streetError = document.getElementById("streetError")?.value;
    const postcodeError = document.getElementById("postcodeError")?.value;
    const sizeError = document.getElementById('sizeError')?.value;
    const descriptionError = document.getElementById('descriptionError')?.value;

    if (nameError && nameError !== "") {
        showErrorPopUp(document.getElementById('name'), nameError);
    }
    if (countryError && countryError !== "") {
        showErrorPopUp(document.getElementById('country'), countryError);
    }
    if (cityError && cityError !== "") {
        showErrorPopUp(document.getElementById('city'), cityError);
    }
    if (suburbError && suburbError !== "") {
        showErrorPopUp(document.getElementById('suburb'), suburbError);
    }
    if (streetError && streetError !== "") {
        showErrorPopUp(document.getElementById('street'), streetError);
    }
    if (postcodeError && postcodeError !== "") {
        showErrorPopUp(document.getElementById('postcode'), postcodeError);
    }
    if (sizeError && sizeError !== "") {
        showErrorPopUp(document.getElementById('size'), sizeError);
    }
    if (descriptionError && descriptionError !== "") {
        showErrorPopUp(document.getElementById('description'), descriptionError);
    }

    highlightBackEndErrors();
}