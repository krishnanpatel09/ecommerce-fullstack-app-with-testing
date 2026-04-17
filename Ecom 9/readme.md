1. Introduction
   The E-Commerce Management System aims to deliver a seamless, full-featured online shopping platform. Key goals include:

User Registration & Secure Login (JWT-based).

Product Browsing & Filtering to help customers find items by name or price.

Cart & Order Management for an intuitive buying process.

Admin Controls to manage products, orders, and user data.

Secure Transactions by validating payments and ensuring data integrity.

By combining React.js on the frontend and Spring Boot on the backend, this system is both modular and scalable, supporting a robust feature set while remaining easy to maintain and extend.

2. Key Features
   2.1 Customer Features
   User Registration & Login: Sign up with username, email, and password; log in to receive a JWT for subsequent requests.

Product Browsing: Search for products by name and filter them by price; view product details like images, prices, and descriptions.

Cart Management: Add items to the cart, update quantities, or remove them altogether.

Checkout & Payment: Provide secure card details (card number, expiry date, CVV) to place an order.

Order History: Track the status (processing, delivered, canceled) of past orders.

2.2 Admin Features
Secure Admin Login: Dedicated credentials validated by Spring Security.

Product CRUD: Add new products, update price and stock, remove outdated items, and handle product images.

Order Management: View all orders in the system, update their status, and review order details.

User Management: Toggle user access (blocked/unblocked), update user details, and remove suspicious or inactive accounts.

3. Technology Stack
   Frontend

React.js for building a responsive interface

React Router for in-app navigation

React Toastify for user-friendly notifications

Context API for global state management

CSS for styling

Backend

Spring Boot as the core framework

Spring Security & JWT for authentication and authorization

SQLite (LiteSQL) as the primary datastore

Maven for dependency management

Testing

JUnit for unit tests

Cucumber for behavior-driven (acceptance) tests

4. System Architecture
   The application follows a multi-tier MVC approach, separating responsibilities into logical layers:

Presentation Layer (React.js) – Renders UI components and communicates with the backend via RESTful calls.

Business Logic (Services, Spring Boot) – Encapsulates domain logic (e.g., inventory checks, order validation).

Data Access (Spring Data JPA & SQLite) – Stores and retrieves information such as users, products, carts, and orders.

Security & JWT – Ensures endpoints are protected and only valid tokens can access restricted resources.

4.1 Frontend (React.js)
Routing: React Router handles navigation (e.g., /login, /products, /cart).

State Management: Context API holds user tokens and cart data.

Notifications: React Toastify shows real-time status messages for common actions (adding items, checkout errors, etc.).

4.2 Backend (Spring Boot)
Controllers: REST endpoints (AuthController, ProductController, CartController, OrderController, etc.).

Services: Business logic (validating stock, calculating totals, handling order statuses).

Repositories: Spring Data JPA interfaces (UserRepository, ProductRepository, etc.) for CRUD operations on the SQLite database.

4.3 Security & JWT
JWT: Token-based authentication ensures each request is validated.

Spring Security: Role-based access for ADMIN vs. CUSTOMER.

JwtAuthenticationFilter intercepts requests to check authorization before allowing access.

4.4 Database (LiteSQL)
SQLite: A file-based database that is easy to configure.

Entities: Tables for users, products, carts, orders, and so on, managed via JPA.

5. Data Flow & Operations
   5.1 User Authentication
   Login: Credentials are posted to /auth/login.

JWT Issuance: A successful login returns a token stored on the frontend.

Secure Requests: Each subsequent request includes an Authorization header: Bearer <token>.

5.2 Product Actions
Public Access: Fetch product listings, filter by price or name.

Admin Access: Create, update, delete products, handle image uploads, and set stock levels.

5.3 Cart & Checkout
CartController manages adding items (POST /cart/addItem), updating quantities, or removing them.

Checkout finalizes the cart into an order and triggers payment validation.

5.4 Order Management
Customer: Views order history (GET /orders/history) and tracks status (processing, delivered, canceled).

Admin: Modifies order status, ensuring real-time monitoring of ongoing shipments.

5.5 User Management
Customers: Manage personal details (profile updates).

Admins: Retrieve all users, block/unblock users, or remove inactive accounts.

6. Development Process
   6.1 Agile Methodology
   We used Agile with short, iterative sprints, daily stand-ups, and continuous integration:

