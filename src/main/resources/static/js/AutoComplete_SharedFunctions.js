/**
 * Debounce function to limit the rate at which the autocomplete function is invoked
 * @param func The function to debounce.
 * @param delay The delay in milliseconds between function calls.
 * @returns {(function(): void)|*}  A debounced version of the provided function.
 */
function debounce(func, delay) {
    let timerId;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timerId);
        timerId = setTimeout(() => func.apply(context, args), delay);
    };
}

/**
 * Closes the autocomplete dropdown lost
 * depends on element id
 */
function closeDropDownList() {
    const autocompleteItemsElement = document.getElementById("autocomplete-items");
    if (autocompleteItemsElement) {
        while (autocompleteItemsElement.firstChild) {
            autocompleteItemsElement.removeChild(autocompleteItemsElement.firstChild);
        }
    }
}
// Close the suggestions when clicking elsewhere
document.body.addEventListener("mousedown", function(e) {
    const autocompleteItemsElement = document.getElementById("autocomplete-items");
    if (autocompleteItemsElement && !autocompleteItemsElement.contains(e.target) && e.target !== input) {
        closeDropDownList();
    }
});