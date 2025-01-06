/**
 * Updates the current URL to have the 'lang' parameter, with its value
 * set to the language code which was passed in. This works alongside LocaleResolver,
 * which will notice this change, and update the site's locale accordingly.
 * @param lang the language code/identifier to set the website locale to
 */
function setLocaleToGivenOption(lang) {

    // Resource used for URLSearchParams:
    // https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams

    // get the full query string from the URL (the string of parameters)
    const queryString = window.location.search;
    // parse the query string for the individual parameters
    const urlParams = new URLSearchParams(queryString);
    // set the lang parameter to the language code which was passed in.
    // if no lang parameter is present, then it creates it, otherwise it overrides it.
    urlParams.set("lang", lang);

    // update the url with the changes
    // the code below was taken from: https://stackoverflow.com/a/74545496
    const path = window.location.href.split('?')[0];
    const newURL = `${path}?${urlParams}`;
    history.pushState({}, '', newURL);

    // Reload the page so that the language change is visible
    location.reload();
}