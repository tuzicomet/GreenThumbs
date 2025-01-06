
// Price must either be an integer, or a number followed by a . or , then two decimal places
const COST_REGEX = /^[0-9]+([.,][0-9]{1,2})?$/;

/**
 * Validates the input in the price field and updates the style of the field accordingly
 */
function validatePrice() {
    // retrieve and format the minimum price value
    const priceField = document.getElementById('price');
    const price = priceField.value.trim().replace(/,/g, '.');
    const priceMin = parseFloat(document.getElementById('priceMinHidden').value);
    const priceMax = parseFloat(document.getElementById('priceMaxHidden').value);
    const priceFloat = parseFloat(price);


    if (!price) {
        setStyleInvalid(priceField);
        return;
    }

    // Ensure the minimum value meets the price regex. If not, set the field style to invalid
    if (!COST_REGEX.test(price)) {
        setStyleInvalid(priceField);
        return;
    }
    if(priceFloat < priceMin || price > priceMax){
        setStyleInvalid(priceField);
        return;
    }

    // if there were no problems, set the field style to valid
    setStyleValid(priceField);
}

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
 * Validates the input in the dateMin field and updates the style of the field accordingly
 */
function validateDate() {
    const dateField = document.getElementById('date');
    const dateValue = getParsedValueOfDateField(dateField);
    const dateMinField = document.getElementById('dateMinLabel');
    const dateMinValue = new Date(dateMinField.innerText.split('/').reverse().join('-'));
    const dateMaxField = document.getElementById('dateMaxLabel');
    const dateMaxValue = new Date(dateMaxField.innerText.split('/').reverse().join('-'));

    if (!dateValue || !dateMinValue || !dateMaxValue) {
        setStyleInvalid(dateField);
        return;
    }
    if (dateValue < dateMinValue || dateValue > dateMaxValue) {
        setStyleInvalid(dateField);
    } else {
        setStyleValid(dateField);
    }
}

/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllRequestErrors() {
    const priceError = document.getElementById('errorPrice')?.value;
    const dateError = document.getElementById('errorDate')?.value;

    if (priceError && priceError !== "") {
        showErrorPopUp(document.getElementById('price'), priceError);
    }
    if (dateError && dateError !== "") {
        showErrorPopUp(document.getElementById('date'), dateError);
    }

}


/**
 * Runs all the validation functions for the form
 */
function validateAllRequestFields() {
    validatePrice();
    validateDate();
}

const modal = document.getElementById("applyModal");
if (document.getElementById("modalOpen").value === "true") {
    modal.style.display = "block";
    validateAllRequestFields();
    showAllRequestErrors();
}


// event listeners
document.getElementById('price').addEventListener('input', validatePrice)
document.getElementById('date').addEventListener('input', validateDate)

