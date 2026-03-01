Velora Project Structure
Velora is a business management system designed to bridge the trust and complexity gap for small shop owners. This project follows a Domain-Centric Architecture, ensuring business rules are isolated from technical implementations.

📂 Directory Layout
Plaintext
velora-system/
├── pom.xml                        # Maven project configuration
├── src/
│   ├── main/
│   │   ├── java/com/velora/app/
│   │   │   │
│   │   │   ├── core/              # THE BRAIN (Pure Java, no SQL/UI logic)
│   │   │   │   ├── domain/        # Entities & Data Models (Shop, User, Product)
│   │   │   │   ├── service/       # Business Logic & Rule Validation
│   │   │   │   └── repository/    # Data Access Contracts (Interfaces)
│   │   │   │
│   │   │   ├── infrastructure/    # THE TOOLS (Technical Implementations)
│   │   │   │   ├── db/            # JDBC & PostgreSQL Implementation (DAOs)
│   │   │   │   ├── ui/            # Presentation Layer (JavaFX/Swing)
│   │   │   │   └── util/          # Database Connections & Helpers
│   │   │   │
│   │   │   ├── common/            # Shared Utilities & Custom Exceptions
│   │   │   └── Main.java          # Application Entry Point (Manual DI)
│   │   │
│   │   └── resources/             # Configuration & Database Scripts
│   │       ├── application.properties
│   │       └── schema.sql
│   │
│   └── test/                      # Unit Testing (JUnit 5)
🏗️ Layer Responsibilities
1. Core Layer (/core)
This layer contains the "Soul" of the application. It is independent of the database and the UI.

Domain: Defined using Encapsulation and Inheritance. Classes like Product and Shop include internal validation (e.g., ensuring prices are not negative).

Service: The application logic. It coordinates tasks (like processing a sale) and enforces business rules.

Repository: Contains Java Interfaces. It defines what data is needed without caring how it is fetched.

2. Infrastructure Layer (/infrastructure)
This layer handles the "How" of the system.

DB: Contains the concrete implementations of the Repository interfaces using Raw JDBC to communicate with PostgreSQL.

UI: The graphical interface. It interacts only with the service layer, never directly with the db.

Util: Includes the DatabaseConfig (Singleton) to manage connection pools and resource cleanup.

3. Entry Point (Main.java)
The application starts here. Since this is a pure Java project (No Spring), the Main class performs Manual Dependency Injection:

Initialize the Database Connection.

Instantiate the Repository implementations.

Inject Repositories into Services.

Inject Services into the UI and launch.

🛠️ Tech Stack (Phase 1)
Language: Java SE (Standard Edition)

Build Tool: Maven

Database: PostgreSQL

Persistence: JDBC (DAO Pattern)

Testing: JUnit 5