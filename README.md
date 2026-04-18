<div align="center"> <h1>🛒 E-Commerce Full Stack Application with Testing</h1> <p> Full stack e-commerce system built with modern software engineering practices including Agile, TDD, and BDD. </p> <p> <a href="https://www.oracle.com/java/" target="_blank"> <img src="https://img.shields.io/badge/Java-21-orange" /> </a> <a href="https://spring.io/projects/spring-boot" target="_blank"> <img src="https://img.shields.io/badge/Spring%20Boot-Backend-green" /> </a> <a href="https://react.dev/" target="_blank"> <img src="https://img.shields.io/badge/React-Frontend-blue" /> </a> <a href="https://jwt.io/" target="_blank"> <img src="https://img.shields.io/badge/JWT-Security-red" /> </a> <a href="https://junit.org/junit5/" target="_blank"> <img src="https://img.shields.io/badge/JUnit-Testing-yellow" /> </a> <a href="https://cucumber.io/" target="_blank"> <img src="https://img.shields.io/badge/Cucumber-BDD-success" /> </a> </p> </div>
<h2>📌 Overview</h2> <p> This project is a full stack e-commerce platform that simulates a real-world online shopping system. It enables users to browse products, manage carts, place orders, and track purchases, while providing administrators with full control over products, users, and order management. </p> <p> The system is built using a layered architecture with a React frontend and Spring Boot backend, secured using JWT authentication. It follows Agile development practices and integrates Test-Driven Development (TDD) and Behavior-Driven Development (BDD) for high-quality, maintainable code. </p>
<h2>🎯 Project Motivation</h2> <ul> <li>Build a scalable and maintainable full stack application</li> <li>Apply real-world Agile development using Jira</li> <li>Implement secure authentication using JWT</li> <li>Demonstrate TDD and BDD in a production-style system</li> </ul>
<h2>🚀 Key Features</h2> <ul> <li>JWT-based authentication and authorization</li> <li>Role-based access (Admin and Customer)</li> <li>Product management (CRUD operations)</li> <li>Shopping cart and checkout workflow</li> <li>Order processing and history tracking</li> <li>Admin dashboard for system control</li> <li>Secure REST API communication</li> <li>Extensive automated testing</li> </ul>
<h2>🏗️ System Architecture</h2> <p> The system follows a clean layered architecture using MVC principles: </p> <ul> <li><b>Frontend:</b> React for UI and state management</li> <li><b>Backend:</b> Spring Boot REST APIs</li> <li><b>Database:</b> SQLite</li> <li><b>Security:</b> Spring Security with JWT</li> </ul> <p> Each layer is decoupled to improve scalability and maintainability. </p>
<h2>⚙️ Development Approach (Agile + Jira)</h2> <p> The project was developed using Agile methodology with structured sprint planning and execution using Jira. </p> <ul> <li>Project divided into <b>4 sprints</b></li> <li>Each sprint contained well-defined user stories</li> <li>Tasks assigned using <b>story points</b> for effort estimation</li> <li>Features prioritized using backlog prioritization</li> <li>Continuous tracking using Jira boards</li> </ul> <p><b>Sprint Breakdown:</b></p> <ul> <li><b>Sprint 1:</b> User authentication and basic setup</li> <li><b>Sprint 2:</b> Product and admin functionality</li> <li><b>Sprint 3:</b> Cart and order management</li> <li><b>Sprint 4:</b> Testing, bug fixes, and refinement</li> </ul> <p> Agile enabled iterative development, faster feedback, and better feature validation. </p>
<h2>🧪 Testing Strategy (TDD + BDD)</h2> <h3>Test-Driven Development (TDD)</h3> <ul> <li>Tests written before implementing business logic</li> <li>Used JUnit for service and controller testing</li> <li>Ensured each feature passes defined test cases before deployment</li> </ul> <p><b>Covered Areas:</b></p> <ul> <li>Authentication and login</li> <li>Product management</li> <li>Cart operations</li> <li>Order processing</li> </ul>
<h3>Behavior-Driven Development (BDD)</h3> <ul> <li>Implemented using Cucumber</li> <li>Feature files written in Gherkin syntax</li> <li>Simulates real user scenarios</li> </ul> <p><b>Example Features:</b></p> <ul> <li>User registration</li> <li>Product management</li> <li>Cart workflow</li> <li>Order placement</li> </ul>
<h3>Testing Benefits</h3> <ul> <li>Improved code reliability</li> <li>Early bug detection</li> <li>Better alignment with user requirements</li> <li>High test coverage across modules</li> </ul>
<h2>📂 Data Model</h2> <ul> <li>Users</li> <li>Products</li> <li>Carts and Cart Items</li> <li>Orders and Order Items</li> </ul> <p> Relational schema and ER design ensure data integrity and efficient queries. </p>
<h2>🧰 Tech Stack</h2> <table> <tr><th>Layer</th><th>Technology</th></tr> <tr><td>Frontend</td><td>React, Context API, CSS</td></tr> <tr><td>Backend</td><td>Spring Boot, Spring Security</td></tr> <tr><td>Authentication</td><td>JWT</td></tr> <tr><td>Database</td><td>SQLite</td></tr> <tr><td>Testing</td><td>JUnit, Cucumber</td></tr> <tr><td>Build Tool</td><td>Maven</td></tr> <tr><td>Project Management</td><td>Jira (Agile)</td></tr> </table>
<h2>📁 Project Structure</h2> 
ecommerce-fullstack-app-with-testing/
├── frontend/
│   ├── public/
│   │   ├── index.html
│   │   ├── manifest.json
│   │   └── Logo192.png
│   │
│   ├── src/
│   │   ├── components/
│   │   │   ├── admin/
│   │   │   │   ├── AdminDashboard.js
│   │   │   │   ├── ProductManagement.js
│   │   │   │   └── UserManagement.js
│   │   │   │
│   │   │   ├── auth/
│   │   │   │   ├── Login.js
│   │   │   │   ├── Register.js
│   │   │   │   └── ProtectedRoute.js
│   │   │   │
│   │   │   ├── customer/
│   │   │   │   ├── ProductList.js
│   │   │   │   ├── Cart.js
│   │   │   │   ├── Checkout.js
│   │   │   │   ├── OrderHistory.js
│   │   │   │   └── Payment.js
│   │   │   │
│   │   │   └── common/
│   │   │       ├── Home.js
│   │   │       └── Navigation.js
│   │   │
│   │   ├── context/
│   │   │   └── AuthContext.js
│   │   │
│   │   ├── App.js
│   │   └── index.js
│   │
│   ├── package.json
│   └── package-lock.json
│
├── src/
│   ├── main/
│   │   ├── java/com/TDD/Ecom/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── security/
│   │   │   ├── service/
│   │   │   └── EcomApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql
│   │       ├── data.sql
│   │       └── features/
│   │
│   └── test/
│       ├── java/
│       │   ├── service/
│       │   ├── stepDefinitions/
│       │   └── CucumberTestRunner.java
│       │
│       └── resources/
│           └── features/
│
├── .mvn/
├── mvnw
├── mvnw.cmd
├── pom.xml
├── Ecom.db
├── .gitignore
└── README.md

