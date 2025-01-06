/**
 * Function to handle validation and enforcing of location fields that require autofill options
 * to be selected.
 * Function to clear all specific location fields (street, suburb, etc...) when the user manually types in the location
 * autocomplete field. This is required when only autocompleted locations are allowed (such as for contractor
 * registration). This makes it so that you cannot select an autocompleted location, then edit it and submit.
 */
function handleLocationInputAndValidation() {
    // Get the location autocomplete field, as well as the specific location fields
    const locationAutocompleteField = document.getElementById('location');
    const specificLocationFields = ['street', 'suburb', 'city', 'postcode', 'country'];

    // Also get the specific city and country fields (required fields for any valid location)
    const cityHiddenField = document.getElementById('city');
    const countryHiddenField = document.getElementById('country');

    /**
     * Function to validate and highlight the location autocomplete field.
     */
    const validateLocationField = () => {
        /*
        A location autocomplete field is considered valid only if a location was selected using
        the autocomplete.
        Our current method of checking this, is making it so that the hidden location fields
        can only be filled by selecting an autocomplete option, and if the user modifies the location
        autocomplete field afterward, these fields are cleared. That way, if the fields have values,
        then we know that the autocomplete was just used.
        City and Country are the only required fields for locations, so we can just check those
         */

        const cityHiddenField = document.getElementById('city');
        const countryHiddenField = document.getElementById('country');

        // If the city or country field is empty, set the field to invalid
        if (cityHiddenField.value === "" || countryHiddenField.value === "") {
            setStyleInvalid(locationAutocompleteField);
        } else {
            setStyleValid(locationAutocompleteField);
        }
    };

    // Add a listener which clears the specific location fields whenever the user manually inputs
    // in the location autocomplete. This makes it so that they cannot select an autocomplete
    // option, and then modify it and still be able to submit it.
    locationAutocompleteField.addEventListener('input', () => {
        // Clear the specific location fields when typing manually
        specificLocationFields.forEach(id => {
            const field = document.getElementById(id);
            if (field) {
                field.value = '';
            }
        });

        // set the location autocomplete field to invalid whenever the user manually inputs to it
        setStyleInvalid(locationAutocompleteField);
    });

    // Add a listener to the country field to validate the location field whenever the
    // countryHiddenField changes (this occurs after an autocomplete option is selected)
    countryHiddenField.addEventListener('input', validateLocationField);

    validateLocationField();
}

handleLocationInputAndValidation();
