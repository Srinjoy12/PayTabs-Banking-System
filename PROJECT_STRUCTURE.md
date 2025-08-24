# Project Structure Overview

This document provides an overview of the Banking System POC project structure and organization.

## Directory Structure

```
Paytabs Task/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── paytabs/
│   │   │           └── banking/
│   │   │               ├── BankingSystemApplication.java    # Main Spring Boot application
│   │   │               ├── config/                          # Configuration classes
│   │   │               │   ├── SecurityConfig.java          # Spring Security configuration
│   │   │               │   └── DataInitializer.java         # Sample data initialization
│   │   │               ├── controller/                      # REST API and web controllers
│   │   │               │   ├── TransactionController.java   # System 1 API endpoints
│   │   │               │   ├── System2Controller.java      # System 2 API endpoints
│   │   │               │   ├── AdminController.java         # Super Admin UI controller
│   │   │               │   ├── CustomerController.java      # Customer UI controller
│   │   │               │   └── MainController.java          # Main web interface controller
│   │   │               ├── dto/                             # Data Transfer Objects
│   │   │               │   ├── TransactionRequest.java      # Transaction request DTO
│   │   │               │   ├── TransactionResponse.java     # Transaction response DTO
│   │   │               │   └── ProcessTransactionRequest.java # System 2 request DTO
│   │   │               ├── entity/                          # JPA entities
│   │   │               │   ├── Card.java                    # Card entity with encrypted PIN
│   │   │               │   ├── Transaction.java             # Transaction entity
│   │   │               │   └── User.java                    # User entity for authentication
│   │   │               ├── repository/                      # Data access layer
│   │   │               │   ├── CardRepository.java          # Card data operations
│   │   │               │   ├── TransactionRepository.java   # Transaction data operations
│   │   │               │   └── UserRepository.java          # User data operations
│   │   │               ├── service/                         # Business logic layer
│   │   │               │   ├── TransactionService.java      # System 1 transaction processing
│   │   │               │   ├── System2Service.java          # System 2 transaction processing
│   │   │               │   ├── CardService.java             # Card-related operations
│   │   │               │   └── UserService.java             # User authentication service
│   │   │               └── util/                            # Utility classes
│   │   │                   └── PinHasher.java               # PIN hashing utilities
│   │   └── resources/
│   │       ├── application.properties                       # Application configuration
│   │       └── templates/                                   # Thymeleaf HTML templates
│   │           ├── login.html                               # Login page
│   │           ├── admin/
│   │           │   └── dashboard.html                       # Super Admin dashboard
│   │           └── customer/
│   │               └── dashboard.html                       # Customer dashboard
│   └── test/                                                # Test files (not implemented in POC)
├── pom.xml                                                   # Maven project configuration
├── README.md                                                 # Main project documentation
├── SETUP.md                                                  # Detailed setup instructions
├── PROJECT_STRUCTURE.md                                      # This file
├── test-api.sh                                               # API testing script
├── quick-start.sh                                            # Quick start automation script
└── Banking-System-POC.postman_collection.json               # Postman collection for testing
```

## Architecture Overview

### System 1: Transaction Gateway
- **Controller**: `TransactionController.java`
- **Service**: `TransactionService.java`
- **Responsibility**: 
  - Accepts transaction requests
  - Validates input parameters
  - Routes transactions based on card number range
  - Only supports cards starting with '4' (Visa simulation)

### System 2: Transaction Processor
- **Controller**: `System2Controller.java`
- **Service**: `System2Service.java`
- **Responsibility**:
  - Validates card details
  - Authenticates PIN using SHA-256 hashing
  - Checks and updates card balance
  - Processes withdrawals and top-ups

### Web Interface
- **Main Controller**: `MainController.java` - Handles routing and authentication
- **Admin Controller**: `AdminController.java` - Super Admin dashboard
- **Customer Controller**: `CustomerController.java` - Customer dashboard
- **Templates**: Thymeleaf HTML templates with Bootstrap 5 styling

