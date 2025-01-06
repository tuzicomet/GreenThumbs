/**
 * Used to update whether a garden is public or not.
 */
function submitPublicisedForm(isPublicised) {
    const publicisedInput = document.getElementById("publicised");
    publicisedInput.checked = isPublicised;
    document.getElementById("updatePublicisedForm").submit();
}

/**
 * Used to update whether the recently accessed gardens widget is visible or not on main page.
 */
function submitRecentGardensForm(setRecentGardens) {
    const recentGardensInput = document.getElementById("recentGardens");
    recentGardensInput.checked = setRecentGardens;
    document.getElementById("updateRecentGardensForm").submit();
}

/**
 * Used to update whether the recently accessed plants widget is visible or not on main page.
 */
function submitRecentPlantsForm(setRecentPlants) {
    const recentPlantsInput = document.getElementById("recentPlants");
    recentPlantsInput.checked = setRecentPlants;
    document.getElementById("updateRecentPlantsForm").submit();
}

/**
 * Used to update whether my friends list widget is visible or not on main page.
 */
function submitFriendsForm(setFriends) {
    const friendsInput = document.getElementById("friends");
    friendsInput.checked = setFriends;
    document.getElementById("updateFriendsForm").submit();
}

/**
 * Does the front end validation for the tags including checking the regex and whether it is
 * longer than 25 characters.
 */
function validateTag() {
    const tagInput = document.getElementById("tag-input");
    const tag = tagInput.value.trim();
    // Regex which allows alphanumeric/accented characters, spaces, -, _, ', or ”
    // NOTE: the 'u flag' is required at the end to make JavaScript regex properly
    // handle unicode characters, unlike Java which natively supports Unicode
    const tagRegex = /^[\p{L}\p{N}\p{M}  \-_'"]+$/u;

    if (tag.length === 0) {
        setStyleDefault(tagInput)
        return;
    }
    if (tag.length > 25) {
        setStyleInvalid(tagInput)
        return;
    }
    if (!tagRegex.test(tag)) {
        setStyleInvalid(tagInput)
        return;
    }
    setStyleValid(tagInput)
}

/**
 * Is called on window load to show any error popups that might be needed in this case any tag
 * errors.
 */
function showTagErrors() {
    const tagError = document.getElementById('tagError')?.value;
    if (tagError && tagError !== "") {
        showErrorPopUp(document.getElementById("tag-input"), tagError);
        setStyleInvalid(document.getElementById("tag-input"));
    }
}