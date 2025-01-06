/**
 * Script to validate images for the New Service Request page
 */

const imageInput = document.getElementById("image-select-listener");
const imageDisplay = document.getElementById("image-select-display")
const imageButton = document.getElementById("image-button");
imageInput.addEventListener("change", function(event){
    const allowedExtensions = ["image/jpeg", "image/png", "image/svg+xml"];
    const MB_SIZE = Math.pow(1024, 2);
    const files = event.target.files;
    const file = files.length === 0 ? null : files[0];
    let hasError = false;
    let imagesError = "";
    if (!allowedExtensions.includes(file.type)) {
        hasError = true;
        imagesError = document.getElementById('image-format-error-front-end').value;

    } else if (file.size > MB_SIZE * 10) {
        hasError = true;
        imagesError = document.getElementById('image-too-large-error-front-end').value;
    }
    if (hasError) {
        imageInput.value = null;
        showErrorPopUp(imageButton, imagesError);
    }else{
        const reader = new FileReader();
        reader.onloadend = function () {
            imageDisplay.src = reader.result;
        }
        reader.readAsDataURL(file);
        removeErrorPopUp(imageButton)
    }
});