# Integration Guide

This guide covers integrating QueryDSL MySQL JSON Query Support with popular Java frameworks and libraries.

## Table of Contents

- [Spring Boot Integration](#spring-boot-integration)
- [Spring Data JPA Integration](#spring-data-jpa-integration)
- [Standalone JPA Integration](#standalone-jpa-integration)
- [Spring JDBC Integration](#spring-jdbc-integration)
- [Testing Integration](#testing-integration)
- [Connection Pooling](#connection-pooling)
- [Transaction Management](#transaction-management)
- [Common Patterns](#common-patterns)
- [Troubleshooting](#troubleshooting)

---

## Spring Boot Integration

### JPA Module with Spring Boot

#### 1. Dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL MySQL JSON
    implementation("io.github.snowykte0426:querydsl-mysql-json-jpa:0.2.3")

    // QueryDSL JPA
    implementation("io.github.openfeign.querydsl:querydsl-jpa:7.1")

    // MySQL Driver
    runtimeOnly("com.mysql:mysql-connector-j")

    // Annotation Processor for Q-classes
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
}
```

#### 2. Application Configuration

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true
```

#### 3. QueryDSL Configuration

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

#### 4. Entity Definition

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
    private String metadata;

    @Column(columnDefinition = "JSON")
    private String settings;

    // Constructors, getters, setters...

    public User() {}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and setters...
}
```

#### 5. Service Layer

```java
import jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonFunctions;
import expressions.jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.entity.QUser.user;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final JPAQueryFactory queryFactory;

    public UserService(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<User> findUsersByRole(String role) {
        return queryFactory
            .selectFrom(user)
            .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
                .eq("\"" + role + "\""))
            .fetch();
    }

    public List<User> findActiveUsers() {
        JPAJsonExpression metadata = JPAJsonExpression.of(user.metadata);

        return queryFactory
            .selectFrom(user)
            .where(metadata.extract("$.status").eq("\"active\""))
            .fetch();
    }

    @Transactional
    public void updateUserPlan(Long userId, String newPlan) {
        User foundUser = queryFactory
            .selectFrom(user)
            .where(user.id.eq(userId))
            .fetchOne();

        if (foundUser != null) {
            String updatedMetadata = JPAJsonFunctions
                .jsonSet(foundUser.getMetadata(), "$.plan", newPlan)
                .toString();
            foundUser.setMetadata(updatedMetadata);
        }
    }
}
```

### SQL Module with Spring Boot

#### 1. Dependencies

```kotlin
// build.gradle.kts
dependencies {
    // Spring Boot JDBC
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // QueryDSL MySQL JSON
    implementation("io.github.snowykte0426:querydsl-mysql-json-sql:0.2.3")

    // QueryDSL SQL
    implementation("io.github.openfeign.querydsl:querydsl-sql:7.1")

    // MySQL Driver
    runtimeOnly("com.mysql:mysql-connector-j")

    // Connection Pool (HikariCP)
    implementation("com.zaxxer:HikariCP:5.1.0")
}
```

#### 2. Configuration

```java
import sql.io.github.snowykte0426.querydsl.mysql.json.MySQLJsonTemplates;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;

@org.springframework.context.annotation.Configuration
public class SqlQueryDSLConfig {

    @Bean
    public Configuration querydslConfiguration() {
        return new Configuration(MySQLJsonTemplates.DEFAULT);
    }

    @Bean
    public SQLQueryFactory sqlQueryFactory(
            Configuration querydslConfiguration,
            DataSource dataSource) {
        return new SQLQueryFactory(querydslConfiguration, dataSource);
    }
}
```

#### 3. Repository Pattern

```java
import sql.io.github.snowykte0426.querydsl.mysql.json.SqlJsonFunctions;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;
import static com.example.QUsers.users;

@Repository
public class UserSqlRepository {

    private final SQLQueryFactory queryFactory;

    public UserSqlRepository(SQLQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Map<String, Object>> findUsersByRole(String role) {
        return queryFactory
            .select(users.id, users.name, users.email)
            .from(users)
            .where(SqlJsonFunctions.jsonExtract(users.metadata, "$.role")
                .eq("\"" + role + "\""))
            .fetch()
            .stream()
            .map(tuple -> Map.of(
                "id", tuple.get(users.id),
                "name", tuple.get(users.name),
                "email", tuple.get(users.email)
            ))
            .collect(Collectors.toList());
    }

    public long updateUserPlan(Long userId, String newPlan) {
        return queryFactory
            .update(users)
            .set(users.metadata,
                SqlJsonFunctions.jsonSet(users.metadata, "$.plan", newPlan))
            .where(users.id.eq(userId))
            .execute();
    }
}
```

---

## Spring Data JPA Integration

### Custom Repository Implementation

#### 1. Define Custom Interface

```java
public interface UserRepositoryCustom {
    List<User> findByMetadataField(String field, String value);
    List<User> findUsersWithPermission(String permission);
    Map<String, Long> countUsersByRole();
}
```

#### 2. Extend JsonFunctionRepositorySupport

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
    public List<User> findByMetadataField(String field, String value) {
        JPAJsonExpression metadata = jsonExpression(user.metadata);

        return from(user)
            .where(metadata.extract("$." + field)
                .eq("\"" + value + "\""))
            .fetch();
    }

    @Override
    public List<User> findUsersWithPermission(String permission) {
        return from(user)
            .where(memberOf(permission, user.permissions))
            .fetch();
    }

    @Override
    public Map<String, Long> countUsersByRole() {
        List<Tuple> results = getQueryFactory()
            .select(
                jsonExtract(user.metadata, "$.role"),
                user.count()
            )
            .from(user)
            .groupBy(jsonExtract(user.metadata, "$.role"))
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                t -> t.get(0, String.class),
                t -> t.get(1, Long.class)
            ));
    }
}
```

#### 3. Main Repository Interface

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>,
                                        UserRepositoryCustom {
    // Standard Spring Data methods
    List<User> findByName(String name);
    Optional<User> findByEmail(String email);
}
```

#### 4. Service Usage

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAdminUsers() {
        return userRepository.findByMetadataField("role", "admin");
    }

    public List<User> getUsersWithPermission(String permission) {
        return userRepository.findUsersWithPermission(permission);
    }

    public Map<String, Long> getUserCountByRole() {
        return userRepository.countUsersByRole();
    }
}
```

---

## Standalone JPA Integration

### Without Spring Framework

```java
import jakarta.persistence.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {

    private static EntityManagerFactory emf;

    public static void initialize() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url",
            "jdbc:mysql://localhost:3306/mydb");
        properties.put("jakarta.persistence.jdbc.user", "root");
        properties.put("jakarta.persistence.jdbc.password", "password");
        properties.put("jakarta.persistence.jdbc.driver",
            "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.dialect",
            "org.hibernate.dialect.MySQLDialect");

        emf = Persistence.createEntityManagerFactory("myapp-pu", properties);
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static JPAQueryFactory getQueryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}

// Usage
public class UserDAO {

    public List<User> findAdminUsers() {
        EntityManager em = DatabaseManager.getEntityManager();
        JPAQueryFactory queryFactory = DatabaseManager.getQueryFactory(em);

        try {
            return queryFactory
                .selectFrom(user)
                .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
                    .eq("\"admin\""))
                .fetch();
        } finally {
            em.close();
        }
    }
}
```

---

## Spring JDBC Integration

### DataSource Configuration

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("root");
        config.setPassword("password");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}
```

### JdbcTemplate with JSON Functions

```java
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> findUsersByRole(String role) {
        String sql = """
            SELECT id, name, email
            FROM users
            WHERE JSON_EXTRACT(metadata, '$.role') = ?
            """;
        return jdbcTemplate.queryForList(sql, "\"" + role + "\"");
    }

    public int updateUserPlan(Long userId, String newPlan) {
        String sql = """
            UPDATE users
            SET metadata = JSON_SET(metadata, '$.plan', ?)
            WHERE id = ?
            """;
        return jdbcTemplate.update(sql, newPlan, userId);
    }
}
```

---

## Testing Integration

### Testcontainers Setup

```java
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @Test
    void testJsonQueryFunctions() {
        // Test implementation
        User user = new User("John", "john@example.com");
        user.setMetadata("{\"role\":\"admin\",\"plan\":\"premium\"}");
        userRepository.save(user);

        List<User> admins = userRepository.findByMetadataField("role", "admin");
        assertThat(admins).hasSize(1);
    }
}
```

### Unit Testing with H2 (Limited JSON Support)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testBasicJsonOperations() {
        // Note: H2 has limited JSON support
        // Use MySQL Testcontainer for full JSON testing
        User user = new User("Jane", "jane@example.com");
        user.setMetadata("{\"role\":\"user\"}");

        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
    }
}
```

---

## Connection Pooling

### HikariCP Configuration

```java
@Configuration
public class HikariCPConfig {

    @Bean
    public DataSource hikariDataSource() {
        HikariConfig config = new HikariConfig();

        // Basic settings
        config.setJdbcUrl("jdbc:mysql://localhost:3306/mydb");
        config.setUsername("root");
        config.setPassword("password");

        // Pool sizing
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        // Connection timeout
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);      // 10 minutes
        config.setMaxLifetime(1800000);     // 30 minutes

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }
}
```

---

## Transaction Management

### JPA Transactions

```java
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserTransactionService {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Transactional
    public void updateMultipleUsers(List<Long> userIds, String newRole) {
        for (Long userId : userIds) {
            User user = entityManager.find(User.class, userId);
            if (user != null) {
                String updated = JPAJsonFunctions
                    .jsonSet(user.getMetadata(), "$.role", newRole)
                    .toString();
                user.setMetadata(updated);
            }
        }
        // All changes committed together
    }

    @Transactional(readOnly = true)
    public List<User> findUsers(String role) {
        return queryFactory
            .selectFrom(user)
            .where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
                .eq("\"" + role + "\""))
            .fetch();
    }
}
```

### SQL Transactions

```java
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class UserSqlTransactionService {

    private final SQLQueryFactory queryFactory;
    private final TransactionTemplate transactionTemplate;

    public void updateUsersInTransaction(List<Long> userIds, String newPlan) {
        transactionTemplate.execute(status -> {
            for (Long userId : userIds) {
                queryFactory
                    .update(users)
                    .set(users.metadata,
                        SqlJsonFunctions.jsonSet(users.metadata, "$.plan", newPlan))
                    .where(users.id.eq(userId))
                    .execute();
            }
            return null;
        });
    }
}
```

---

## Common Patterns

### 1. Repository Layer Pattern

```java
@Repository
public class GenericJsonRepository<T> {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    public List<T> findByJsonField(
            EntityPath<T> entityPath,
            StringPath jsonColumn,
            String field,
            String value) {

        return queryFactory
            .selectFrom(entityPath)
            .where(JPAJsonFunctions.jsonExtract(jsonColumn, "$." + field)
                .eq("\"" + value + "\""))
            .fetch();
    }
}
```

### 2. DTO Projection Pattern

```java
@Value
public class UserSummaryDTO {
    Long id;
    String name;
    String role;
    String plan;
}

@Repository
public class UserProjectionRepository {

    private final JPAQueryFactory queryFactory;

    public List<UserSummaryDTO> getUserSummaries() {
        return queryFactory
            .select(Projections.constructor(
                UserSummaryDTO.class,
                user.id,
                user.name,
                JPAJsonFunctions.jsonExtract(user.metadata, "$.role"),
                JPAJsonFunctions.jsonExtract(user.metadata, "$.plan")
            ))
            .from(user)
            .fetch();
    }
}
```

### 3. Specification Pattern

```java
public class UserSpecifications {

    public static BooleanExpression hasRole(String role) {
        return JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
            .eq("\"" + role + "\"");
    }

    public static BooleanExpression hasPlan(String plan) {
        return JPAJsonFunctions.jsonExtract(user.metadata, "$.plan")
            .eq("\"" + plan + "\"");
    }

    public static BooleanExpression isActive() {
        JPAJsonExpression metadata = JPAJsonExpression.of(user.metadata);
        return metadata.extract("$.status").eq("\"active\"");
    }
}

// Usage
List<User> activeAdmins = queryFactory
    .selectFrom(user)
    .where(
        UserSpecifications.hasRole("admin")
            .and(UserSpecifications.isActive())
    )
    .fetch();
```

---

## Troubleshooting

### Issue: Q-classes not generated

**Solution:**
```bash
# Gradle
./gradlew clean compileJava

# Maven
mvn clean compile
```

Ensure annotation processors are configured:
```kotlin
annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")
```

### Issue: JSON column not recognized

**Solution:**
Always specify `columnDefinition = "JSON"`:
```java
@Column(columnDefinition = "JSON")
private String metadata;
```

### Issue: Transaction not working

**Solution:**
Ensure `@Transactional` is on the service layer, not repository:
```java
@Service
@Transactional  // Correct
public class UserService {
    // ...
}
```

### Issue: Connection pool exhaustion

**Solution:**
Configure connection pool properly:
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

### Issue: Slow JSON queries

**Solutions:**
1. Create functional indexes:
```sql
CREATE INDEX idx_user_role
ON users((CAST(JSON_EXTRACT(metadata, '$.role') AS CHAR(50))));
```

2. Use generated columns:
```java
@Column(name = "role_generated",
        columnDefinition = "VARCHAR(50) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')))")
private String roleGenerated;

@Index(name = "idx_role", columnList = "role_generated")
```

### Issue: MySQL version compatibility

**Solution:**
Ensure MySQL 8.0.17+ and enable JSON functions:
```sql
SELECT VERSION();  -- Should be 8.0.17 or higher
SHOW VARIABLES LIKE 'sql_mode';  -- Check for JSON support
```

---

## Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [QueryDSL Reference](http://querydsl.com/static/querydsl/latest/reference/html/)
- [MySQL JSON Functions](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html)
- [Testcontainers Documentation](https://www.testcontainers.org/)

---

**Need More Help?** Check the [main README](./README.md) or [module-specific documentation](./querydsl-mysql-json-jpa/README.md).
