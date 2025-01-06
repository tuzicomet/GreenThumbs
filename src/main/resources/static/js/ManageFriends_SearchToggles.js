/**
 * Function which toggles the Add Friend search popup on/off
 */
function toggleSearchTools() {
    var searchTools = document.getElementById("search-tools");

    // if searchTools is currently visible
    if (searchTools.style.display !== "none") {
        searchTools.style.display = "none";
    } else {
        searchTools.style.display = "block";
    }
}

/**
 * Toggles the given user's confirmation window to remove them as a friend
 */
function toggleUserRemovalConfirmationWindow(userId) {
    // get the confirmation window belonging to the specific target user
    var confirmationWindow = document.getElementById(
        `remove-friend-${userId}-confirmation`);

    // if the confirmation window is currently visible
    if (confirmationWindow.style.display !== "none") {
        // hide the window
        confirmationWindow.style.display = "none";
    } else {
        // otherwise display it
        confirmationWindow.style.display = "block";
    }
}