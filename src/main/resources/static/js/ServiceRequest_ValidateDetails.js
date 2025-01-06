/**
 * The number of milliseconds in a year. Can be added to a date object to increase the year by one
 * @type {number}
 */
const MILLISECONDS_IN_YEAR = 1000 * 60 * 60 * 24 * 365

// Price must either be an integer, or a number followed by a . or , then two decimal places
const PRICE_REGEX = /^[0-9]+([.,][0-9]{1,2})?$/;

/**
 * Helper function to return a parsed date or set the style invalid if it cannot be parsed correctly.
 * @param dateField {HTMLInputElement} The HTML input field to get input from / set styling on
 */
function getParsedValueOfDateField(dateField) {
    const DATE_REGEX = /^\d{2}\/\d{2}\/\d{4}$/;

    if (!dateField) return;

    const dateMin = dateField.value;

    // Check if the date format is correct and if it is a valid date
    if (!DATE_REGEX.test(dateMin)) {
        setStyleInvalid(dateField);
        return;
    }

    // Switches DD/MM/YYYY to yyyy-MM-dd before parsing, otherwise conversion issues
    // e.g. 01/01/0000 gets parsed as 01/01/2000. I love Javascript!!!!!!!!!!!!
    const dateFormatted = dateMin.split('/').reverse().join('-')
    const date = new Date(dateFormatted);

    if (isNaN(date.getTime())) {
        setStyleInvalid(dateField);
        return;
    }

    // Handle highlighting for invalid dates overflowing to the next month
    // 31/04/2000 turns into 31/05/2000 when parsed, below logic detect this overflow.
    const splitDate = dateMin.split('/');
    const month = parseInt(splitDate[1], 10) - 1; // months (but not days) start at 0. JavaScript goated moment
    if (date.getMonth() !== month){
        setStyleInvalid(dateField);
        return;
    }

    return date;
}

/**
 * Validates the input in the title field and updates the style of the field accordingly
 */
