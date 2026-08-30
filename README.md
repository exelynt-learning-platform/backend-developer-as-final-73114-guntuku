<<<<<<< HEAD
# backend-developer-as-final-73114-guntuku
Final Project Assignment - This repository contains the complete final project code and documentation.
=======
# RESTful Resource Booking System API

A production-grade RESTful Resource Booking System built with **Spring Boot 3**, **Java 17+**, **Spring Security 6**, **JWT Authentication**, and **Spring Data JPA**.

The application allows users to view available resources and manage their own reservations, while administrators have full access to manage resources, view all reservations across users, and manage booking statuses.

---

## Key Features

- **JWT Authentication**: Secure login via `POST /auth/login` returning stateless Bearer JWT tokens.
- **Role-Based Access Control (RBAC)**:
  - **ADMIN**: Full CRUD permissions on resources and all user reservations.
  - **USER**: Read-only access to resources; create/read/update/delete access restricted strictly to their own reservations.
- **Strict User Identity Enforcement**: Reservation ownership is resolved directly from the authenticated JWT principal (`SecurityContextHolder`), preventing identity forgery.
- **Reservation Lifecycle & Decimal Pricing**:
  - Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`.
  - Pricing stored as `BigDecimal` decimal values and computed automatically based on duration and resource hourly rate.
- **Advanced Filtering, Pagination & Sorting**:
  - Filter reservations by `status`, `minPrice`, and `maxPrice`.
  - Full pagination support (`page`, `size`).
  - Customizable sorting (`sort=createdAt,desc`).
- **Database Versatility**: Runs with **H2 In-Memory DB** by default for instant setup, with ready-to-use profiles for **MySQL** and **PostgreSQL**.
- **Interactive Documentation**: Integrated **Swagger / OpenAPI UI** and pre-configured **Postman Collection**.

---

## Seed Test Accounts

On application startup, `DataInitializer` automatically seeds default user accounts, resources, and reservations if the database is empty.

| Role | Username | Email | Password |
| :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin@example.com` | `admin123` |
| **USER** | `user` | `user@example.com` | `user123` |
| **USER** | `user2` | `user2@example.com` | `user123` |

---

## Prerequisites

- **Java JDK 17+** (JDK 17 or JDK 21 supported)
- **Maven 3.8+** (or using the included Maven Wrapper `./mvnw`)

---

## Getting Started & Running locally

### 1. Run with Default H2 Database (Zero Setup)
No database installation required. Simply execute:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`.
H2 Console is accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bookingdb`, Username: `sa`, Password: empty).

### 2. Run with MySQL Profile
Set the active profile to `mysql` and configure environment variables if needed:

```bash
# Environment variables (Optional, defaults provided)
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=booking_db
export DB_USER=root
export DB_PASSWORD=root

./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 3. Run with PostgreSQL Profile
Set the active profile to `postgres`:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=booking_db
export DB_USER=postgres
export DB_PASSWORD=postgres

./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## API Documentation & Testing Tools

### Swagger / OpenAPI UI
Access interactive API documentation and test endpoints directly in your browser:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

To authorize in Swagger UI:
1. Call `POST /auth/login` with admin or user credentials.
2. Copy the `accessToken` string.
3. Click the **Authorize** button in Swagger UI and paste the token.

### Postman Collection
An importable Postman collection is included in the project root:
- [`postman_collection.json`](file:///e:/Personal%20Project/backend-developer-as-final-73114-guntuku-main/backend-developer-as-final-73114-guntuku-main/postman_collection.json)

---

## REST API Summary

### 1. Authentication
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Authenticate credentials & receive JWT token | Public |
| `POST` | `/auth/register` | Register new user account | Public |

### 2. Resources
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/resources` | Get all resources (Optional `?available=true`) | USER, ADMIN |
| `GET` | `/resources/{id}` | Get resource details by ID | USER, ADMIN |
| `POST` | `/resources` | Create a new resource | ADMIN |
| `PUT` | `/resources/{id}` | Update resource details | ADMIN |
| `DELETE` | `/resources/{id}` | Delete a resource | ADMIN |

### 3. Reservations
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/reservations` | Create reservation (user identity from JWT) | USER, ADMIN |
| `GET` | `/reservations` | List reservations with filtering & pagination | USER (own), ADMIN (all) |
| `GET` | `/reservations/{id}` | Get reservation details by ID | USER (owner), ADMIN |
| `PUT` | `/reservations/{id}` | Update reservation timing or resource | USER (owner), ADMIN |
| `PATCH` | `/reservations/{id}/status` | Update reservation status (`PENDING`, `CONFIRMED`, `CANCELLED`) | USER (owner), ADMIN |
| `DELETE` | `/reservations/{id}` | Delete / cancel reservation | USER (owner), ADMIN |

#### Reservation Query Parameters
- `status`: `PENDING`, `CONFIRMED`, `CANCELLED`
- `minPrice`: Decimal value (e.g. `50.00`)
- `maxPrice`: Decimal value (e.g. `300.00`)
- `page`: Page index (default `0`)
- `size`: Items per page (default `10`)
- `sort`: Field and direction (e.g. `createdAt,desc` or `price,asc`)

---

## Running Unit and Integration Tests

Run the complete automated test suite covering authentication, RBAC authorization, resource CRUD, reservation filtering, pagination, and token validation:

```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/booking/resourcebooking/
│   │   ├── config/              # DataInitializer & OpenApiConfig
│   │   ├── controller/          # REST Controllers
│   │   ├── dto/                 # Request & Response DTOs
│   │   ├── exception/           # Custom exceptions & GlobalExceptionHandler
│   │   ├── model/               # JPA Entities & Enums
│   │   ├── repository/          # Spring Data JPA Repositories & Specifications
│   │   ├── security/            # SecurityConfig & JWT Filter/Provider
│   │   ├── service/             # Business Logic & Implementations
│   │   └── ResourceBookingSystemApplication.java
│   └── resources/
│       ├── application.properties          # Default (H2 In-Memory)
│       ├── application-mysql.properties    # MySQL Profile
│       └── application-postgres.properties # PostgreSQL Profile
└── test/
    └── java/com/booking/resourcebooking/   # Unit & Integration Tests
```
>>>>>>> 585468d (Complete RESTful Resource Booking System implementation)
