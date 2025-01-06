Feature: U2004 - Browse Available Jobs
  As Ava, I want to browse the list of available jobs so that I can find new opportunities that match my skills and availability.


  Scenario: AC1.1 - Contractor Accesses Available Jobs
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open the Available Jobs page
    Then I am shown the Available Jobs page

  Scenario: AC1.2 - Standard User Accesses Available Jobs
    Given I am logged in with email "pending@gmail.com" and password "Testp4$$"
    When I attempt to open the Available Jobs page
    Then I am redirected to the homepage

  Scenario: AC5.1 - Filter By Minimum Budget
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "101.0" for the minimum budget
    When I apply the filters
    Then I am only shown jobs that have a maximum budget that is greater than "101.0"
    And I see no error message


  Scenario: AC5.2 - Filter By Maximum Budget
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "101.0" for the maximum budget
    When I apply the filters
    Then I am only shown jobs that have a minimum budget that is less than "101.0"
    And I see no error message

  Scenario: AC5.3 - Filter By Minimum Date
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "08/12/2024" for the minimum date
    When I apply the filters
    Then I am only shown jobs that have a latest date that is after "08/12/2024"
    And I see no error message

  Scenario: AC5.4 - Filter By Maximum Date
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "09/12/2024" for the maximum date
    When I apply the filters
    Then I am only shown jobs that have a earliest date that is before "09/12/2024"
    And I see no error message

  Scenario: AC5.5 - Filter By Maximum Distance
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "350" for the maximum distance
    When I apply the filters
    Then I am only shown jobs that have a distance that is less than "350"
    And I see no error message

  Scenario: AC5.6 - Filter By Date Range
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "08/12/2024" for the minimum date
    And I have entered "17/12/2024" for the maximum date
    When I apply the filters
    Then I am only shown jobs that have a earliest date that is before "17/12/2024"
    And I am only shown jobs that have a latest date that is after "08/12/2024"
    And I see no error message

  Scenario: AC5.7 - Filter By Budget Range
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "101.0" for the minimum budget
    And I have entered "200.0" for the maximum budget
    When I apply the filters
    Then I am only shown jobs that have a maximum budget that is greater than "101.0"
    And I am only shown jobs that have a minimum budget that is less than "200.0"
    And I see no error message

  Scenario: AC5.8 - Filter By All Filters
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "101.0" for the minimum budget
    And I have entered "200.0" for the maximum budget
    And I have entered "09/12/2024" for the minimum date
    And I have entered "17/12/2024" for the maximum date
    And I have entered "350" for the maximum distance
    When I apply the filters
    Then I am only shown jobs that have a maximum budget that is greater than "101.0"
    And I am only shown jobs that have a minimum budget that is less than "200.0"
    And I am only shown jobs that have a earliest date that is before "17/12/2024"
    And I am only shown jobs that have a latest date that is after "09/12/2024"
    And I am only shown jobs that have a distance that is less than "350"
    And I see no error message


  Scenario Outline: AC5.9 - Invalid Minimum Budget
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "<invalidBudget>" for the minimum budget
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message

    Examples:
      | invalidBudget |
      | abc             |
      |20000000000000000|
      | 20abc           |
      | 2.3.4           |

  Scenario Outline: AC5.10 - Invalid Maximum Budget
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "<invalidBudget>" for the maximum budget
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message
    Examples:
      | invalidBudget |
      | abc             |
      |20000000000000000|
      | 20abc           |
      | 2.3.4           |

  Scenario Outline: AC5.11 - Invalid Minimum Date
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "<invalidDate>" for the minimum date
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message

    Examples:
      | invalidDate     |
      | abc             |
      |20               |
      | 09/13/2024      |
      | 02.03.2024      |
      | 09/13/24        |

  Scenario Outline: AC5.12 - Invalid Maximum Date
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "<invalidDate>" for the maximum date
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message

    Examples:
      | invalidDate     |
      | abc             |
      |20               |
      | 09/13/2024      |
      | 02.03.2024      |
      | 09/13/24        |

  Scenario Outline: AC5.13 - Invalid Maximum Distance
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "<invalidDistance>" for the maximum distance
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message

    Examples:
    | invalidDistance |
    | abc             |
    |20000000000000000|
    | 20abc           |
    | 2.3.4           |


  Scenario: AC5.13 - Multiple Invalid Fields
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "20000000000000" for the maximum budget
    And I have entered "320qxc" for the maximum distance
    And I have entered "30/2/2024" for the maximum date
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message

  Scenario: AC5.13 - Valid and Invalid fields
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And I have navigated to the Available Jobs page
    And I have entered "200" for the maximum budget
    And I have entered "320qxc" for the maximum distance
    And I have entered "25/2/2024" for the minimum date
    And I have entered "205/2/2024" for the maximum date
    When I apply the filters
    Then I am shown all jobs
    And I see at least one error message
  Scenario: AC6 - Pagination
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open the Available Jobs page
    Then I should see 10 jobs listed on the page
    And pagination should be available