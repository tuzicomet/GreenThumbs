Feature: U20009 - Edit unassigned Service Requests
  As Sarah, I want to be able to edit my unassigned service requests, those that do not have a contractor assigned,
  so that I can update details or make changes.

  Scenario: AC1 - Edit form redirect
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the service request details page
    When I click the edit button
    Then I am redirected to the edit service request form

  Scenario: AC2 - Edit form populated when opened
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the service request details page
    When I click the edit button
    Then The edit form is prefilled with the current information


  Scenario: AC4 - Valid inputs
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter valid values for the service request
    And I click the edit service request form's submit button
    Then I am taken to my service requests and the service request is updated

  Scenario Outline: AC5.1 - Invalid title
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a title of <title>
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the title error "Title cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"
    Examples:
      | title        |
      |  "title3"    |
      |  "TITLE#"    |
      |  "tit-le6"   |
      |  "ti tle=y"  |
      |  "title's["  |
      |  "tit.le+"   |
      |  "-------"   |

  Scenario Outline: AC5.2 - Invalid description
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a description of <description>
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the description error "Description cannot be empty and must only include letters, spaces, hyphens or apostrophes, and cannot consist solely of non-alphabetical characters"
    Examples:
      | description  |
      |  "desc 3"    |
      |  "DESC #"    |
      |  "des-cr6"   |
      |  "td tf3=y"  |
      |  "erbs's["  |
      |  "%27.le+"   |
      |  "-------"   |

  Scenario Outline: AC9.1.1 - Invalid minimum price
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a minimum price of <minimumPrice>
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the minimum price error "Minimum price must be valid and between 0 and 100000 (inclusive)."
    Examples:
      | minimumPrice |
      |  "-1"        |
      |  "100001"    |
      |  "1,"        |
      |  "1."        |
      |  "1.111"     |
      |  "1,1111"    |

  Scenario Outline: AC9.1.2 - Invalid maximum price
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a maximum price of <maximumPrice>
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the maximum price error "Maximum price must be valid and between 0 and 100000 (inclusive)."
    Examples:
      | maximumPrice |
      |  "-1"        |
      |  "100001"    |
      |  "1,"        |
      |  "1."        |
      |  "1.111"     |
      |  "1,1111"    |

  Scenario: AC10 - Invalid date format
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a earliest date with content "12/31/2025"
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the earliest date error "Date is not in valid format, (DD/MM/YYYY)"

  Scenario: AC11 - Latest date is before earliest date
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a earliest date that is 3 days away
    And I enter a latest date that is 2 days away
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the latest date error "Latest date must not be before the earliest date"

  Scenario Outline: AC12 - Maximum price is lower than minimum price
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am on the edit service request form
    When I enter a minimum price of <minimumPrice>, and a maximum price of <maximumPrice>
    And I enter valid values for the remaining fields
    And I click the service request form's submit button
    Then I remain on the service request form page, and I see the maximum price error "Maximum price cannot be less than the minimum price."
    Examples:
      | minimumPrice | maximumPrice |
      |  "1"         |  "0"         |
      |  "10"        |  "9.99"      |
      |  "1,99"      |  "1,98"      |
      |  "1.01"      |  "1.00"      |
      |  "100000"    |  "1"         |
      |  "999"       |  "99"        |