Sprint Planning: Defined tasks, user stories, and acceptance criteria.

Daily Stand-ups: Addressed blockers, updated progress.

Sprint Review: Demonstrated features, collected feedback, refined the product backlog.

6.2 Sprints Overview
Sprint 1 (Feb 1 – Feb 14) – User registration/login, admin block/unblock user.

Sprint 2 (Feb 15 – Mar 7) – Product CRUD, price filtering, file uploads.

Sprint 3 (Mar 8 – Mar 28) – Order creation, order history, stock validations.

Sprint 4 (Mar 29 – Apr 8) – Refactoring, minor bug fixes, performance improvements.

7. Test-Driven Development (TDD)
   We wrote failing tests for each new feature before implementing the actual code:

Write a Test: Example: CartService.addItemToCart must calculate the total amount.

Fail: The test fails as no implementation exists yet.

Implement: Write minimal code to pass the test.

Refactor: Improve code clarity and maintainability, ensuring tests still pass.

This red-green-refactor cycle ensured we addressed edge cases early and maintained a clean, robust codebase.

8. Testing Strategy
   8.1 Unit Testing (JUnit)
   AdminServiceTest: Ensures only ADMIN role can manage products, orders, and user data.

CartServiceTest: Validates cart totals, handling stock checks and error conditions.

ProductServiceTest: Tests product creation, retrieval, update, and deletion logic.

UserRegistrationTest: Confirms user details and checks boundary conditions (duplicate emails, invalid input).

8.2 Acceptance Testing (Cucumber)
Feature Files: Written in plain language describing user stories (e.g., “As a customer, I can add products to my cart”).

Step Definitions: Executed against the real application, ensuring it behaves as described.

8.3 Test Coverage Analysis
Overall Coverage: ~47% class coverage, 54% method coverage, 42% line coverage, and 8% branch coverage across packages (service, security, repository), etc.

Gaps: Limited coverage in controllers and certain security paths. Additional integration tests could improve these metrics.

9. Code Refactoring
   We performed extensive refactoring to enhance maintainability:

Centralized Authentication Logic: Replaced scattered SecurityContextHolder calls with a single authenticateUser() helper method.

Mocking & Data Setup: Introduced mockDependenciesForCart() and addItemToCart() in CartServiceTest to reduce boilerplate.

Naming Improvements: Emphasized concise, self-explanatory method names to make the test suite more readable.

10. Lessons Learned
    Requirement Gathering: Early user story definitions prevent scope creep.

Agile & Iterations: Short sprints boost adaptability and continuous feedback.

TDD: Writing tests first clarifies features and catches bugs early.

Security & Roles: Integrating JWT and Spring Security from the start simplifies dev.

Refactoring: Regular cleanup keeps the codebase sustainable.

Documentation: Up-to-date README, code comments, and wikis streamline team onboarding.

11. Getting Started
    11.1 Prerequisites
    Java 17+

Maven (for backend dependencies)

Node.js & npm (for React.js frontend)

IDE (e.g., IntelliJ IDEA, VSCode)

11.2 Installation & Setup
Clone the Repository

bash
Copy
Edit
git clone https://github.com/YourUsername/EcomManagementSystem.git
cd EcomManagementSystem
Backend Setup

Navigate to the backend folder:

bash
Copy
Edit
cd backend
mvn clean install
mvn spring-boot:run
Confirm Spring Boot starts on localhost:8000 (default).

Frontend Setup

Navigate to the frontend folder:

bash
Copy
Edit
cd ../frontend
npm install
npm start
React app should start on localhost:3000.

Database (SQLite)

By default, an SQLite file (database.db) is used/stored in the backend’s working directory.

No extra configuration is needed unless you wish to migrate to a different DB engine.

Access the Application

Open a browser and go to http://localhost:3000.

Try registering a user, logging in, browsing products, etc.

12. Contributing
    We welcome pull requests and issue submissions. For major changes, please open an issue first to discuss what you would like to change.

Fork this repository.

Create a feature branch (git checkout -b feature/newFeature).

Commit changes (git commit -m 'Add new feature').

Push to the branch (git push origin feature/newFeature).

Open a new Pull Request.

13. License
    This project is released under MIT License. You’re free to use, modify, and distribute it, but please give appropriate credit to the original authors.