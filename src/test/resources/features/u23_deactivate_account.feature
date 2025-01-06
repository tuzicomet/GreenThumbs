Feature: U23 - Deactivate account temporarily

  Scenario: AC1 - Add fifth inappropriate tag
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I have a valid garden
    And I have added 4 inappropriate tags
    When I add a tag with content "fuck"
    Then a message gives me a warning saying my account will be blocked
    And I receive a fifth strike email

  Scenario: AC2 - Add sixth inappropriate tag
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I have a valid garden
    And I have added 5 inappropriate tags
    When I add a tag with content "fuck"
    Then I am logged out
    And I see a message saying I have been banned for one week
    And I receive an account blocked email

  Scenario: AC3 - Shown message when currently banned
    Given the user with email "verifieduser@gmail.com" is banned for 7 days
    When I enter the email "verifieduser@gmail.com" and the password "Testp4$$"
    Then I see a message saying my account is blocked for 7 days

  Scenario: AC4 - User gets unbanned after period expires
    Given the user with email "verifieduser@gmail.com" was banned for 7 days, 8 days ago
    When I enter the email "verifieduser@gmail.com" and the password "Testp4$$"
    Then I am logged in
