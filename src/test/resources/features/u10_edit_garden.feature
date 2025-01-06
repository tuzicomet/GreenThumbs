Feature: U10 - Edit Garden
  Background:
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I have a garden to edit

  Scenario: AC2 - Valid details edit garden
    Given I am on the edit garden form
    When I enter valid garden details to edit
    And I click the Submit button to edit
    Then The garden details are updated
    And I am taken to the Garden Details page after edit

  Scenario: AC3.1 - Invalid name edit garden
    Given I am on the edit garden form
    When I enter the name "FreakyBob?$/" to edit
    And I click the Submit button to edit
    Then The garden details are not updated
    And I am not redirected

  Scenario: AC3.2 - Empty name edit garden
    Given I am on the edit garden form
    When I enter the name "" to edit
    And I click the Submit button to edit
    Then The garden details are not updated
    And I am not redirected

  Scenario: AC5.1 - Invalid size edit garden
    Given I am on the edit garden form
    When I enter the size "12ABC" to edit
    And I click the Submit button to edit
    Then The garden details are not updated
    And I am not redirected

  Scenario: AC5.2 - European size edit garden
    Given I am on the edit garden form
    When I enter the size "2,3" to edit
    And I click the Submit button to edit
    Then The garden details are updated
    And I am taken to the Garden Details page after edit


