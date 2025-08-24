# PayTabs Bank - Simplified Banking System POC

This project is a Proof of Concept (POC) for a simplified banking system, developed as a task for PayTabs. It simulates core banking functionalities including transaction processing (withdrawals and top-ups), secure card validation, and role-based transaction monitoring.

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [Accessing the UI](#accessing-the-ui)
- [Demo Credentials](#demo-credentials)
- [API Endpoints](#api-endpoints)
  - [System 1: Transaction Gateway](#system-1-transaction-gateway)
  - [System 2: Transaction Processor](#system-2-transaction-processor)
- [Security Features](#security-features)

## Project Overview

The system is composed of two main components:

1.  **System 1 (Transaction Gateway):** An API gateway that accepts transaction requests, performs initial validation, and routes them based on card type.
2.  **System 2 (Transaction Processor):** A core processing system that handles card authentication, balance management, and transaction logging.

The application also features a web-based UI with role-based access for a **Super Admin** (to monitor all transactions) and **Customers** (to view their own balance and transaction history).

## Features

- **Dual-System Architecture:** Simulates a real-world scenario with a gateway and a processor.
- **RESTful APIs:** For transaction processing and data retrieval.
- **Role-Based Access Control:** Separate UIs and permissions for Admins and Customers.
- **Secure PIN Handling:** PINs are hashed using SHA-256 and never stored in plain text.
- **Card Number Encryption:** Card numbers are encrypted at rest in the database using AES.
- **In-Memory Database:** Utilizes H2 for simplicity and ease of setup.
- **Dynamic UI:** Modern, responsive frontend built with Thymeleaf and vanilla JavaScript.

## Technology Stack

- **Backend:** Java 17, Spring Boot 3.2.0, Spring Security, Spring Data JPA
- **Frontend:** Thymeleaf, HTML5, CSS3, JavaScript
- **Database:** H2 In-Memory Database
- **Build Tool:** Apache Maven
- **Libraries:** Lombok

## Setup and Installation

### Prerequisites

- Java Development Kit (JDK) 17 or later
- Apache Maven 3.6 or later
- An IDE such as IntelliJ IDEA or VS Code (optional)

### Steps

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd Paytabs-Task
    ```

2.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```
    This will compile the source code and download all the required dependencies.

## Running the Application

You can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

## Accessing the UI

- **Login Page:** Navigate to `http://localhost:8080/login`
- **Super Admin Dashboard:** Log in with admin credentials to be redirected to `/admin/dashboard`.
- **Customer Dashboard:** Log in with customer credentials (username/password or card/PIN) to be redirected to `/customer/dashboard`.

## Demo Credentials

The application is pre-loaded with the following demo accounts:

| Role         | Username     | Password      | Card Number        | PIN  |
|--------------|--------------|---------------|--------------------|------|
| Super Admin  | `admin`      | `admin123`    | N/A                | N/A  |
| Customer 1   | `john_doe`   | `password123` | `4111111111111111` | `1234` |
| Customer 2   | `jane_smith` | `password456` | `4222222222222222` | `5678` |

## API Endpoints

You can use tools like `curl` or Postman to interact with the APIs.

### System 1: Transaction Gateway

This is the main entry point for all transactions. It performs initial validation and routing.

- **URL:** `POST /api/transaction`
- **Description:** Submits a transaction for processing. The gateway will only route cards starting with '4' (Visa simulation) to System 2.
- **Content-Type:** `application/json`

**Request Body:**

```json
{
  "cardNumber": "4111111111111111",
  "pin": "1234",
  "amount": 100.50,
  "type": "withdraw"
}
```
- `type` can be `withdraw` or `topup`.

**Success Response (200 OK):**

```json
{
    "success": true,
    "message": "Withdrawal successful",
    "status": "success",
    "amount": 100.50,
    "balanceAfter": 1399.50,
    "timestamp": "2023-10-27T10:00:00.123456",
    "transactionId": "1"
}
```

**Failure Response (Card Range Not Supported):**

```json
{
    "success": false,
    "message": "Card range not supported",
    "status": "declined",
    ...
}
```

### System 2: Transaction Processor

This endpoint is intended for internal or trusted systems that might bypass the gateway. *Note: For this endpoint, the PIN is expected to be pre-hashed.* This is a simulation and not a recommended real-world practice.

- **URL:** `POST /api/process`
- **Description:** Directly processes a transaction.
- **Content-Type:** `application/json`

**Request Body:**

```json
{
  "cardNumber": "4111111111111111",
  "pinHash": "81dc9bdb52d04dc20036dbd8313ed055",
  "amount": 50.00,
  "type": "topup"
}
```

## Security Features

- **Authentication:** Managed by Spring Security, supporting both username/password and card/PIN login methods.
- **PIN Hashing:** The `pin` field is never stored. It is hashed using SHA-256 on arrival and compared against the stored `pinHash`.
- **Card Number Encryption:** The `cardNumber` field is automatically encrypted before being saved to the database using AES encryption. It is decrypted upon retrieval, remaining secure at rest.
