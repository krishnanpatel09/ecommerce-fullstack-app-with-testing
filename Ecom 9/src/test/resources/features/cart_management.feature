Feature: Cart Management
  As a customer
  I want to manage the products in my shopping cart
  So that I can review, update, and eventually checkout my order

  Scenario: Customer adds a product to the cart
    Given a product with ID 1, price 10.0, and stock 20
    When the customer adds 2 of product with ID 1 to the cart
    Then the cart should contain the product and total amount should be 20.0

  Scenario: Customer attempts to add product exceeding available stock
    Given a product with ID 1, price 10.0, and stock 1
    When the customer tries to add 5 of product with ID 1 exceeding stock
    Then an error should be thrown with message "Not enough stock available"

  Scenario: Customer updates the quantity of an item in the cart
    Given a product with ID 1, price 10.0, and stock 10
    And the customer adds 1 of product with ID 1 to the cart
    And the customer updates the quantity of product with ID 1 to 3
    Then the cart should reflect updated subtotal of 30.0

  Scenario: Customer removes a product from the cart
    Given a product with ID 1, price 10.0, and stock 10
    And the customer adds 1 of product with ID 1 to the cart
    And the customer removes the product with ID 1 from the cart
    Then the cart should be empty with total amount 0.0

  Scenario: Customer clears the cart
    Given a product with ID 1, price 10.0, and stock 10
    And the customer adds 2 of product with ID 1 to the cart
    And the customer clears the cart
    Then the cart should be empty with total amount 0.0

  Scenario: Customer proceeds to checkout
    Given a product with ID 1, price 10.0, and stock 10
    And the customer adds 2 of product with ID 1 to the cart
    And the customer proceeds to checkout
    Then the cart should be empty with total amount 0.0
