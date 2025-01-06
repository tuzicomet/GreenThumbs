/**
 * Handle image upload event
 *
 * Validate upload images and display them
 *
 * If images are invalid then an error message is created and display under the input element
 * Error message display all requirement not met
 *
 * If images are valid then display them in a horizontal scrollable div
 *
 * When user uploaded 5 images the upload button is hidden
 */
document.getElementById('imagesUpload').addEventListener('change', function(event) {
    const allowedExtensions = ["image/jpeg", "image/png", "image/svg+xml"];
    const MB_SIZE = 10000000; // 10MB
    const files = event.target.files;
    const validFileInput = document.getElementById('validUpload');
    let validFiles = new DataTransfer();
    const previewContainer = document.getElementById('imagesHolder');
    const imagePathString = document.getElementById("imagesPaths").value;
    const imagePaths = imagePathString.slice(1, -1)
        .split(", ")
        .filter(path => path.trim() !== '');

    let imageCount = validFileInput.files.length + imagePaths.length;
    //Add existing files from valid input to data transfer
    for (let i = 0; i < validFileInput.files.length; i++) {
        validFiles.items.add(validFileInput.files[i]); // Add each existing files from the validUpload input to the DataTransfer object
    }
    //Reset existing files in valid file input
    validFileInput.value = ''

    let hasError = false;
    let imagesError = "";

    for (let i = 0; i < files.length; i++) {
        let file = files[i];
        let fileSize = file.size

        if (imageCount >= 5) {
            hasError = true;
            imagesError = document.getElementById('image-more-than-five').textContent;
            break;
        }
        if (!allowedExtensions.includes(file.type)) {
            hasError = true;
            imagesError = document.getElementById('image-format-error').textContent;
            continue;

        } else if (fileSize >= MB_SIZE) {
            hasError = true;
            imagesError = document.getElementById('image-too-large-error').textContent;
            continue;
        } else {
            validFiles.items.add(file);
            const reader = new FileReader();
            reader.onload = function (e) {
                // Create a container div for each image and its remove button
                const imgContainer = document.createElement('div');
                imgContainer.classList.add('image-container');

                const img = document.createElement('img');
                img.src = e.target.result;
                img.classList.add('image');

                // Create the remove button
                const removeIcon = document.createElement('span');
                removeIcon.id = 'remove-icon';
                removeIcon.className = 'material-symbols-outlined remove-icons';
                removeIcon.textContent = 'cancel';

                // Attach the remove functionality
                removeIcon.addEventListener('click', function() {
                    imgContainer.remove();
                    imageCount--; // Decrease counter when an image is removed
                    const validFileInput = document.getElementById('validUpload');
                    let dt = new DataTransfer()
                    for (let i = 0; i < validFileInput.files.length; i++) {
                        if (validFileInput.files[i] !== file) {
                            dt.items.add(validFileInput.files[i])
                        }}
                    validFileInput.value = '';
                    validFileInput.files = dt.files;
                    updateUploadIconVisibility(); // Update visibility of upload icon
                });

                // Append the image and the remove button to the container
                imgContainer.appendChild(img);
                imgContainer.appendChild(removeIcon);

                // Append the container to the preview container
                previewContainer.appendChild(imgContainer);
            };
            imageCount++; // Increase counter when an image is added
            reader.readAsDataURL(file);
        }
    }

    //Empty image upload input files
    event.target.value = ''

    // Handle errors after all files are processed
    if (hasError) {
        showErrorPopUp(document.getElementById("imagesUpload"), imagesError);
    }

    // Update the upload icon visibility
    function updateUploadIconVisibility() {
        document.getElementById("upload-icon").style.display = (imageCount >= 5) ? "none" : "inline-block";
    }

    // Initial check to update icon visibility
    updateUploadIconVisibility();

    validFileInput.files = validFiles.files
});