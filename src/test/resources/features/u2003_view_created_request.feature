Feature: U2003 - View created requests for garden services
  As Sarah, I want to view my created service requests so that I can keep track of the services I have requested.

  Scenario: AC1 - I can view the my service requests page
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    When I click on the My Service Requests link
    Then I am shown the My Service Requests page