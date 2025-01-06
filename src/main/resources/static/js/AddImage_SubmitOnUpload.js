/**
 * Adds onclick listeners to each of the edit-image buttons.
 * Needs each input to have the `semi-transparent-button-listener` class on it and the 'image/input/button' group
 * to be wrapped in a form element which contains details on its encoding, request method, and action.
 */
function displayUploadedImage() {
    const imageUploadInputs = document.getElementsByClassName('semi-transparent-button-listener');
    const allowedExtensions = ["image/jpeg", "image/png", "image/svg+xml"];
    for (let i = 0; i < imageUploadInputs.length; i++) {
        let input = imageUploadInputs[i];
        input.addEventListener('change', (event) => {
            if (event.target.files[0]) {
                if(event.target.files[0].size > 10*1024*1024){
                    input.nextElementSibling.textContent = "Image must be less than 10MB"
                }else if(!allowedExtensions.includes(event.target.files[0].type)) {
                    input.nextElementSibling.textContent = "Image must be of type png, jpg or svg"
                } else{
                        input.parentElement.submit();

                    }
                }
        }, false);
    }
}

displayUploadedImage();