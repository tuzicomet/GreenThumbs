Feature: U20 - Browse Gardens
  As a user, I want to browse public gardens so that I can view their details and search for specific gardens.
  Background:
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"

  Scenario: AC1 - View public garden details
    Given another user owns 1 gardens marked public
    When I click on a link to the garden
    Then I can view the garden name, size, and plants

  Scenario: AC3 - Search for gardens by name or plants
    Given I am on the browse gardens page
    When I enter a search string and click the search button
    Then I am shown gardens whose names or plants include my search value

  Scenario: AC5 - No search matches
    Given I am on the browse gardens page
    When I enter the search string "NotGoingToMatchAnything"
    Then a message tells me “No gardens match your search”

  Scenario: AC6 - Paginated search results
    Given another user owns 11 gardens marked public
    When I enter the search string "Garden"
    Then the results are paginated with 10 per page
