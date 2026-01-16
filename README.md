# QueryDSL MySQL JSON Query Support

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java: 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![QueryDSL: 7.1](https://img.shields.io/badge/QueryDSL-7.1-blue.svg)](https://github.com/OpenFeign/querydsl)
[![MySQL: 8.0.17+](https://img.shields.io/badge/MySQL-8.0.17%2B-blue.svg)](https://dev.mysql.com/doc/refman/8.0/en/json.html)
[![Status: Alpha](https://img.shields.io/badge/Status-Alpha-red.svg)]()

A QueryDSL extension library that enables type-safe, fluent method chaining for all MySQL 8.0.17+ JSON functions.

> **Warning**: This project is in early development. APIs are unstable and subject to change.

---

## Why This Library?

### Before
```java
// String-based templates - not type-safe, error-prone
Expressions.stringTemplate("JSON_EXTRACT({0}, {1})", user.metadata, "$.role")
    .eq("\"admin\"");
```

### After (JPA Module)
```java
// Type-safe static functions with IDE autocompletion
JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
    .eq("\"admin\"");

// Or fluent API for method chaining
JPAJsonExpression.of(user.metadata)
    .extract("$.role")
    .eq("\"admin\"");
```

### After (SQL Module)
```java
// Type-safe static functions
SqlJsonFunctions.jsonExtract(user.metadata, "$.role")
    .eq("\"admin\"");

// Or fluent API
SqlJsonExpression.of(user.metadata)
    .extract("$.role")
    .eq("\"admin\"");
```

---

## Features

### Complete MySQL JSON Function Coverage

This library provides type-safe QueryDSL expressions for all 35 MySQL JSON functions:

| Category                   | Count | Functions                                                                                                   |
|:---------------------------|:-----:|:------------------------------------------------------------------------------------------------------------|
| **Creation Functions**     |   3   | `JSON_ARRAY`, `JSON_OBJECT`, `JSON_QUOTE`                                                                   |
| **Search Functions**       |  10   | `JSON_EXTRACT`, `JSON_CONTAINS`, `JSON_SEARCH`, `JSON_VALUE`, `JSON_OVERLAPS`, `MEMBER OF`, and more        |
| **Modification Functions** |  10   | `JSON_SET`, `JSON_INSERT`, `JSON_REPLACE`, `JSON_REMOVE`, `JSON_ARRAY_APPEND`, `JSON_MERGE_PATCH`, and more |
| **Attribute Functions**    |   4   | `JSON_DEPTH`, `JSON_LENGTH`, `JSON_TYPE`, `JSON_VALID`                                                      |
| **Utility Functions**      |   3   | `JSON_PRETTY`, `JSON_STORAGE_SIZE`, `JSON_STORAGE_FREE`                                                     |
| **Schema Validation**      |   2   | `JSON_SCHEMA_VALID`, `JSON_SCHEMA_VALIDATION_REPORT`                                                        |
| **Aggregate Functions**    |   2   | `JSON_ARRAYAGG`, `JSON_OBJECTAGG`                                                                           |
| **Table Functions**        |   1   | `JSON_TABLE`                                                                                                |

### Multi-Module

Choose only the modules you need:

| Module                         | Description                      |
|:-------------------------------|:---------------------------------|
| **`querydsl-mysql-json-core`** | Core functionality and operators |
| **`querydsl-mysql-json-sql`**  | Support for QueryDSL SQL module  |
| **`querydsl-mysql-json-jpa`**  | Support for QueryDSL JPA module  |

### Type Safety

- Compile-time type checking
- IDE autocompletion and refactoring support
- SQL injection prevention through parameterized queries

### Built on OpenFeign QueryDSL

Based on the actively maintained [OpenFeign QueryDSL](https://github.com/OpenFeign/querydsl) fork (version 7.1).

---

## Installation

### Gradle
```gradle
dependencies {
    // Core module (required)
    implementation 'io.github.snowykte0426:querydsl-mysql-json-core:0.1.0-Dev.3'

    // Choose your module
    implementation 'io.github.snowykte0426:querydsl-mysql-json-sql:0.1.0-Dev.3'   // For SQL
    // OR
    implementation 'io.github.snowykte0426:querydsl-mysql-json-jpa:0.1.0-Dev.3'   // For JPA
}
```

> **Note**: Binary releases are not yet available. The project is in early development.

---

## Quick Start

### JPA Module Example

```java
import jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonFunctions;
import expressions.jpa.io.github.snowykte0426.querydsl.mysql.json.JPAJsonExpression;
import static com.example.entity.QUser.user;

// Static function style
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

// JSON_CONTAINS: Check if JSON contains value
List<User> users = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonContains(
        user.preferences,
        "\"notifications\"",
        "$.settings"
    ))
    .fetch();

// JSON_ARRAYAGG: Aggregate as JSON array
List<Tuple> emailsByDept = queryFactory
    .select(
        user.department,
        JPAJsonFunctions.jsonArrayAgg(user.email)
    )
    .from(user)
    .groupBy(user.department)
    .fetch();
```

### SQL Module Example

```java
import sql.io.github.snowykte0426.querydsl.mysql.json.SqlJsonFunctions;
import expressions.sql.io.github.snowykte0426.querydsl.mysql.json.SqlJsonExpression;
import static com.example.QUsers.users;

// Configure QueryFactory with MySQLJsonTemplates
Configuration configuration = new Configuration(MySQLJsonTemplates.DEFAULT);
SQLQueryFactory queryFactory = new SQLQueryFactory(configuration, dataSource);

// Static function style
List<Tuple> admins = queryFactory
    .select(users.id, users.name, users.email)
    .from(users)
    .where(SqlJsonFunctions.jsonExtract(users.metadata, "$.role")
        .eq("\"admin\""))
    .fetch();

// Fluent API style
SqlJsonExpression metadata = SqlJsonExpression.of(users.metadata);

List<Tuple> premiumUsers = queryFactory
    .select(users.all())
    .from(users)
    .where(metadata.extract("$.plan").eq("\"premium\""))
    .fetch();

// Complex query with joins
List<Tuple> results = queryFactory
    .select(users.name, products.name)
    .from(users)
    .join(products)
    .on(SqlJsonFunctions.memberOf(products.category, users.interests))
    .fetch();

// JSON_TABLE for flattening JSON arrays
SqlJsonFunctions.jsonTable(
    users.orders,
    "$.items[*]"
)
.column("item_id", "INT", "$.id")
.column("item_name", "VARCHAR(255)", "$.name")
.column("quantity", "INT", "$.quantity")
.build();
```

### Module Comparison

| Feature                 | JPA Module                                        | SQL Module                           |
|-------------------------|---------------------------------------------------|--------------------------------------|
| **Use Case**            | JPA entities with JSON columns                    | Direct SQL queries, complex queries  |
| **Configuration**       | EntityManager + JPAQueryFactory                   | SQLQueryFactory + MySQLJsonTemplates |
| **Entity Mapping**      | `@Entity` with `@Column(columnDefinition="JSON")` | Q-classes from `CREATE TABLE` schema |
| **API Entry Point**     | `JPAJsonFunctions`                                | `SqlJsonFunctions`                   |
| **Fluent API**          | `JPAJsonExpression`                               | `SqlJsonExpression`                  |
| **Transaction Support** | JPA transactions (`@Transactional`)               | Manual JDBC transactions             |
| **Spring Integration**  | `JsonFunctionRepositorySupport`                   | Standard Spring JDBC                 |
| **Updates**             | Entity merge + `em.merge()`                       | Direct `UPDATE` statements           |
| **Performance**         | ORM overhead                                      | Direct SQL (faster)                  |
| **Best For**            | Domain-driven design, ORM apps                    | High-performance, reporting queries  |

**Choosing a Module:**
- Use **JPA Module** if you're building a standard Spring Boot application with JPA entities
- Use **SQL Module** if you need maximum performance or complex analytical queries
- Both modules delegate 100% to the core module, ensuring consistent behavior

For detailed examples and documentation:
- [JPA Module README](./querydsl-mysql-json-jpa/README.md)
- [SQL Module README](./querydsl-mysql-json-sql/README.md)

---

## Requirements

- Java 17 or higher
- MySQL 8.0.17 or higher
- QueryDSL (OpenFeign fork) 7.1

---

## Documentation

### Module Documentation
- [JPA Module README](./querydsl-mysql-json-jpa/README.md) - Complete JPA module guide with Spring Data integration
- [SQL Module README](./querydsl-mysql-json-sql/README.md) - Complete SQL module guide with MySQLJsonTemplates
- [Core Module](./querydsl-mysql-json-core/README.md) - Core module internals (advanced users)

### Project Documentation
- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - Complete reference for all 35 JSON functions
- [PROGRESS.md](./PROGRESS.md) - Development progress and testing status

### External Resources
- [MySQL JSON Functions Reference](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html) - Official MySQL documentation
- [QueryDSL Documentation](http://querydsl.com/static/querydsl/latest/reference/html/) - QueryDSL reference
- [OpenFeign QueryDSL](https://github.com/OpenFeign/querydsl) - Maintained QueryDSL fork

---

## Contributing

Contributions are welcome! This project is in early development and we're looking for:

- Bug reports
- Feature requests
- Documentation improvements
- Code contributions

---

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

---

## Acknowledgments

- **OpenFeign Team** - For maintaining the QueryDSL fork
- **Original QueryDSL Team** - For the excellent type-safe query DSL foundation
- **MySQL Team** - For comprehensive JSON function support

---

## Contact

- GitHub: [@snowykte0426](https://github.com/snowykte0426)
- Email: snowykte0426@naver.com

---

<div align="center">

[View on GitHub](https://github.com/snowykte0426/querydsl-mysql-json-query-support) • [Report Bug](https://github.com/snowykte0426/querydsl-mysql-json-query-support/issues) • [Request Feature](https://github.com/snowykte0426/querydsl-mysql-json-query-support/issues)

</div>
