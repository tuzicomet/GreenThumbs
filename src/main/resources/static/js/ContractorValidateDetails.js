/**
 * Validates the input in the description field, and updates the style of the field accordingly
 */
function validateDescription() {
    let description = document.getElementById('description');
    let pattern = /^[\s\S]{0,1024}$/s;

    if(description.value === ""){
        setStyleInvalid(description);
        return;
    }

    if(!pattern.test(description.value)){
        setStyleInvalid(description);
        return;
    }

    setStyleValid(description);
}

/**
 * Validates all input fields
 */
function validateAllContractor() {
    validateDescription();
}

/**
 * Show an error message pop up on each input that has raised an error at the back end.
 */
function showAllContractorErrors() {
    const descriptionError = document.getElementById('descriptionError')?.value;
    const imageFormatError = document.getElementById('format-error')?.value;
    const imageSizeError = document.getElementById('size-error')?.value;
    const imageMoreThanFiveError = document.getElementById('length-error')?.value;

    let imageErrors = [];
    if (imageFormatError && imageFormatError !== "") {
        imageErrors.push(imageFormatError);
    }
    if (imageSizeError && imageSizeError !== "") {
        imageErrors.push(imageSizeError);
    }
    if (imageMoreThanFiveError && imageMoreThanFiveError !== "") {
        imageErrors.push(imageMoreThanFiveError);
    }

    const combinedImageErrors = imageErrors.join("<br>");

    // Show description errors
    if (descriptionError && descriptionError !== "") {
        showErrorPopUp(document.getElementById('description'), descriptionError);
    }

    if (combinedImageErrors) {
        showErrorPopUp(document.getElementById('imagesUpload'), combinedImageErrors);
    }
}