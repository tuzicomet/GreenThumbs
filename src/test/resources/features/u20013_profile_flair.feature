Feature: U20013 - Contractor profile flair
  As Ava, I want to have an added flair on my profile image to indicate my “tier” of user so that I can showcase my experience and build trust with potential clients.

  Scenario: AC1 - No jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed 0 jobs
    When I am on the profile page
    Then profile image displays a "leaf" hat flair

  Scenario Outline: AC2 - 1 - 9 jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed <jobsCompleted> jobs
    When I am on the profile page
    Then profile image displays a "straw" hat flair

    Examples:
      | jobsCompleted |
      | 1             |
      | 4             |
      | 6             |
      | 9             |

  Scenario Outline: AC3 - 10 - 49 jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed <jobsCompleted> jobs
    When I am on the profile page
    Then profile image displays a "cowboy" hat flair

    Examples:
      | jobsCompleted |
      | 10            |
      | 26            |
      | 43            |
      | 49            |

  Scenario Outline: AC4 - 50 - 99 jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed <jobsCompleted> jobs
    When I am on the profile page
    Then profile image displays a "archer" hat flair

    Examples:
      | jobsCompleted |
      | 50            |
      | 54            |
      | 86            |
      | 99            |

  Scenario Outline: AC5 - 100 - 499 jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed <jobsCompleted> jobs
    When I am on the profile page
    Then profile image displays a "crown" hat flair

    Examples:
      | jobsCompleted |
      | 100           |
      | 234           |
      | 456           |
      | 499           |

  Scenario Outline: AC6 - 500 or more jobs completed
    Given I am logged in with email "contractor@gmail.com" and password "Testp4$$"
    And contractor has completed <jobsCompleted> jobs
    When I am on the profile page
    Then profile image displays a "flower_crown" hat flair

    Examples:
      | jobsCompleted |
      | 500           |
      | 634           |
      | 7886          |
      | 2147483647    |

  Scenario Outline: AC8 - Other contractors profile page flairs
    Given I am logged in with email "userwithgarden@gmail.com" and password "Testp4$$"
    And I am friends with user with email "contractor@gmail.com"
    And contractor has completed <jobsCompleted> jobs
    When I am on their profile page
    Then profile image displays a <flairName> hat flair

    Examples:
      | jobsCompleted | flairName      |
      | 0             | "leaf"         |
      | 1             | "straw"        |
      | 9             | "straw"        |
      | 10            | "cowboy"       |
      | 49            | "cowboy"       |
      | 50            | "archer"       |
      | 99            | "archer"       |
      | 100           | "crown"        |
      | 499           | "crown"        |
      | 500           | "flower_crown" |
      | 2147483647    | "flower_crown" |