function validateTitle(){
    const titleField = document.getElementById('title');
    if (!titleField) return;
    const title = titleField.value.trim();
    const titleRegex = /^(?=.*\p{L}.*)[\p{L}\p{M} \-']{1,32}$/u;
    if (title === "" || !titleRegex.test(title)) {
        setStyleInvalid(titleField);
        return;
    }
    setStyleValid(titleField);
}

/**
 * Validates the input in the description field and updates the style of the field accordingly
 */
function validateDescription(elementName) {
    let description = document.getElementById(elementName);
    if (!description) return;
    let pattern = /^(?=.*\p{L}.*)[\p{L}\p{M} \-']{1,512}$/u;

    if(!pattern.test(description.value)){
        setStyleInvalid(description);
        return;
    }

    setStyleValid(description);
}

/**
 * Compare two Dates to see if they relate to the same day
 * @param date1 {Date} first date to compare
 * @param date2 {Date} second date to compare
 * @returns {boolean} whether or not the two Date objects are within the same day
 */
function sameDay(date1, date2) {
    return date1.getDate() === date2.getDate() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getFullYear() === date2.getFullYear()
}

/**
 * Validates the input in the dateMin field and updates the style of the field accordingly
 */
function validateDateMin() {
    const dateMinField = document.getElementById('dateMin');
    const dateValue = getParsedValueOfDateField(dateMinField);
    const prevDateMinField = document.getElementById('prevDateMin');
    if (!dateValue) return;

    // If the field exists, we're on the edit form. Invalid inputs that are the same as last time are allowed.
    if (prevDateMinField !== null && prevDateMinField.value === dateMinField.value) {
        setStyleValid(dateMinField);
        return;
    }


    const today = new Date();

    // Ensure the date isn't in the past
    if (dateValue < today && !sameDay(today, dateValue)) {
        setStyleInvalid(dateMinField);
        return;
    }

    const oneYearAway = new Date(today.getTime() + MILLISECONDS_IN_YEAR);
    // Earliest date should be less than one year away. Second condition excludes the whole day exactly one year away.
    if (oneYearAway < dateValue || sameDay(oneYearAway, dateValue)) {
        setStyleInvalid(dateMinField);
        return;
    }

    setStyleValid(dateMinField);
}

/**
 * Validates the input in the dateMax field and updates the style of the field accordingly
 */
function validateDateMax() {
    const dateMaxField = document.getElementById('dateMax');
    const dateMaxValue = getParsedValueOfDateField(dateMaxField);
    if (!dateMaxValue) return;

    const dateMinField = document.getElementById('dateMin');
    const dateMinValue = getParsedValueOfDateField(dateMinField);

    const today = new Date();

    // Ensure the date isn't in the past
    if (dateMaxValue < today && !sameDay(today, dateMaxValue)) {
        setStyleInvalid(dateMaxField);
        return;
    }

    // Ensure the max date is greater than the min date
    if (dateMinValue > dateMaxValue) {
        setStyleInvalid(dateMaxField);
        return;
    }

    const oneYearAway = new Date(today.getTime() + MILLISECONDS_IN_YEAR);
    // Latest date should be less than one year away. Second condition excludes the whole day exactly one year away.
    if (oneYearAway < dateMaxValue || sameDay(oneYearAway, dateMaxValue)) {
        setStyleInvalid(dateMaxField);
        return;
    }

    setStyleValid(dateMaxField);
}

/**
 * Validates the input in the priceMin field and updates the style of the field accordingly
 */
function validatePriceMin() {
    // retrieve and format the minimum price value
    const priceMinField = document.getElementById('priceMin');
    const minValue = priceMinField.value.trim().replace(/,/g, '.');

    if (!minValue) {
        setStyleInvalid(priceMinField);
        return;
    }

    // Ensure the minimum value meets the price regex. If not, set the field style to invalid
    if (!PRICE_REGEX.test(minValue)) {
        setStyleInvalid(priceMinField);
        return;
    }

    // if there were no problems, set the field style to valid
    setStyleValid(priceMinField);
}

/**
 * Validates the input in the priceMax field and updates the style of the field accordingly
 */
function validatePriceMax() {
    // retrieve and format the minimum price value
    const priceMinField = document.getElementById('priceMin');
    const minValue = priceMinField.value.trim().replace(/,/g, '.');

    // retrieve and format the maximum price value
    const priceMaxField = document.getElementById('priceMax');
    const maxValue = priceMaxField.value.trim().replace(/,/g, '.');

    if (!maxValue) {
        setStyleInvalid(priceMaxField);
        return;
    }

    // Ensure the max date is greater than the min date
    if (!PRICE_REGEX.test(maxValue) || maxValue > 100000) {
        setStyleInvalid(priceMaxField);
        return;
    }

    const minFloat = parseFloat(minValue);
    const maxFloat = parseFloat(maxValue);

    // Check if the maximum price value is greater or equal to the minimum,
    // but only if the minimum value is also valid
    if (!isNaN(minFloat)) {
        if (maxFloat < minFloat) {
            setStyleInvalid(priceMaxField);
            return;
        }
    }

    setStyleValid(priceMaxField);
}

/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllRequestErrors() {
    const titleError = document.getElementById('errorTitle')?.value;
    const descriptionError = document.getElementById('errorDescription')?.value;
    const dateMinError = document.getElementById('errorDateMin')?.value;
    const dateMaxError = document.getElementById('errorDateMax')?.value;

    const priceMinError = document.getElementById('errorPriceMin')?.value;
    const priceMaxError = document.getElementById('errorPriceMax')?.value;

    const gardenError = document.getElementById('errorGarden')?.value;

    const imageError = document.getElementById('errorImage')?.value;

    if (titleError && titleError !== "") {
        showErrorPopUp(document.getElementById('title'), titleError);
    }
    if (descriptionError && descriptionError !== "") {
        showErrorPopUp(document.getElementById('description'), descriptionError);
    }
    if (dateMinError && dateMinError !== "") {
        showErrorPopUp(document.getElementById('dateMin'), dateMinError);
    }
    if (dateMaxError && dateMaxError !== "") {
        showErrorPopUp(document.getElementById('dateMax'), dateMaxError);
    }

    if (priceMinError && priceMinError !== "") {
        showErrorPopUp(document.getElementById('priceMin'), priceMinError);
    }
    if (priceMaxError && priceMaxError !== "") {
        showErrorPopUp(document.getElementById('priceMax'), priceMaxError);
    }

    if (gardenError && gardenError !== "") {
        showErrorPopUp(document.getElementById('garden'), gardenError);
    }
    if (imageError && imageError !== "") {
        showErrorPopUp(document.getElementById('image-button'), imageError);
    }
}

/**
 * Method checks whether there is a value in the gardenfield, because if there is
 * then that implies that the user selected an option as there is no other way
 * to type in that field.
 * @returns {boolean} indicating whether validation passed.
 */
function validateGardenSelection() {
    const gardenField = document.getElementById('garden');
    const selectedGarden = gardenField.value;
    const gardenError = document.getElementById('errorGarden')?.value;

    if (!selectedGarden || selectedGarden === "") {
        setStyleInvalid(gardenField);
        return false;
    } else if (gardenError && gardenError !== "") {
        setStyleInvalid(gardenField);
        return false;
    }


    setStyleValid(gardenField);
    removeErrorPopUp(gardenField);
    return true;
}

/**
 * Runs all the validation functions for the form
 */
function validateAllRequestFields() {
    validateDateMin();
    validateDateMax();
    validateDescription('description');
    validateTitle();
    validatePriceMin();
    validatePriceMax();
    validateGardenSelection();
}

// Adds input event listeners to the fields, cleaner than putting it in the template
document.getElementById('title').addEventListener('input', validateTitle)
document.getElementById('description').addEventListener('input', () => validateDescription('description'))
document.getElementById('dateMin').addEventListener('input', validateDateMin);
document.getElementById('dateMax').addEventListener('input', validateDateMax);
// Add a listener to the minimum date field to update the maximum field's validity if changed.
// This is so the 'latest date not before earliest date' highlighting stays consistent
document.getElementById('dateMin').addEventListener('input', validateDateMax);
document.getElementById('priceMin').addEventListener('input', validatePriceMin)
document.getElementById('priceMax').addEventListener('input', validatePriceMax)

document.getElementById('garden').addEventListener('change', validateGardenSelection);
