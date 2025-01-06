/**
 * Validate the provided file in the image field
 */
function validateProfileImage(){
    const image = document.getElementById("file1")
    const error = document.getElementById("image-error")
    const allowedExtensions = ["image/jpeg", "image/png", "image/svg+xml"];
    const baseUrl = window.location.href.split('/profile')[0];
    const MB_SIZE = Math.pow(1024, 2)
    if(image.files.length > 0){
        const fileSize = image.files.item(0).size
        if(fileSize > MB_SIZE * 10){
            document.getElementById("image-select-display").src = baseUrl + "/image/default.jpg";
            image.value = null
            error.innerText = "Image must be less than 10MB"
            return false;
        } else if(!allowedExtensions.includes(image.files.item(0).type)){
            document.getElementById("image-select-display").src = baseUrl + "/image/default.jpg";
            image.value = null
            error.innerText = "Image must be of type png, jpg or svg"
            return false;
        }
    }
    error.innerText= ""
    return true;

}