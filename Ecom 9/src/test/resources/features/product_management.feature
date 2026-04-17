Feature: Product Management Functionality

  Scenario: Add a product successfully
    Given an authenticated admin user
    When they add a new product with name "Test Product" and price 9.99
    Then the product should be added successfully with name "Test Product"

  Scenario: Update an existing product successfully
    Given an authenticated admin user
    When they update the product with ID 1 with name "Updated" and price 12.99
    Then the product should be updated with name "Updated"

  Scenario: Get product by ID that does not exist
    Given an authenticated admin user
    When they get product by ID 999
    Then a "Product not found with id" exception should be thrown

  Scenario: List all products
    Given an authenticated admin user
    When they list all products
    Then the product list should contain 2 products

  Scenario: Delete product successfully
    Given an authenticated admin user
    When they delete the product with ID 1
    Then the product should be deleted successfully

  Scenario: Delete product as unauthenticated user
    Given an unauthenticated user
    When they attempt to delete a product with ID 1
    Then a SecurityException with message "User is not authenticated" should be thrown

  Scenario: Delete product as non-admin user
    Given an authenticated non-admin user
    When they attempt to delete a product with ID 1
    Then a SecurityException with message "Access denied. Admin privileges required." should be thrown

  Scenario: Update product that does not exist
    Given an authenticated admin user
    When they try to update non-existing product with ID 123
    Then a "Product not found with id" exception should be thrown

  Scenario: Update product with duplicate name
    Given an authenticated admin user
    When they try to update product with duplicate name
    Then a "Product with this name already exists" exception should be thrown

  Scenario: Update product with empty name
    Given an authenticated admin user
    When they try to update product with empty name
    Then a "Product name cannot be empty" exception should be thrown

  Scenario: Delete product that does not exist
    Given an authenticated admin user
    When they try to delete a non-existing product with ID 999
    Then the product should not be deleted
