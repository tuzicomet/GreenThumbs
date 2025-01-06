Feature: U2007 - View Service Request Details
  As Ava, I want to be able to view the Service Request details page for a Service Request, so I can consider applying for it.


  Scenario: AC1.1 - Contractor Accesses Service Request Details
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open a Service Request Details page
    Then I am shown the Service Request Details page

#    -------------------------------

  Scenario: AC1.2 - Contactor sees earliest and latest Date
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open a Service Request Details page
    Then I am shown the Service Request Details page
    And I can see min and max values for date

  Scenario: AC1.2 - Contactor sees minimum and maximum Price
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open a Service Request Details page
    Then I am shown the Service Request Details page
    And I can see min and max values for price

  Scenario: AC1.2 - Contactor sees description for job
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    When I attempt to open a Service Request Details page
    Then I am shown the Service Request Details page
    And I can see job title and description



#    -----------------------------

  Scenario: AC2.1 - Standard User Accesses Service Request Details page they don't own
    Given I am logged in with email "verifieduser@gmail.com" and password "Testp4$$"
    When I attempt to open a Service Request Details page
    Then I am redirected to my service requests

  Scenario: AC2.2 - Owner of a request can see its details page
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I own a service
    When I attempt to open a Service Request Details page of my request
    Then I am shown the Service Request Details page

  Scenario: AC3.1 - Creator of a service request can edit the request
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I own a service
    When I attempt to open a Service Request Details page
    Then I see a button to edit the service request and there is no apply button

    Scenario: AC4.1 - Contractor does not own the request they can apply to the request
      Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
      When I attempt to open a Service Request Details page
      Then I can see a button to apply the service request and I cannot edit it