<h2>🛠️ Installation</h2> <h3>Prerequisites</h3> <ul> <li>Java 21+</li> <li>Maven</li> <li>Node.js (v16 or higher)</li> <li>npm</li> </ul>
<h3>Backend Setup (Spring Boot)</h3> <pre> # Navigate to project root cd ecommerce-fullstack-app-with-testing # Run Spring Boot application ./mvnw spring-boot:run </pre> <p> Backend will start at: http://localhost:8080 </p>
<h3>Frontend Setup (React)</h3> <pre> # Navigate to frontend folder cd frontend # Install dependencies npm install # Start frontend npm start </pre> <p> Frontend will start at: http://localhost:3000 </p>
<h3>Database</h3> <ul> <li>SQLite database is preconfigured</li> <li>Schema and sample data auto-load using <code>schema.sql</code> and <code>data.sql</code></li> <li>No manual setup required</li> </ul>
<h3>Notes</h3> <ul> <li>Ensure backend is running before starting frontend</li> <li>Frontend uses proxy to connect to backend (localhost:8080)</li> </ul>

<h2>▶️ Usage</h2> <ul> <li>Frontend: http://localhost:3000</li> <li>Backend: http://localhost:8080</li> <li>Register or login to access features</li> </ul>
p
<h2>📊 Performance & Insights</h2> <ul> <li>Layered architecture improves maintainability</li> <li>TDD reduces production bugs</li> <li>JWT ensures secure communication</li> <li>Agile improves delivery speed</li> </ul>
<h2>📚 Key Learnings</h2> <ul> <li>Agile project execution using Jira</li> <li>Implementing TDD and BDD together</li> <li>Designing scalable backend APIs</li> <li>Building secure authentication systems</li> </ul>
<h2>⚠️ Limitations</h2> <ul> <li>SQLite not suitable for large-scale deployment</li> <li>No real payment gateway integration</li> <li>Limited UI optimization</li> </ul>
<h2>🔮 Future Work</h2> <ul> <li>Integrate real payment systems</li> <li>Use PostgreSQL or MySQL</li> <li>Add recommendation engine</li> <li>Improve UI/UX</li> </ul>
<h2>💡 Why This Project Matters</h2> <p> This project demonstrates full stack development with industry practices. It shows the ability to build secure, testable, and scalable systems while following Agile workflows. </p>
<h2>👤 Authors</h2> <ul> <li>Krishna Nikunjkumar Patel</li> <li>Hinal Dharmendrabhai Vasava</li> <li>Farnoush Kashanirad</li> </ul>
