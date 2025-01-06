/**
 * When the user presses the answer button the question related to that is shown, with
 * the answer text box and buttons shown.
 * @param button the related button
 * @param questionId the related question
 */
function answerQuestion(button, questionId) {
    document.querySelectorAll('.QA-Box').forEach(function(questionDiv) {
        questionDiv.style.display = 'none';
    });

    var questionDiv = button.closest('.QA-Box');
    questionDiv.style.display = 'flex';

    button.style.display = 'none';

    document.getElementById('answer-form').style.display = 'block';
    document.getElementById('questionIdInput').value = questionId;

    var content = button.closest('.container-applications') || button.closest('.content');
    content.style.maxHeight = content.scrollHeight + "px";
    document.getElementById('answer-form').scrollIntoView({ behavior: 'smooth' });
}

/**
 * Javascript function that is called when the back button is pressed,
 * just undoes the previous function
 */
function goBackToQuestions() {
    // Hide the answer form
    document.getElementById('answer-form').style.display = 'none';

    // Show all question-related sections
    document.querySelectorAll('.QA-Box').forEach(function(questionDiv) {
        questionDiv.style.display = 'flex';
    });
    document.querySelectorAll('.question-format').forEach(function(questionDiv) {
        questionDiv.style.display = 'flex';
    });
    document.querySelectorAll('.answer-section').forEach(function(questionDiv) {
        questionDiv.style.display = 'flex';
    });

    // Display answer buttons
    document.querySelectorAll('.answer-button').forEach(function(button) {
        button.style.display = 'inline-block';
    });

    // Clear the answer input
    document.getElementById('answer').value = '';

    // Handle the content section height update
    var content = document.getElementById("QAcontent")

    // Temporarily set the height to auto to force recalculation
    content.style.maxHeight = 'none';
    content.style.overflow = 'hidden';

    // Use setTimeout to allow the DOM to update before calculating the new height
    setTimeout(function() {
        content.style.maxHeight = content.scrollHeight + "px";
        content.style.overflow = '';  // Reset overflow
    }, 100); // Small delay to ensure all layout recalculations have completed
}
