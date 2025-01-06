/**
 * Adds an event listener to the form to customise handling of the Enter key.
 * When added to a html template, prevents the default action (i.e. form submission)
 * when the Enter key is pressed on any input field in the form, other than the last
 * input field in the form, or the submit button. Needed for NFR12.
 */
document.addEventListener("DOMContentLoaded", function() {
    // Select the <form> element from the document
    const form = document.querySelector('form');
    // Select all input elements, apart from the submit button
    const inputs = form.querySelectorAll('input:not([type="submit"])');
    // Select only input fields by filtering out any inputs with 'error' in their id name
    // (hidden inputs used for error messages)
    const inputFields = Array.from(inputs).filter(
        input => !input.id.toLowerCase().includes('error')
    );
    // save the last input element as lastInput
    const lastInput = inputFields[inputFields.length - 1];
    const searchInput = document.getElementById('search-gardens');

    // Event listener
    form.addEventListener("keydown", function(event) {
        // if the Enter key is pressed on input elements other than the last input field
        // or the submit button, prevent the default action (form submission)
        if (event.key === "Enter" &&
            document.activeElement.tagName === "INPUT" &&
            document.activeElement.type !== "submit" &&
            document.activeElement !== lastInput &&
            document.activeElement !== searchInput) {
            event.preventDefault();
        }
    });
});