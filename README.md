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
    .eq("admin");
```

### After
```java
// Type-safe method chaining with IDE autocompletion
user.metadata
    .jsonExtract("$.role")
    .eq("admin");
```

---

## Features

### Complete MySQL JSON Function Coverage

This library provides type-safe QueryDSL expressions for all 35 MySQL JSON functions:

| Category | Count | Functions |
| :--- | :---: | :--- |
| **Creation Functions** | 3 | `JSON_ARRAY`, `JSON_OBJECT`, `JSON_QUOTE` |
| **Search Functions** | 10 | `JSON_EXTRACT`, `JSON_CONTAINS`, `JSON_SEARCH`, `JSON_VALUE`, `JSON_OVERLAPS`, `MEMBER OF`, and more |
| **Modification Functions** | 10 | `JSON_SET`, `JSON_INSERT`, `JSON_REPLACE`, `JSON_REMOVE`, `JSON_ARRAY_APPEND`, `JSON_MERGE_PATCH`, and more |
| **Attribute Functions** | 4 | `JSON_DEPTH`, `JSON_LENGTH`, `JSON_TYPE`, `JSON_VALID` |
| **Utility Functions** | 3 | `JSON_PRETTY`, `JSON_STORAGE_SIZE`, `JSON_STORAGE_FREE` |
| **Schema Validation** | 2 | `JSON_SCHEMA_VALID`, `JSON_SCHEMA_VALIDATION_REPORT` |
| **Aggregate Functions** | 2 | `JSON_ARRAYAGG`, `JSON_OBJECTAGG` |
| **Table Functions** | 1 | `JSON_TABLE` |

### Multi-Module

Choose only the modules you need:

| Module | Description |
| :--- | :--- |
| **`querydsl-mysql-json-core`** | Core functionality and operators |
| **`querydsl-mysql-json-sql`** | Support for QueryDSL SQL module |
| **`querydsl-mysql-json-jpa`** | Support for QueryDSL JPA module |

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
    implementation 'com.github.snowykte0426:querydsl-mysql-json-core:0.1.0-Dev.1'

    // Choose your module
    implementation 'com.github.snowykte0426:querydsl-mysql-json-sql:0.1.0-Dev.1'   // For SQL
    // OR
    implementation 'com.github.snowykte0426:querydsl-mysql-json-jpa:0.1.0-Dev.1'   // For JPA
}
```

> **Note**: Binary releases are not yet available. The project is in early development.

---

## Quick Start

### Basic Usage Examples

```java
// JSON_EXTRACT: Extract value from JSON path
List<User> admins = queryFactory
    .selectFrom(user)
    .where(user.metadata.jsonExtract("$.role").eq("admin"))
    .fetch();

// JSON_CONTAINS: Check if JSON contains value
List<User> users = queryFactory
    .selectFrom(user)
    .where(JsonFunctions.jsonContains(
        user.preferences,
        "\"notifications\"",
        "$.settings"
    ))
    .fetch();

// JSON_SET: Update JSON value
queryFactory
    .update(user)
    .set(user.metadata, JsonFunctions.jsonSet(
        user.metadata,
        "$.lastLogin",
        LocalDateTime.now()
    ))
    .where(user.id.eq(userId))
    .execute();

// JSON_ARRAYAGG: Aggregate as JSON array
List<String> emailsPerDept = queryFactory
    .select(JsonFunctions.arrayAgg(user.email))
    .from(user)
    .groupBy(user.department)
    .fetch();
```

---

## Requirements

- Java 17 or higher
- MySQL 8.0.17 or higher
- QueryDSL (OpenFeign fork) 7.1

---

## Project Status

### v0.1.0-Dev.1 (Current)
- Project infrastructure setup
- Multi-module Gradle configuration
- 35 JSON operators defined as enums
- OpenFeign QueryDSL 7.1 integration
- Initial build system complete

### v0.1.0-Dev.2 (Next)
- Expression classes implementation
- Search functions implementation
- Creation functions implementation
- Basic integration tests with Testcontainers

### v0.1.0-Dev.3
- Modification functions implementation
- Attribute and utility functions
- Comprehensive test coverage

### v0.1.0 (Stable Release)
- All 35 functions fully implemented
- Complete documentation and examples
- Production-ready quality
- Maven Central publication

---

## Documentation

- [MySQL JSON Functions Reference](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html) - Official MySQL documentation

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
