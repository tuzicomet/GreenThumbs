Feature: U8 - Create Garden
  Background: 
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"

  Scenario: AC2 - Valid details create garden
    Given I am on the create garden form
    When I enter valid garden details
    And I click the Submit button
    Then I am taken to the garden details page

  Scenario: AC3.1 - Empty name create garden
    Given I am on the create garden form
    When I enter an empty garden name
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden name cannot be empty"

  Scenario: AC3.2 - Name with symbols create garden
    Given I am on the create garden form
    When I enter the garden name "TE$T GARDEN"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden name must only include letters, numbers, spaces, dots, hyphens, or apostrophes"

  Scenario: AC3.3 - Too long name create garden
    Given I am on the create garden form
    When I enter the garden name "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden name must be no more than 100 characters"

  Scenario: AC5.1 - Size with letters create garden
    Given I am on the create garden form
    When I enter the garden size "BAD SIZE"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden size must be a positive number"

  Scenario: AC5.2 - Negative size create garden
    Given I am on the create garden form
    When I enter the garden size "-500"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden size must be a positive number"

  Scenario: AC5.3 - Too small size create garden
    Given I am on the create garden form
    When I enter the garden size "0.09999"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden size must be no less than 0.1 square meters"

  Scenario: AC5.4 - Too long size create garden
    Given I am on the create garden form
    When I enter the garden size "0.09999999999999999"
    And I click the Submit button
    Then I remain on the garden form and I am shown the message "Garden size must be no more than 10 characters long"

  Scenario: AC6 - Size in european format create garden
    Given I am on the create garden form
    When I enter the garden size "10,9"
    And I click the Submit button
    Then I am taken to the garden details page