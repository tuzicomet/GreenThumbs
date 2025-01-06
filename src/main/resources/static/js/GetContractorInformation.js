function showContractorModal(contractorId) {
    const url = window.location.href.split('?')[0] + `/applicant/${contractorId}`;
    const baseURL = window.location.href.split('/serviceRequest')[0]
    fetch(url)
        .then(res => {
            if (!res.ok) {
                return null;
            } else {
                return res.json();
            }
        })
        .then(data => {
            if (!data) {
                console.error('No contractor data found.');
                return;
            }

            // Populate the modal with the contractor's information
            document.getElementById('tooltipFlairId').textContent = data.flairToolTip;
            document.getElementById('contractorName').textContent = data.name;
            document.getElementById('contractorProfilePicture').src = baseURL + data.profilePicture;
            document.getElementById('contractorFlair').src = baseURL + data.flair;
            document.getElementById('contractorAboutMe').textContent = data.aboutMe;
            document.getElementById('contractorNumRatings').textContent = data.numRatings;
            document.getElementById('star-rating').style.setProperty('--rating', data.avgRating);
            const imageContainer = document.getElementById("contractor-work-pictures")
            imageContainer.innerHTML = ''; //Empty Container before appending new images
            if (data.avgRating === 'null') {
                document.getElementById('star-rating').style.display = 'none'
            } else {
                document.getElementById('star-rating').style.display = 'block'
            }

            // Handle work pictures
            if (data.workPictures && data.workPictures !== '[]') {
                document.getElementById('picture-header').style.display = 'block';
                let pictures = data.workPictures.replace(/[\[\]]/g, '');
                let picturePaths = pictures.split(',').map(path => path.trim());
                for (let i = 0; i < picturePaths.length; i++) {
                    let img = document.createElement('img');
                    img.src = baseURL + picturePaths[i];
                    img.classList.add('contractor-work-pictures')
                    imageContainer.appendChild(img);
                }
            } else {
                document.getElementById('picture-header').style.display = 'none';
            }
            // Display the modal
            document.getElementById('contractor-info-modal').style.display = 'block';
        })
        .catch(error => {
            console.error('Error fetching contractor data:', error);
        });
}

// Close the modal
function closeContractorModal() {
    document.getElementById('contractor-info-modal').style.display = 'none';
}
