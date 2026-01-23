# QueryDSL MySQL JSON - JPA Module

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.2-red.svg)]()
[![JitPack](https://jitpack.io/v/snowykte0426/querydsl-mysql-json-query-support.svg)](https://jitpack.io/#snowykte0426/querydsl-mysql-json-query-support)
[![Java: 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![QueryDSL: 7.1](https://img.shields.io/badge/QueryDSL-7.1-blue.svg)](https://github.com/OpenFeign/querydsl)

Seamlessly integrate MySQL JSON functions with QueryDSL JPA for type-safe JSON operations in your JPA entities.

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Configuration](#configuration)
- [Quick Start](#quick-start)
- [Entity Setup](#entity-setup)
- [Function Categories](#function-categories)
- [Advanced Usage](#advanced-usage)
- [Spring Data JPA Integration](#spring-data-jpa-integration)
- [API Reference](#api-reference)
- [FAQ](#faq)
- [Related Documentation](#related-documentation)

## Overview

The JPA module provides seamless integration of MySQL JSON functions with QueryDSL JPA. It allows you to work with JSON columns in your JPA entities using type-safe, fluent API.

**Key Features:**
- All 35 MySQL JSON functions available for JPA queries
- Two API styles: Static functions and Fluent API
- 100% delegation to core module (zero business logic duplication)
- Full Spring Data JPA integration via `JsonFunctionRepositorySupport`
- Works with Hibernate, EclipseLink, and other JPA providers
- Type-safe query building with compile-time checking

## Installation

### Gradle

```kotlin
dependencies {
    implementation("io.github.snowykte0426:querydsl-mysql-json-jpa:0.2.2")

    // Required dependencies (if not already included)
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Annotation processor for Q-classes
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
}
```

### Maven

```xml
<dependencies>
    <dependency>
        <groupId>io.github.snowykte0426</groupId>
        <artifactId>querydsl-mysql-json-jpa</artifactId>
        <version>0.2.2</version>
    </dependency>

    <!-- Required dependencies -->
    <dependency>
        <groupId>io.github.openfeign.querydsl</groupId>
        <artifactId>querydsl-jpa</artifactId>
        <version>7.1</version>
    </dependency>
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Annotation processor for Q-classes -->
        <plugin>
            <groupId>com.mysema.maven</groupId>
            <artifactId>apt-maven-plugin</artifactId>
            <version>1.1.3</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process</goal>
                    </goals>
                    <configuration>
                        <outputDirectory>target/generated-sources/java</outputDirectory>
                        <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration

### EntityManager Setup

Configure your `persistence.xml` with MySQL dialect:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="3.0">
    <persistence-unit name="myapp-pu" transaction-type="RESOURCE_LOCAL">
        <properties>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/mydb"/>
            <property name="jakarta.persistence.jdbc.user" value="root"/>
            <property name="jakarta.persistence.jdbc.password" value="password"/>
            <property name="jakarta.persistence.jdbc.driver"
                      value="com.mysql.cj.jdbc.Driver"/>

            <!-- Hibernate Configuration -->
            <property name="hibernate.dialect"
                      value="org.hibernate.dialect.MySQLDialect"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

### JPAQueryFactory Setup

```java
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class DatabaseConfig {
    private static EntityManagerFactory emf;
    private static ThreadLocal<EntityManager> emThread = new ThreadLocal<>();

    public static void initialize() {
        emf = Persistence.createEntityManagerFactory("myapp-pu");
    }

    public static EntityManager getEntityManager() {
        EntityManager em = emThread.get();
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
            emThread.set(em);
        }
        return em;
    }

    public static JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(getEntityManager());
    }

    public static void close() {
        EntityManager em = emThread.get();
        if (em != null && em.isOpen()) {
            em.close();
        }
        emThread.remove();
    }
}
```

## Quick Start

### 1. Define Your Entity

```java
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @Column(columnDefinition = "JSON")
    private String metadata;  // Store as String, query as JSON

    @Column(columnDefinition = "JSON")
    private String settings;

    @Column(columnDefinition = "JSON")
    private String roles;

    // Constructors, getters, setters...
}
```

### 2. Generate Q-Classes

Run your build to generate QueryDSL Q-classes:

```bash
./gradlew compileJava  # Gradle
mvn compile            # Maven
```

This generates `QUser` with type-safe paths for your entity.

### 3. Query with JSON Functions

```java
import jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonFunctions;
import expressions.jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonExpression;
import static entity.jpa.io.github.snowykte0426.querydsl.mysql.json.QUser.user;

// Static function style
JPAQueryFactory queryFactory = DatabaseConfig.getQueryFactory();

List<User> admins = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
        .eq("\"admin\""))
    .fetch();

// Fluent API style
JPAJsonExpression metadata = JPAJsonExpression.of(user.metadata);

List<User> premiumUsers = queryFactory
    .selectFrom(user)
    .where(metadata.extract("$.plan").eq("\"premium\""))
    .fetch();

// Complex queries
List<User> usersWithDarkMode = queryFactory
    .selectFrom(user)
    .where(JPAJsonExpression.of(user.settings)
        .contains("\"dark\"", "$.theme"))
    .fetch();
```

## Entity Setup

### Recommended Column Definition

Always use `columnDefinition = "JSON"` for JSON columns:

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;

    // Correct: Explicitly specify JSON type
    @Column(columnDefinition = "JSON")
    private String attributes;

    @Column(columnDefinition = "JSON")
    private String tags;

    @Column(name = "config", columnDefinition = "JSON")
    private String configuration;
}
```

### Entity Best Practices

1. **Use String for JSON columns**: Store JSON as `String` type in Java
2. **Explicit column definition**: Always specify `columnDefinition = "JSON"`
3. **Nullable handling**: Use `@Column(nullable = false)` if JSON is required
4. **Naming conventions**: Use clear names like `metadata`, `settings`, `config`

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "JSON", nullable = false)
    private String items;  // Array of order items

    @Column(columnDefinition = "JSON")
    private String shippingAddress;  // Nested object

    @Column(columnDefinition = "JSON")
    private String metadata;  // Flexible data
}
```

## Function Categories

All 35 MySQL JSON functions are available in the JPA module:

| Category | Functions | Count |
|----------|-----------|-------|
| **Creation** | JSON_ARRAY, JSON_OBJECT, JSON_QUOTE | 3 |
| **Search** | JSON_EXTRACT, JSON_CONTAINS, JSON_SEARCH, JSON_KEYS, JSON_OVERLAPS, MEMBER OF, JSON_CONTAINS_PATH, JSON_VALUE | 10 |
| **Modification** | JSON_SET, JSON_INSERT, JSON_REPLACE, JSON_REMOVE, JSON_ARRAY_APPEND, JSON_ARRAY_INSERT, JSON_MERGE_PATCH, JSON_MERGE_PRESERVE, JSON_UNQUOTE | 9 |
| **Attribute** | JSON_DEPTH, JSON_LENGTH, JSON_TYPE, JSON_VALID | 4 |
| **Utility** | JSON_PRETTY, JSON_STORAGE_SIZE, JSON_STORAGE_FREE | 3 |
| **Schema** | JSON_SCHEMA_VALID, JSON_SCHEMA_VALIDATION_REPORT | 2 |
| **Aggregate** | JSON_ARRAYAGG, JSON_OBJECTAGG | 2 |
| **Table** | JSON_TABLE | 1 |

## Advanced Usage

### 1. Complex Queries with Joins

```java
import static entity.jpa.io.github.snowykte0426.querydsl.mysql.json.QUser.user;
import static entity.jpa.io.github.snowykte0426.querydsl.mysql.json.QProduct.product;

// Join with JSON condition
List<Tuple> results = queryFactory
    .select(user, product)
    .from(user)
    .join(product)
    .on(JPAJsonFunctions.memberOf(
        product.category,
        user.metadata  // $.interests array
    ))
    .fetch();

// Nested JSON extraction in join
List<User> users = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonExtract(user.settings, "$.region")
        .eq(JPAJsonFunctions.jsonExtract(product.attributes, "$.availability")))
    .fetch();
```

### 2. JSON Modification in Updates

```java
EntityManager em = DatabaseConfig.getEntityManager();
em.getTransaction().begin();

// Update JSON field with new value
User user = em.find(User.class, 1L);
String updatedMetadata = JPAJsonFunctions
    .jsonSet(user.metadata, "$.lastLogin", "2024-01-17")
    .toString();

user.setMetadata(updatedMetadata);
em.merge(user);
em.getTransaction().commit();

// Bulk update with native query
em.createNativeQuery(
    "UPDATE users SET metadata = JSON_SET(metadata, '$.status', :status) " +
    "WHERE JSON_EXTRACT(metadata, '$.plan') = :plan"
)
.setParameter("status", "active")
.setParameter("plan", "\"premium\"")
.executeUpdate();
```

### 3. Aggregate Functions

```java
import static entity.jpa.io.github.snowykte0426.querydsl.mysql.json.QUser.user;

// Group users by department, aggregate names as JSON array
List<Tuple> departmentUsers = queryFactory
    .select(
        JPAJsonFunctions.jsonExtract(user.metadata, "$.department"),
        JPAJsonFunctions.jsonArrayAgg(user.name)
    )
    .from(user)
    .groupBy(JPAJsonFunctions.jsonExtract(user.metadata, "$.department"))
    .fetch();

// Aggregate key-value pairs into JSON object
List<Tuple> userRoles = queryFactory
    .select(
        user.id,
        JPAJsonFunctions.jsonObjectAgg(
            user.name,
            JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
        )
    )
    .from(user)
    .groupBy(user.id)
    .fetch();
```

### 4. Subqueries with JSON

```java
// Find users whose role exists in another table
QRole role = QRole.role;

List<User> users = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
        .in(JPAQuery.select(role.name)
            .from(role)
            .where(role.active.isTrue())
        )
    )
    .fetch();
```

### 5. Array Operations

```java
// Check if user has specific permission in array
List<User> admins = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.memberOf(
        "admin",
        user.roles  // JSON array of roles
    ))
    .fetch();

// Append to array
String updatedRoles = JPAJsonFunctions
    .jsonArrayAppend(user.roles, "$", "new_role")
    .toString();

// Remove from array by index
String trimmedRoles = JPAJsonFunctions
    .jsonRemove(user.roles, "$[2]")
    .toString();
```

### 6. Working with JSON_CONTAINS

The `jsonContains` function tests whether a JSON document contains a specific value. MySQL's `JSON_CONTAINS()` requires values to be valid JSON, which can be inconvenient for simple string or numeric searches.

#### Option 1: Manual JSON Escaping (Traditional)

```java
// For string values, you need to escape manually
BooleanExpression hasAdmin = JPAJsonFunctions.jsonContains(
    user.roles,
    "\"admin\""  // Note the escaped quotes
);

// For numeric values
BooleanExpression hasId = JPAJsonFunctions.jsonContains(
    product.features,
    "42"  // Numbers don't need quotes
);
```

#### Option 2: Auto-Escaping (Recommended, 0.1.0-Beta.4+)

Use convenience methods for automatic escaping:

```java
// String values - no manual escaping needed!
BooleanExpression hasAdmin = JPAJsonFunctions.jsonContainsString(
    user.roles,
    "admin"  // Automatically escaped to "\"admin\""
);

// With path parameter
BooleanExpression hasScope = JPAJsonFunctions.jsonContainsString(
    apiKey.metadata,
    "student:read",
    "$.scope"
);

// Numeric values
BooleanExpression hasId = JPAJsonFunctions.jsonContainsNumber(
    product.features,
    42
);

BooleanExpression hasPrice = JPAJsonFunctions.jsonContainsNumber(
    product.metadata,
    99.99,
    "$.price"
);

// Boolean values
BooleanExpression isActive = JPAJsonFunctions.jsonContainsBoolean(
    user.settings,
    true,
    "$.active"
);
```

#### Real-World Example: API Scopes

```java
// Before (manual escaping, error-prone):
List<ApiKey> keys = queryFactory
    .selectFrom(apiKey)
    .where(JPAJsonFunctions.jsonContains(
        apiKey.scopes,
        "\"student:read\""  // Easy to forget quotes!
    ))
    .fetch();

// After (automatic escaping, cleaner):
List<ApiKey> keys = queryFactory
    .selectFrom(apiKey)
    .where(JPAJsonFunctions.jsonContainsString(
        apiKey.scopes,
        "student:read"  // No manual escaping needed
    ))
    .fetch();
```

### 7. Schema Validation

```java
String userSchema = """
{
  "type": "object",
  "required": ["name", "email", "role"],
  "properties": {
    "name": {"type": "string"},
    "email": {"type": "string", "format": "email"},
    "role": {"type": "string", "enum": ["admin", "user", "guest"]}
  }
}
""";

// Find users with valid metadata
List<User> validUsers = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonSchemaValid(userSchema, user.metadata))
    .fetch();

// Get validation report
String report = JPAJsonFunctions
    .jsonSchemaValidationReport(userSchema, user.metadata)
    .toString();
```

## Spring Data JPA Integration

### 1. Extend JsonFunctionRepositorySupport

```java
import spring.jpa.io.github.snowykte0426.querydsl.mysql.json.JsonFunctionRepositorySupport;
import expressions.jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonExpression;
import org.springframework.stereotype.Repository;
import static com.example.entity.QUser.user;

@Repository
public class UserRepositoryImpl extends JsonFunctionRepositorySupport
        implements UserRepositoryCustom {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public List<User> findByRole(String role) {
        JPAJsonExpression metadata = jsonExpression(user.metadata);

        return from(user)
            .where(metadata.extract("$.role").eq("\"" + role + "\""))
            .fetch();
    }

    @Override
    public List<User> findByPlan(String plan) {
        return from(user)
            .where(jsonExtract(user.metadata, "$.plan")
                .eq("\"" + plan + "\""))
            .fetch();
    }

    @Override
    public List<User> findUsersWithPermission(String permission) {
        return from(user)
            .where(memberOf(permission, user.roles))
            .fetch();
    }
}
```

### 2. Define Custom Repository Interface

```java
public interface UserRepositoryCustom {
    List<User> findByRole(String role);
    List<User> findByPlan(String plan);
    List<User> findUsersWithPermission(String permission);
}

public interface UserRepository extends JpaRepository<User, Long>,
                                        UserRepositoryCustom {
    // Standard Spring Data methods...
}
```

### 3. Configuration

```java
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDSLConfig {

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }
}
```

### 4. Service Layer Usage

```java
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<User> getAdminUsers() {
        return userRepository.findByRole("admin");
    }

    @Transactional(readOnly = true)
    public List<User> getPremiumUsers() {
        return userRepository.findByPlan("premium");
    }

    @Transactional(readOnly = true)
    public List<User> getUsersWithPermission(String permission) {
        return userRepository.findUsersWithPermission(permission);
    }
}
```

### 5. Advanced Repository Pattern

```java
@Repository
public class ProductRepositoryImpl extends JsonFunctionRepositorySupport
        implements ProductRepositoryCustom {

    public ProductRepositoryImpl() {
        super(Product.class);
    }

    @Override
    public List<Product> findByTag(String tag) {
        JPAJsonExpression tags = jsonExpression(product.tags);

        return getQueryFactory()
            .selectFrom(product)
            .where(tags.contains("\"" + tag + "\""))
            .fetch();
    }

    @Override
    public Map<String, Long> countByCategory() {
        List<Tuple> results = getQueryFactory()
            .select(
                product.category,
                product.count()
            )
            .from(product)
            .groupBy(product.category)
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                t -> t.get(product.category),
                t -> t.get(product.count())
            ));
    }

    @Override
    public List<Product> searchByAttributes(Map<String, String> filters) {
        JPAJsonExpression attrs = jsonExpression(product.attributes);
        BooleanExpression condition = null;

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            BooleanExpression expr = attrs
                .extract("$." + entry.getKey())
                .eq("\"" + entry.getValue() + "\"");

            condition = (condition == null) ? expr : condition.and(expr);
        }

        return getQueryFactory()
            .selectFrom(product)
            .where(condition)
            .fetch();
    }
}
```

## API Reference

### Core Classes

- **`JPAJsonFunctions`** - Static entry point for all JSON functions
  - Location: `io.github.snowykte0426.querydsl.mysql.json.jpa`
  - Usage: `JPAJsonFunctions.jsonExtract(...)`

- **`JPAJsonExpression`** - Fluent API wrapper for method chaining
  - Location: `io.github.snowykte0426.querydsl.mysql.json.jpa.expressions`
  - Usage: `JPAJsonExpression.of(user.metadata).extract("$.key")`

- **`JsonFunctionRepositorySupport`** - Spring Data JPA base class
  - Location: `io.github.snowykte0426.querydsl.mysql.json.jpa.spring`
  - Usage: Extend in custom repository implementations

### Expression Types

- **`JsonArrayExpression`** - Represents JSON arrays
- **`JsonObjectExpression`** - Represents JSON objects
- **`JsonValueExpression`** - Represents JSON scalar values
- **`JsonExpression<T>`** - Generic JSON expression

## FAQ

### Q: Do I need to store JSON as String in my entity?

**A:** Yes. While MySQL has a native JSON type, JPA entities should use `String` with `@Column(columnDefinition = "JSON")`. This ensures proper type mapping and compatibility across different JPA providers.

```java
// Correct
@Column(columnDefinition = "JSON")
private String metadata;

