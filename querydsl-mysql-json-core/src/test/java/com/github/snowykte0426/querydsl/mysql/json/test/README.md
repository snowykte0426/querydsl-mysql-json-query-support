# JSON Function Test Infrastructure

## Overview

This package provides test infrastructure for JSON function tests using Testcontainers and MySQL 8.0.

## Components

### AbstractJsonFunctionTest

Base test class that provides:
- MySQL 8.0.33 container with full JSON support
- Automatic database connection management
- Test schema initialization (users, products, orders tables)
- Data cleanup between tests
- Utility methods for executing queries

**Usage:**
```java
class MyJsonFunctionTest extends AbstractJsonFunctionTest {
    @Test
    void testJsonExtract() throws SQLException {
        // Use getConnection() to access database
        String result = executeScalar("SELECT JSON_EXTRACT(...)");
        assertThat(result).isEqualTo(...);
    }
}
```

### TestDataBuilder

Fluent API for creating test data with JSON columns:

**Users:**
```java
long userId = TestDataBuilder.users(connection)
    .name("John Doe")
    .email("john@example.com")
    .metadata("role", "admin")
    .metadata("department", "IT")
    .settings("theme", "dark")
    .insert();
```

**Products:**
```java
long productId = TestDataBuilder.products(connection)
    .name("Laptop")
    .price(999.99)
    .attribute("brand", "TechCorp")
    .tags("electronics", "computers")
    .insert();
```

**Orders:**
```java
long orderId = TestDataBuilder.orders(connection)
    .userId(userId)
    .orderData("total", 999.99)
    .shippingInfo("address", "123 Main St")
    .insert();
```

## Requirements

- Docker must be running (for Testcontainers)
- At least 512MB RAM available for MySQL container
- Internet connection (first run downloads MySQL image)

## Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests TestInfrastructureTest

# Run with info logging
./gradlew test --info
```

## Database Schema

### users table
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(100)
- `email` VARCHAR(255)
- `metadata` JSON
- `settings` JSON
- `created_at` TIMESTAMP

### products table
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(200)
- `price` DECIMAL(10, 2)
- `attributes` JSON
- `tags` JSON
- `created_at` TIMESTAMP

### orders table
- `id` BIGINT (PK, AUTO_INCREMENT)
- `user_id` BIGINT
- `order_data` JSON
- `shipping_info` JSON
- `created_at` TIMESTAMP

## Notes

- MySQL container is reused between test runs for performance
- Data is automatically cleaned up before each test method
- All JSON functions from MySQL 8.0.17+ are supported
