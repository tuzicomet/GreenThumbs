

const imagesContainers = document.getElementsByClassName("image-container")
const imagePathString = document.getElementById("imagesPaths").value;
const imagePaths = imagePathString.slice(1, -1)
    .split(", ")
    .filter(path => path.trim() !== '');

document.getElementById("upload-icon").style.display = (imagesContainers.length >= 5) ? "none" : "inline-block";


for (let i = 0; i < imagesContainers.length; i++) {
    const removeIcon = document.createElement('span');
    removeIcon.id = 'remove-icon';
    removeIcon.className = 'material-symbols-outlined remove-icons';
    removeIcon.textContent = 'cancel';

    removeIcon.addEventListener('click', function() {
        imagesContainers[i].remove();
        imagePaths.splice(i, 1);
        document.getElementById("imagesPaths").value = '[' + imagePaths.join(", ") + ']';
        document.getElementById("upload-icon").style.display = "inline-block";
    });

    imagesContainers[i].appendChild(removeIcon);
}