// Get the modal
const applyModal = document.getElementById("applyModal");

// Get the button that opens the modal
const btn = document.getElementById("applyModalButton");
if (btn) {
    // When the user clicks on the Apply button, open the modal
    btn.onclick = function () {
        applyModal.style.display = "block";
    }
}

/**
 * Open a modal given its HTML ID. Sets display to block.
 * @param modalId HTML ID of the modal to open.
 */
function showModal(modalId) {
    const requestedModal = document.getElementById(modalId)
    requestedModal.style.display = "block"
}

/**
 * Opens a confirmation modal (typically used for any of the accept or decline ones).
 * Hides all the buttons on the confirmation modal so the calling method can set its desired one.
 * @param modalId HTML ID of the modal to open.
 * @param content Text content to display in the modal.
 */
function openConfirmationModal(modalId, content) {
    const confirmationModal = document.getElementById(modalId)
    const contentBox = confirmationModal.querySelector('.content');

    // Due to the use of template strings using user input, this must always be innerText.
    contentBox.innerText = content;
    confirmationModal.style.display = 'block';

    // Hide all the buttons, then can open specific ones in specific functions
    for (const formElement of confirmationModal.querySelectorAll('form')) {
        formElement.style.display = 'none';
    }
}

/**
 * Opens an accept modal by changing its display, setting the button type to 'accept', and adding the required content.
 * @param id
 * @param name
 */
function openAcceptModal(id, name) {
    // Needed for i18n to work on dynamic client-side strings
    let content = document.getElementById('confirm-accept-template-string').value;
    content = content.replace('{{name}}', name);

    // Fills a hidden input on the modal with the job's ID
    const applicationIdInput = document.getElementById('application-id-input');
    applicationIdInput.value = id;

    // Opens the modal
    const acceptButton = document.getElementById('confirm-accept');
    openConfirmationModal('accept-confirmation-modal', content);
    acceptButton.style.display = 'block';

    // Disable buttons after confirmation, stops buttons from being spammed clientside
    acceptButton.onsubmit = function () {
        disableApplicationButtons();
    };
}

/**
 * Function for opening the decline job application modal
 * @param id the id of the job
 */
function openDeclineModal(id) {
    // Needed for i18n to work on dynamic client-side strings
    let content = document.getElementById('confirm-decline-template-string').value;

    // Fills a hidden input on the modal with the job's ID
    const applicationIdInput = document.getElementById('decline-application-id');
    applicationIdInput.value = id;

    // Opens the modal
    const declineForm = document.getElementById('confirm-decline');
    openConfirmationModal('decline-confirmation-modal', content);
    declineForm.style.display = 'block';

    // Disable buttons after submission, stops buttons from being spammed clientside
    declineForm.onsubmit = function () {
        disableApplicationButtons();
    };
}

/**
 * Function to disable all accept and decline buttons
 */
function disableApplicationButtons() {
    const buttons = document.querySelectorAll('.accept, .decline');
    buttons.forEach(button => {
        button.disabled = true;
    });
}

// Only for the owner, make all the job accept and decline buttons open modals
const containers = document.getElementById('container-applications');
if (containers) {
    const acceptButtons = containers.querySelectorAll('.accept')
    for (const button of acceptButtons) {
        button.addEventListener('click', () => {
            const id = button.dataset.id;
            const name = button.dataset.name;
            openAcceptModal(id, name);
        })
    }

    const declineButtons = containers.querySelectorAll('.decline')
    for (const button of declineButtons) {
        button.addEventListener('click', () => {
            const id = button.dataset.id;
            openDeclineModal(id);
        })
    }
}

// Make all the cancel buttons close their respective modals
const modals = document.getElementsByClassName('modal');
for (const modal of modals) {
    const cancelButton = modal.querySelector('.cancel');
    cancelButton.addEventListener('click', () => {
        modal.style.display = 'none';
    })
}

/**
 * When a user clicks off any modal, close all the modals
 */
window.onclick = function (event) {
    if (event.target.classList.contains('modal')) {
        const modals = document.getElementsByClassName('modal')
        for (const modal of modals) {
            modal.style.display = "none";
        }
    }
}