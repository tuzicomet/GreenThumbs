Feature: U15 - Garden location and autocomplete
  As a user, I want to provide and manage addresses for my garden to ensure location-based services work correctly.

  Background:
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"

  Scenario: AC5 - Required fields for address
    Given I am on the create new garden form
    When I submit the form without providing a city and country
    Then an error message tells me "City and Country are required"

  Scenario: AC6 - Address autocomplete suggestions
    Given I am on the create new garden form
    When I start typing a location "Test"
    Then I receive reasonable suggestions of locations matching the current entry I have provided

  Scenario: AC8 - No matching address suggestions
    Given there are no matching address suggestions for my current entry
    Then I am shown the message "No matching location found, location-based services may not work"
