/**
 * Validates the input in the name field and updates the style of the field accordingly
 * @returns {boolean}true if the input is valid
 */
function validatePlantName(){
    const nameField = document.getElementById('name');
    if (!nameField) return false;
    const name = nameField.value.trim();
    const nameRegex = /^(?!\s+$)[0-9\p{L}\p{M} .,\-']{1,100}$/u;
    if (name === "" || !nameRegex.test(name)) {
        setStyleInvalid(nameField);
        return false;
    }
    setStyleValid(nameField);
    return true;
}
/**
 * Validates the input in the description field, and updates the style of the field accordingly
 */
function validateDescription() {
    let description = document.getElementById('description');
    let pattern = /^(?=.*\p{L}.*)([\s\S]{0,512}|)$/su;

    if(description.value === ""){
        setStyleDefault(description);
        return;
    }

    if(!pattern.test(description.value)){
        setStyleInvalid(description);
        return;
    }

    setStyleValid(description);
}

/**
 * Checks if the input is no more than 255 characters, matches the expected syntax, and is no greater than the size of the earth.
 * Updates the style depending on the detected validity
 */
function validateCount(){
    const countField = document.getElementById("count");
    const input = countField.value;
    const sizeRegex = /^[0-9]*?$/;

    if(input === ""){
        setStyleDefault(countField);
        return;
    }

    if(!sizeRegex.test(input)){
        setStyleInvalid(countField);
        return;
    }

    if(input.length>9){
        setStyleInvalid(countField);
        return;
    }

    if(input === "0"){
        setStyleInvalid(countField);
        return;
    }
    setStyleValid(countField);
}

/**
 * Validates the input in the date field, and updates the style accordingly
 */
function validateDate() {
    const dateField = document.getElementById('date');
    const DATE_REGEX = /^\d{2}\/\d{2}\/\d{4}$/;

    if (!dateField) return false;

    const dateValue = dateField.value;

    // Check if the field is empty
    if (dateValue === "") {
        setStyleDefault(dateField);
        return;
    }

    // Check if the date format is correct and if it is a valid date
    if (!DATE_REGEX.test(dateValue)) {
        setStyleInvalid(dateField);
        return;
    }

    // Switches DD/MM/YYYY to yyyy-MM-dd before parsing, otherwise conversion issues
    // e.g. 01/01/0000 gets parsed as 01/01/2000. I love Javascript!!!!!!!!!!!!
    const dateFormatted = dateValue.split('/').reverse().join('-')
    const date = new Date(dateFormatted);

    if (isNaN(date.getTime())) {
        setStyleInvalid(dateField);
        return;
    }

    // Handle highlighting for invalid dates overflowing to the next month
    // 31/04/2000 turns into 01/05/2000 when parsed, below logic detect this overflow.
    const splitDate = dateValue.split('/');
    const month = parseInt(splitDate[1], 10) - 1; // months (but not days) start at 0. JavaScript goated moment
    if (date.getMonth() !== month){
        setStyleInvalid(dateField);
        return;
    }

    const today = new Date();
    const minValidDate = new Date(today.getFullYear() - 120, today.getMonth(), today.getDate());

    // Ensure the date is within valid range
    if (date > today || date < minValidDate) {
        setStyleInvalid(dateField);
        return;
    }

    setStyleValid(dateField);
}

/**
 * Validates all input fields
 */
function validateAllPlant() {
    validatePlantName();
    validateDescription();
    validateCount();
    validateDate();
}
/**
 * Validate the provided file in the image field
 */
function validateImage(){
    const image = document.getElementById("image-select-listener")
    const error = document.getElementById("imageDynamicError")
    const allowedExtensions = ["image/jpeg", "image/png", "image/svg+xml"];
    const baseUrl = window.location.href.split('/garden')[0];
    const MB_SIZE = Math.pow(1024, 2)
    if(image.files.length > 0){
        const fileSize = image.files.item(0).size
        if(fileSize > MB_SIZE * 10){
            document.getElementById("image-select-display").src = baseUrl + document.getElementById("imagePath").value
            image.value = null
            error.innerText = "Image must be less than 10MB"
            return
        } else if(!allowedExtensions.includes(image.files.item(0).type)){
            document.getElementById("image-select-display").src = baseUrl + document.getElementById("imagePath").value
            image.value = null
            error.innerText = "Image must be of type png, jpg or svg"
            return
        }
    }
    error.innerText= ""

}

/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllPlantErrors() {
    const nameError = document.getElementById('nameError')?.value;
    const descriptionError = document.getElementById('descriptionError')?.value;
    const countError = document.getElementById('countError')?.value;
    const dateError = document.getElementById('dateError')?.value;

    if (nameError && nameError !== "") {
        showErrorPopUp(document.getElementById('name'), nameError);
    }
    if (descriptionError && descriptionError !== "") {
        showErrorPopUp(document.getElementById('description'), descriptionError);
    }
    if (countError && countError !== "") {
        showErrorPopUp(document.getElementById('count'), countError);
    }
    if (dateError && dateError !== "") {
        showErrorPopUp(document.getElementById('date'), dateError);
    }
}