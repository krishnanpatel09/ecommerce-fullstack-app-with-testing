Feature: Order Management
  As a customer
  I want to place an order based on my cart
  So that I can complete my purchase

  Scenario: Customer places an order successfully
    Given a product with ID 1, price 10.0, and stock 20
    When the customer places an order
    Then an order should be created with total amount 100.0