// Incorrect - may not work with all JPA providers
@Column(columnDefinition = "JSON")
private JsonNode metadata;
```

### Q: How do I handle null values in JSON?

**A:** Use `JSON_NULL()` for JSON null, not Java null:

```java
// For JSON null
String jsonWithNull = JPAJsonFunctions.jsonObject(
    "key", JPAJsonFunctions.jsonNull()
).toString();

// For missing/null column
user.setMetadata(null);  // Database NULL, not JSON null
```

### Q: Can I use this with native queries?

**A:** Yes, but you lose type safety:

```java
List<User> users = entityManager
    .createNativeQuery(
        "SELECT * FROM users WHERE JSON_EXTRACT(metadata, '$.role') = :role",
        User.class
    )
    .setParameter("role", "admin")
    .getResultList();
```

For type safety, prefer QueryDSL queries.

### Q: How do I index JSON columns?

**A:** Create functional indexes on JSON paths:

```sql
CREATE INDEX idx_user_role
ON users((CAST(JSON_EXTRACT(metadata, '$.role') AS CHAR(50))));
```

Or use generated columns:

```java
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_role", columnList = "role_generated")
})
public class User {
    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "role_generated",
            columnDefinition = "VARCHAR(50) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')))")
    private String roleGenerated;
}
```

### Q: Does this work with EclipseLink or other JPA providers?

**A:** Yes! The module works with any JPA 3.1+ provider. Just ensure your provider supports MySQL's JSON functions (most do).

### Q: How do I test queries with JSON functions?

**A:** Use Testcontainers for integration tests:

```java
@Testcontainers
class UserRepositoryTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33");

    @Test
    void testJsonQuery() {
        // Your tests here
    }
}
```

### Q: Can I use this with pagination?

**A:** Yes, QueryDSL pagination works normally:

```java
Pageable pageable = PageRequest.of(0, 10);

List<User> users = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.active")
        .eq("true"))
    .offset(pageable.getOffset())
    .limit(pageable.getPageSize())
    .fetch();
```

### Q: What about transaction management?

**A:** Use standard JPA transactions or Spring's `@Transactional`:

```java
@Transactional
public void updateUserRole(Long userId, String newRole) {
    User user = em.find(User.class, userId);
    String updated = JPAJsonFunctions
        .jsonSet(user.getMetadata(), "$.role", newRole)
        .toString();
    user.setMetadata(updated);
}
```

## Related Documentation

- [Main Project README](../README.md) - Project overview and all modules
- [Core Module](../querydsl-mysql-json-core/README.md) - Core JSON function implementations
- [SQL Module](../querydsl-mysql-json-sql/README.md) - SQL module for non-JPA usage
- [IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) - Complete function reference
- [PROGRESS.md](../PROGRESS.md) - Development progress and testing status

---

**Need Help?** Check the [main README](../README.md) or open an issue on GitHub.
