/**
 * Checks if the value of the description field is less than 512 characters with a character count.
 * @returns {boolean} true if the description is valid, otherwise false.
 */
function descriptionCounter(maxLength) {
    const description = document.getElementById('description');
    const charCounter = document.getElementById('description-counter');
    const currentLength = description.value.length;

    // description counter
    charCounter.textContent = `${currentLength}/${maxLength}`;

}

/**
 * Checks if the value of the question field is less than 512 characters with a character count.
 * @returns {boolean} true if the question is valid, otherwise false.
 */
function questionCounter(maxLength) {
    const question = document.getElementById('question');
    const charCounter = document.getElementById('question-counter');
    const currentLength = question.value.length;

    // question counter
    charCounter.textContent = `${currentLength}/${maxLength}`;

}
/**
 * Checks if the value of the answer field is less than 512 characters with a character count.
 * @returns {boolean} true if the answer is valid, otherwise false.
 */
function answerCounter(maxLength) {
    const answer = document.getElementById('answer');
    const charCounter = document.getElementById('answer-counter');
    const currentLength = answer.value.length;

    // question counter
    charCounter.textContent = `${currentLength}/${maxLength}`;

}