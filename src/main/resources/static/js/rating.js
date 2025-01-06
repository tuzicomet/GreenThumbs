let stars =
    document.getElementsByClassName("star");

let rating = document.getElementById("rating");

/**
 * Updates the star rating based on user interaction.
 *
 * @param n The number of stars clicked by the user, representing the rating.
 *
 * This function removes any previously applied styling on the stars and applies
 * a new class to the first 'n' star elements based on the rating value (n).
 * It also updates the value of the hidden rating input field.
 *
 * Class names:
 * - 1 star -> "one"
 * - 2 stars -> "two"
 * - 3 stars -> "three"
 * - 4 stars -> "four"
 * - 5 stars -> "five"
 */
function changeRating(n) {
    document.getElementById("submit-rating").disabled = false;
    remove();  // Remove existing star styling
    rating.value = n;  // Set the hidden rating value
    for (let i = 0; i < n; i++) {
        let cls = "";
        if (n === 1) cls = "one";
        else if (n === 2) cls = "two";
        else if (n === 3) cls = "three";
        else if (n === 4) cls = "four";
        else if (n === 5) cls = "five";

        // Apply the new class to the first n stars
        stars[i].className = "star " + cls;
    }
}

/**
 * Removes the pre-applied styling from all star elements.
 *
 * This function resets the className of all stars to "star",
 * removing any rating-specific styling.
 */
function remove() {
    let i = 0;
    // Loop through all 5 stars and reset their class
    while (i < 5) {
        stars[i].className = "star";
        i++;
    }
}