/**
 * Validates the input in the question box, and updates the style of the field accordingly
 */
function validateQuestion(input) {
    const pattern = /^(?=.*\p{L}.*)([\s\S]{0,512}|)$/su;

    if(!pattern.test(input.value.trim())){
        setStyleInvalid(input);
        return;
    }

    setStyleValid(input);
}
/**
 * Show an error message pop up for question if an error is raised in the backend
 */
function showQuestionErrorIfExists() {
    const field = document.getElementById('errorQuestion');
    const questionError = field?.value;
    if (questionError && questionError !== "") {
        validateQuestion(field);
        showErrorPopUp(document.getElementById('question'), questionError);
    }
}
/**
 * Show an error message pop up for answer if an error is raised in the backend
 */
function showAnswerErrorIfExists() {
    const field = document.getElementById('errorAnswer');
    const answerError = field?.value;
    if (answerError && answerError !== "") {
        validateQuestion(field);
        showErrorPopUp(document.getElementById('answer'), answerError);
    }
}

// If the question box exists (not the owner), add a validity listener to it and do an initial validation
const questionBox = document.getElementById('question');
if (questionBox) {
    questionBox.addEventListener('input', validateQuestion);
    showQuestionErrorIfExists();
}
