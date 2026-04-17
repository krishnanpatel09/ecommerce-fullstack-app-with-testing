Feature: User Registration and Authentication
  As a user
  I want to register and login to the system
  So that I can access the e-commerce platform

  Scenario: Successful user registration
    Given a new user provides valid details:
      | username | email             | password     | role |
      | testuser | test@example.com  | Password123! | USER |
    When the user submits the registration form
    Then the system should create an account successfully
    And the response should contain "USER"

  Scenario: Registration with existing email
    When a new user tries to register with the same email:
      | username | email             | password     | role |
      | newuser  | test@example.com  | Password123! | USER |
    Then the registration should fail
    And the error message should be "Email already exists"

  Scenario: Registration with invalid email format
    When a user tries to register with invalid email:
      | username | email         | password     | role |
      | testuser | invalid-email | Password123! | USER |
    Then the registration should fail
    And the error message should be "Invalid email format"

  Scenario: Registration with empty password
    When a user tries to register with empty password:
      | username | email            | password | role |
      | testuser | test@example.com |          | USER |
    Then the registration should fail
    And the error message should be "Password cannot be empty"

  Scenario: Successful user login
    Given a registered user exists:
      | username | email            | password     | role |
      | testuser | test@example.com | Password123! | USER |
    When the user attempts to login with correct credentials
    Then the login should be successful
    And the response should contain "fake-jwt-token"

  Scenario: Failed login with incorrect password
    Given a registered user exists:
      | username | email            | password     | role |
      | testuser | test@example.com | Password123! | USER |
    When the user attempts to login with incorrect password
    Then the login should fail
    And the error message should be "Invalid password"
