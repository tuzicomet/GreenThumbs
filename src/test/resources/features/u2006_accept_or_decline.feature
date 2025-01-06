Feature: U2006 - Accept or decline contractor offers
  As Sarah, I want to be able to accept or decline offers of service from contractors so that I can choose the best service provider for my needs.

  Scenario: AC7 - Accept a valid service request application
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And there is a job application for service request 13
    When I click the Accept button
    Then The application is accepted
    And I am taken back to the details page for service request 13

  Scenario: AC12 - Decline a service request application
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And there is a job application for service request 14
    When I click the Decline button
    Then The application is declined
    And I am taken back to the details page for service request 14