### Data Layer
- **Entities**: JPA entities for Cards, Transactions, and Users
- **Repositories**: Spring Data JPA repositories for data access
- **Database**: H2 in-memory database for simplicity

### Security
- **Configuration**: `SecurityConfig.java` - Spring Security setup
- **Authentication**: Role-based access control (SUPER_ADMIN, CUSTOMER)
- **PIN Security**: SHA-256 hashing for PIN storage
- **Password Security**: BCrypt hashing for user passwords

## Key Components

### 1. Transaction Processing Flow
```
Client Request → TransactionController → TransactionService → System2Service → Database
```

### 2. Security Flow
```
Login Request → UserService → Spring Security → Role-based Access Control
```

### 3. Data Flow
```
API Request → Validation → Business Logic → Database → Response
```

## Design Patterns

### 1. Layered Architecture
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Data access abstraction
- **Entity Layer**: Data models

### 2. Dependency Injection
- Uses Spring's constructor-based dependency injection
- Services are injected into controllers
- Repositories are injected into services

### 3. Repository Pattern
- Spring Data JPA repositories for data access
- Consistent interface for all data operations
- Automatic query generation from method names

## Configuration

### 1. Application Properties
- **Server**: Port 8080
- **Database**: H2 in-memory with console access
- **JPA**: Hibernate with automatic schema generation
- **Logging**: Debug level for development

### 2. Security Configuration
- **Authentication**: Form-based login
- **Authorization**: Role-based access control
- **CSRF**: Disabled for API endpoints
- **Headers**: Security headers enabled

## Sample Data

### 1. Pre-configured Users
- **Super Admin**: `admin` / `admin123`
- **Customer**: `john` / `john123`, `jane` / `jane123`, `bob` / `bob123`

### 2. Sample Cards
- **John Doe**: `4000000000000001` / PIN: `1234` / Balance: `$1,000.00`
- **Jane Smith**: `4000000000000002` / PIN: `5678` / Balance: `$2,500.00`
- **Bob Johnson**: `4000000000000003` / PIN: `9999` / Balance: `$500.00`
- **Alice Brown**: `5000000000000001` / PIN: `1111` / Balance: `$750.00` (unsupported range)

## Testing

### 1. API Testing
- **Script**: `test-api.sh` - Automated API testing
- **Postman**: Collection file for manual testing
- **Coverage**: All major transaction scenarios

### 2. Test Scenarios
- Successful withdrawals and top-ups
- Invalid card numbers and PINs
- Unsupported card ranges
- Insufficient balance scenarios
- Missing required fields

## Deployment

### 1. Development
- Run with `mvn spring-boot:run`
- Access at `http://localhost:8080`
- H2 console at `http://localhost:8080/h2-console`

### 2. Production Considerations
- Replace H2 with PostgreSQL/MySQL
- Configure proper logging
- Set up monitoring and health checks
- Implement proper error handling
- Add API rate limiting

## Future Enhancements

### 1. Technical Improvements
- Add comprehensive unit tests
- Implement API documentation (Swagger)
- Add transaction logging and audit trails
- Implement real-time notifications

### 2. Feature Additions
- Transaction limits and restrictions
- Multi-currency support
- Card management features
- Customer support integration

### 3. Infrastructure
- Docker containerization
- Kubernetes deployment
- CI/CD pipeline setup
- Monitoring and alerting

## Best Practices Implemented

### 1. Security
- Never store plain-text PINs
- Use strong password hashing
- Implement role-based access control
- Validate all input parameters

### 2. Code Quality
- Consistent naming conventions
- Proper separation of concerns
- Comprehensive error handling
- Detailed logging

### 3. Performance
- Efficient database queries
- Proper indexing considerations
- Connection pooling
- Caching strategies

This structure provides a solid foundation for a banking system POC while maintaining clean architecture and following Spring Boot best practices. 