Feature: U200016 - Ask a question about a job
  As Ava, I want to ask users questions about the job so that I can clarify details before applying.

  Scenario: AC3 - No questions asked shows message
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have a valid service request
    And No questions have been asked
    When I visit its details page
    Then I can see a message