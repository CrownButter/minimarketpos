# Minimarket POS System - Spring Boot

## Prerequisites
- Java 25
- Maven 3.9+
- MySQL 8.0+
- IDE (IntelliJ IDEA or Eclipse)

## Quick Start

### 1. Clone/Download Project
```bash
git init minimarket-pos
cd minimarket-pos
```

### 2. Database Setup
```sql
CREATE DATABASE kop_pol;
```

### 3. Configure Application
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
jwt.secret=YOUR_SECRET_KEY_AT_LEAST_256_BITS
stripe.api.key=YOUR_STRIPE_KEY
```

### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Access Application
- API: http://localhost:8080
- Login endpoint: POST /api/auth/login

## API Endpoints

### Authentication
- POST /api/auth/login
- POST /api/auth/register
- POST /api/auth/logout

### Products
- GET /api/products
- GET /api/products/{id}
- POST /api/products
- PUT /api/products/{id}
- DELETE /api/products/{id}

### POS
- POST /api/pos/add-product
- GET /api/pos/load-cart
- POST /api/pos/complete-sale

### Sales
- GET /api/sales
- GET /api/sales/{id}
- PUT /api/sales/{id}
- DELETE /api/sales/{id}

## Project Structure
See PROJECT_STRUCTURE.md for complete structure.

## License
MIT License