Feature: U1 - Register User

  Scenario: AC2 - Valid details register
    Given I am on the register form
    When I enter valid registration details
    And I click the Sign Up button
    Then I am taken to the activate page

  Scenario: AC4.1 - Invalid first name register - symbols
    Given I am on the register form
    When I enter the first name "Fab!an"
    And I click the Sign Up button
    Then I remain on the register page, and I see the first name error "First name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter"

  Scenario: AC4.2 - Invalid last name register - symbols
    Given I am on the register form
    When I enter the last name "Gil$on"
    And I click the Sign Up button
    Then I remain on the register page, and I see the last name error "Last name cannot be empty and must only include letters, spaces, hyphens, or apostrophes, with at least one letter"

  Scenario: AC5 - Invalid first name length register
    Given I am on the register form
    When I enter a first name over 64 characters
    And I click the Sign Up button
    Then I remain on the register page, and I see the first name error "First name must be 64 characters long or less"

  Scenario: AC6 - Invalid last name length register
    Given I am on the register form
    When I enter a last name over 64 characters
    And I click the Sign Up button
    Then I remain on the register page, and I see the last name error "Last name must be 64 characters long or less"

  Scenario: AC6 - Email malformed register
    Given I am on the register form
    When I enter the email "a@b."
    And I click the Sign Up button
    Then I remain on the register page, and I see the email error "Email address must be in the form 'jane@doe.nz'"

  Scenario: AC7 - Email in use register
    Given I am on the register form
    And a user with the email "alreadyinuse@gmail.com" is registered
    When I enter the email "alreadyinuse@gmail.com"
    And I click the Sign Up button
    Then I remain on the register page, and I see the email error "This email address is already in use"

  Scenario: AC8 - Malformed date register
    Given I am on the register form
    When I enter the date "02/27/1990"
    And I click the Sign Up button
    Then I remain on the register page, and I see the date error "Date is not in valid format, (DD/MM/YYYY)"

  Scenario: AC9 - Too young register
    Given I am on the register form
    When I enter a date that is 12 years ago
    And I click the Sign Up button
    Then I remain on the register page, and I see the date error "You must be 13 years or older to create an account"

  Scenario: AC10 - Too old register
    Given I am on the register form
    When I enter a date that is 121 years ago
    And I click the Sign Up button
    Then I remain on the register page, and I see the date error "The maximum age allowed is 120 years"

  Scenario: AC11 - Non-matching passwords register
    Given I am on the register form
    When I enter the password "Testp4$$"
    And I enter the confirm password "NotTheSame"
    And I click the Sign Up button
    Then I remain on the register page, and I see the password error "Passwords do not match"

  Scenario: AC12 - Weak password register
    Given I am on the register form
    When I enter the password "password"
    And I enter the same confirm password
    And I click the Sign Up button
    Then I remain on the register page, and I see the password strength error "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character"
