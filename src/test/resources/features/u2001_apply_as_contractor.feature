Feature: U2001 - Register as a contractor
  As Ava, when I become a contractor I want to provide an about-me description of myself and my services and
  photos of my previous work, so that my job applications will be more appealing to other users.

  Scenario: AC4 - Register contractor with valid inputs
    Given I am logged in with email "declined@gmail.com" and password "Testp4$$"
    And I am on the contractor registration form
    When I enter an about me with content "aboutMe"
    And I enter a location "17 Delph Street, Avonhead 8042, New Zealand", country "New Zealand", city "Christchurch", suburb "", street "17 Delph street" and postcode "8042"
    And I submit 2 valid image and one with type "image/jpeg" and size 2 MB
    And I click the contractor Submit button
    Then I am redirect to profile page

  Scenario: AC7 - Invalid file size
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I am on the contractor registration form
    When I enter an about me with content "aboutMe"
    And I enter a location "17 Delph Street, Avonhead 8042, New Zealand", country "New Zealand", city "Christchurch", suburb "", street "17 Delph street" and postcode "8042"
    And I submit 2 valid image and one with type "image/jpeg" and size 12 MB
    And I click the contractor Submit button
    Then I remain on the contractor page, and I see the error "Files must be no greater than 10MB in size" from "errorImageSize"

  Scenario Outline: AC8 - Invalid file type
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    And I am on the contractor registration form
    When I enter an about me with content "aboutMe"
    And I enter a location "17 Delph Street, Avonhead 8042, New Zealand", country "New Zealand", city "Christchurch", suburb "", street "17 Delph street" and postcode "8042"
    And I submit 0 valid image and one with type <fileType> and size 2 MB
    And I click the contractor Submit button
    Then I remain on the contractor page, and I see the error "Invalid file type" from "errorImageFormat"
    Examples:
      | fileType             |
      | "application/pdf"    |
      | "audio/vorbis"       |
      | "text/plain"         |
      | "multipart/form-data" |

  Scenario: AC11 - Empty about me
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    When I enter an about me with content ""
    And I enter a location "17 Delph Street, Avonhead 8042, New Zealand", country "New Zealand", city "Christchurch", suburb "", street "17 Delph street" and postcode "8042"
    And I submit 0 valid image and one with type "image/jpeg" and size 0 MB
    And I click the contractor Submit button
    Then I remain on the contractor page, and I see the error "You must provide a description" from "errorDescription"
