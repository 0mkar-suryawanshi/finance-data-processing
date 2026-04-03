# Finance Data Processing & Access Control Backend

A backend system for a **finance dashboard** built with Java Spring Boot. It manages financial records, enforces role-based access control, and exposes summary-level analytics APIs.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
  - [Auth](#auth)
  - [Users](#users)
  - [Financial Records](#financial-records)
  - [Dashboard Summary](#dashboard-summary)
- [Role & Access Control](#role--access-control)
- [Validation & Error Handling](#validation--error-handling)
- [Swagger / API Docs](#swagger--api-docs)
- [Assumptions & Tradeoffs](#assumptions--tradeoffs)

---

## Tech Stack

| Layer           | Technology                                  |
|-----------------|---------------------------------------------|
| Language        | Java 21                                     |
| Framework       | Spring Boot 4.0.5                           |
| Security        | Spring Security + JWT (JJWT 0.11.5)        |
| Persistence     | Spring Data JPA + Hibernate                 |
| Database        | MySQL                                       |
| Validation      | Spring Boot Validation (Jakarta Bean)       |
| Boilerplate     | Lombok                                      |
| API Docs        | SpringDoc OpenAPI (Swagger UI) 3.0.1        |
| Build Tool      | Maven                                       |

---

## Project Structure

```
src/
└── main/
    └── java/com/finance/finance_data_processing/
        ├── config/          # Security config, JWT filter, CORS
        ├── controller/      # REST controllers (Auth, User, Record, Dashboard)
        ├── service/         # Business logic layer
        ├── repository/      # Spring Data JPA repositories
        ├── model/           # JPA entities (User, FinancialRecord)
        ├── dto/             # Request and response DTOs
        ├── enums/           # Role, RecordType enums
        ├── exception/       # Custom exceptions, global exception handler
        └── FinanceDataProcessingApplication.java
    └── resources/
        └── application.properties
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+

### Setup

```bash
# Clone the repository
git clone https://github.com/0mkar-suryawanshi/finance-data-processing.git
cd finance-data-processing

# Create the MySQL database
mysql -u root -p
CREATE DATABASE finance_db;
exit;

# Configure application.properties (see Configuration section below)

# Build and run
./mvnw spring-boot:run
```

The server will start at `http://localhost:8080`.

---

## Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/finance_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
app.jwt.secret=your_jwt_secret_key_here
app.jwt.expiration-ms=86400000

# Server
server.port=8080
```

---

## Database Schema

### `users`
| Column      | Type         | Notes                                    |
|-------------|--------------|------------------------------------------|
| id          | BIGINT (PK)  | Auto-increment                           |
| name        | VARCHAR      |                                          |
| email       | VARCHAR      | Unique                                   |
| password    | VARCHAR      | BCrypt hashed                            |
| role        | ENUM         | `ADMIN`, `ANALYST`, `VIEWER`             |
| is_active   | BOOLEAN      | Default: true                            |
| created_at  | TIMESTAMP    | Auto-set on creation                     |

### `financial_records`
| Column      | Type         | Notes                                    |
|-------------|--------------|------------------------------------------|
| id          | BIGINT (PK)  | Auto-increment                           |
| amount      | DECIMAL      |                                          |
| type        | ENUM         | `INCOME`, `EXPENSE`                      |
| category    | VARCHAR      | e.g., Salary, Rent, Travel               |
| date        | DATE         |                                          |
| notes       | VARCHAR      | Optional description                     |
| created_by  | BIGINT (FK)  | References `users.id`                    |
| is_deleted  | BOOLEAN      | Soft delete flag, default: false         |
| created_at  | TIMESTAMP    | Auto-set on creation                     |
| updated_at  | TIMESTAMP    | Auto-updated on modification             |

---

## API Reference

All protected routes require the `Authorization: Bearer <token>` header.

### Auth

| Method | Endpoint               | Description             | Access  |
|--------|------------------------|-------------------------|---------|
| POST   | `/api/auth/register`   | Register a new user     | Public  |
| POST   | `/api/auth/login`      | Login and receive JWT   | Public  |

**POST `/api/auth/register`**
```json
// Request
{
  "name": "Omkar Suryawanshi",
  "email": "omkar@example.com",
  "password": "password123",
  "role": "ADMIN"
}

// Response 201
{
  "message": "User registered successfully"
}
```

**POST `/api/auth/login`**
```json
// Request
{ "email": "omkar@example.com", "password": "password123" }

// Response 200
{
  "token": "<jwt>",
  "type": "Bearer",
  "id": 1,
  "name": "Omkar Suryawanshi",
  "email": "omkar@example.com",
  "role": "ADMIN"
}
```

---

### Users

| Method | Endpoint            | Description                | Access |
|--------|---------------------|----------------------------|--------|
| GET    | `/api/users`        | Get all users              | Admin  |
| GET    | `/api/users/{id}`   | Get a user by ID           | Admin  |
| PATCH  | `/api/users/{id}`   | Update user role or status | Admin  |
| DELETE | `/api/users/{id}`   | Deactivate a user          | Admin  |

**PATCH `/api/users/{id}`**
```json
// Request
{ "role": "ANALYST", "isActive": false }
```

---

### Financial Records

| Method | Endpoint               | Description                      | Access                  |
|--------|------------------------|----------------------------------|-------------------------|
| POST   | `/api/records`         | Create a new record              | Admin                   |
| GET    | `/api/records`         | List all records (with filters)  | Admin, Analyst, Viewer  |
| GET    | `/api/records/{id}`    | Get a single record              | Admin, Analyst, Viewer  |
| PUT    | `/api/records/{id}`    | Update a record                  | Admin                   |
| DELETE | `/api/records/{id}`    | Soft-delete a record             | Admin                   |

**POST `/api/records`**
```json
// Request
{
  "amount": 15000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-04-01",
  "notes": "April salary"
}
```

**Query Parameters for `GET /api/records`:**

| Param    | Type   | Description               | Example      |
|----------|--------|---------------------------|--------------|
| type     | String | Filter by record type     | `INCOME`     |
| category | String | Filter by category        | `Rent`       |
| from     | Date   | Start date (yyyy-MM-dd)   | `2024-01-01` |
| to       | Date   | End date (yyyy-MM-dd)     | `2024-12-31` |
| page     | int    | Page number (default: 0)  | `0`          |
| size     | int    | Page size (default: 10)   | `10`         |

---

### Dashboard Summary

| Method | Endpoint                      | Description                         | Access         |
|--------|-------------------------------|-------------------------------------|----------------|
| GET    | `/api/dashboard/summary`      | Total income, expenses, net balance | Admin, Analyst |
| GET    | `/api/dashboard/by-category`  | Category-wise breakdown             | Admin, Analyst |
| GET    | `/api/dashboard/trends`       | Monthly income vs expense trends    | Admin, Analyst |
| GET    | `/api/dashboard/recent`       | Recent 10 transactions              | All roles      |

**GET `/api/dashboard/summary`** — Example Response:
```json
{
  "totalIncome": 85000.00,
  "totalExpenses": 42300.00,
  "netBalance": 42700.00
}
```

**GET `/api/dashboard/trends?year=2024`** — Example Response:
```json
[
  { "month": "JANUARY",  "income": 10000.00, "expense": 4200.00 },
  { "month": "FEBRUARY", "income": 9500.00,  "expense": 3800.00 }
]
```

---

## Role & Access Control

Access control is enforced using **Spring Security** with a custom **JWT authentication filter** and method-level annotations (`@PreAuthorize`).

| Role       | Permissions                                                                     |
|------------|---------------------------------------------------------------------------------|
| `ADMIN`    | Full access — manage users, create/edit/delete records, view all dashboard data |
| `ANALYST`  | Read records, access all dashboard summaries and trends                         |
| `VIEWER`   | Read records and recent activity only                                           |

**How it works:**
1. `JwtAuthFilter` — Intercepts every request, validates the JWT, and populates the `SecurityContext`.
2. `@PreAuthorize("hasRole('ADMIN')")` — Applied on controller methods to enforce role checks declaratively.
3. Unauthenticated requests receive `401 Unauthorized`; insufficient role receives `403 Forbidden`.

---

## Validation & Error Handling

Input validation uses **Jakarta Bean Validation** annotations (`@NotNull`, `@NotBlank`, `@Positive`, `@PastOrPresent`, etc.) on request DTOs. A `@RestControllerAdvice` global exception handler catches and formats all errors consistently.

**Standard error response format:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be a positive number",
  "timestamp": "2024-04-01T10:30:00Z"
}
```

| Scenario                      | HTTP Status               |
|-------------------------------|---------------------------|
| Successful creation           | 201 Created               |
| Successful retrieval/update   | 200 OK                    |
| Validation failure            | 400 Bad Request           |
| Unauthenticated request       | 401 Unauthorized          |
| Insufficient role             | 403 Forbidden             |
| Resource not found            | 404 Not Found             |
| Internal server error         | 500 Internal Server Error |

---

## Swagger / API Docs

Interactive API documentation is available via **Swagger UI** once the app is running:

```
http://localhost:8080/swagger-ui/index.html
```

The raw OpenAPI spec is available at:
```
http://localhost:8080/v3/api-docs
```

> **Tip:** Use the `/api/auth/login` endpoint in Swagger to get a JWT token, then click the **Authorize** button (top right) and enter `Bearer <your_token>` to authenticate all subsequent requests.

---

## Assumptions & Tradeoffs

- **Roles are fixed enums**: The three roles (`ADMIN`, `ANALYST`, `VIEWER`) are defined as a Java enum. Dynamic permission management is out of scope for this project.
- **Soft deletes only**: Financial records are never hard-deleted. The `is_deleted` flag preserves audit history and data integrity.
- **JWT is stateless**: No token revocation or blacklist mechanism is implemented. In production, a Redis-backed store would be used to support proper logout.
- **Single organisation**: No multi-tenancy. All users and records belong to one shared system.
- **Timestamps in UTC**: All dates and timestamps are stored and returned in UTC.
- **`ddl-auto=update`**: Hibernate manages the schema automatically during development. For a production deployment, Flyway or Liquibase migrations would replace this.
- **Category is a free-form string**: Not normalized into a lookup table, keeping the schema simple. A dedicated `categories` table could be added in a future iteration.
