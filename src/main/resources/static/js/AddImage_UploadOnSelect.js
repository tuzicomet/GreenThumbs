/**
 * Valid content types for image upload.
 */
const VALID_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/svg+xml'];
const MAX_IMAGE_SIZE = 10485760;
/**
 * Whenever a file is uploaded to an element with the 'image-select-listener' id,
 * the file, this script updates the element tagged by the 'image-select-display'
 * id to use the uploaded image as its source, which displays it.
 */
function displayUploadedImage() {
    // The element which displays the image
    const imageDisplay = document.getElementById('image-select-display');
    // the element which uploaded file is inputted to
    const imageUploadButton = document.getElementById('image-select-listener');

    // add an EventListener, which will be triggered whenever a file is
    // selected using the imageUploadButton
    imageUploadButton.addEventListener('change', (event) => {
        // if picker failed to select a file
        if (event.target.files[0] == null) {
            return;
        }
        // retrieve the file
        const file = event.target.files[0];

        // if image type not valid or file size over maximum, show error image
        if (!VALID_IMAGE_TYPES.includes(file['type']) || file.size > MAX_IMAGE_SIZE) {
            imageDisplay.src = "/images/error.png";
            return;
        }

        // read the uploaded image and display
        const reader = new FileReader();
        reader.onloadend = function () {
            imageDisplay.src = reader.result;
        }
        // Read the file as a data URL, which can be displayed in browser
        reader.readAsDataURL(file);
    }, false);
}

displayUploadedImage();