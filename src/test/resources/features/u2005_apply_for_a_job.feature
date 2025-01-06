Feature: U2005 - Apply for a job
  As Ava, I want to apply for a job so that I can offer my gardening services to clients who have requested them.

  Scenario: AC3.1 - Contractor successfully applies for a job
    Given I am logged in with email "applicant@gmail.com" and password "Testp4$$"
    And I am on the job application modal for service request 1
    When I enter valid values for the date and quote price
    And I click the Submit button to submit my job application
    Then I am taken back to the service request details page

  Scenario Outline: AC5 - Apply job with invalid price application not created
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I am on the job application modal for service request 1
    When I enter a valid date and invalid quote price <inputPrice>
    And I click the Submit button to submit my job application
    Then I am on the same page and data is persisted and error message shows
    Examples:
    | inputPrice |
    | "Not a number" |
    | "-1"             |
    | "20.1"           |
    | "15.06.04"       |
    | "10.009"         |



