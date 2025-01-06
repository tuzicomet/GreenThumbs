Feature: U11 - Add plant
  Background:
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I have a garden

  Scenario: AC2 - Valid details create plant
    Given I am on the create plant form
    When I enter valid plant details
    And I click the Submit Plant button
    Then I am taken to the Garden Details page

  Scenario: AC3.1 - Empty name create plant
    Given I am on the create plant form
    When I enter the plant name ""
    And I click the Submit Plant button
    Then I remain on the plant form, and I see the name error message "Plant name cannot be empty"

  Scenario: AC3.2 - Invalid name create plant
    Given I am on the create plant form
    When I enter an invalid plant name
    And I click the Submit Plant button
    Then I remain on the plant form, and I see the name error message "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens, or apostrophes"

  Scenario: AC4 - Too long description create plant
    Given I am on the create plant form
    When I enter a description that is too long
    And I click the Submit Plant button
    Then I remain on the plant form, and I see the description error message "Plant description must be less than or equal to 512 characters"

  Scenario: AC5 - Invalid count create plant
    Given I am on the create plant form
    When I enter an invalid plant count
    And I click the Submit Plant button
    Then I remain on the plant form, and I see the count error message "Plant count must be a positive, whole number"

  Scenario: AC6 - Malformed date create plant
    Given I am on the create plant form
    When I enter a malformed plant date
    And I click the Submit Plant button
    Then I remain on the plant form, and I see the date error message "Date is not in valid format, (DD/MM/YYYY)"