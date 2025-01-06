Feature: Rating a Contractor for a Service Request

  Scenario: Successful Rating Submission
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the service request details page for a complete service request
    And I have selected a "5"-star rating
    When I click the submit button
    Then I am redirected to the "Service Request Details" page

  Scenario: Incomplete Service Request Rating Submission
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the service request details page for an incomplete service request
    And I have selected a "5"-star rating
    When I click the submit button
    Then I am redirected to the "My Service Requests" page