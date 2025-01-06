Feature: U2008 - View Assigned Jobs
  As Ava, I want to view the jobs I’ve been assigned to so that I can organize my work schedule and ensure I complete tasks on time.


  Scenario: AC1.1 - Contractor Access My Jobs Page
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I open the My Jobs page
    Then I am shown the My Jobs page

  Scenario: AC1.2 - Non-contractor Access My Jobs Page
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    When I open the My Jobs page
    Then I am redirected to the Homepage


  Scenario: AC3 - View Current Tab
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I open the My Jobs page
    And I am on the "current" tab
    Then I see all of the incomplete jobs I am assigned to
    And The jobs are sorted by most to least recent

  Scenario: AC5 & AC6 - View Past Tab
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I open the My Jobs page
    And I am on the "past" tab
    Then I see all of the completed jobs I am assigned to
    And The jobs are sorted by most to least recent