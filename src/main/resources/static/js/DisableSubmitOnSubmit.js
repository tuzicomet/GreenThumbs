document.addEventListener("DOMContentLoaded", function() {
    const forms = document.querySelectorAll('form');

    forms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            const submitButton = form.querySelector('#submit');
            if (submitButton) {
                submitButton.disabled = true;
            }
        });
    });
    window.addEventListener('pageshow', function(event) {
        const submitButton = document.querySelector('#submit');
        if (submitButton) {
            submitButton.disabled = false;
        }
    });
});