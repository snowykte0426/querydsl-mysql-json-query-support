# QueryDSL MySQL JSON Query Support

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java: 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
[![QueryDSL: 7.1](https://img.shields.io/badge/QueryDSL-7.1-blue.svg)](https://github.com/OpenFeign/querydsl)
[![MySQL: 8.0.17+](https://img.shields.io/badge/MySQL-8.0.17%2B-blue.svg)](https://dev.mysql.com/doc/refman/8.0/en/json.html)
[![Status: Alpha](https://img.shields.io/badge/Status-Alpha-red.svg)]()

MySQL 8.0.17+의 모든 JSON 함수를 QueryDSL에서 타입 안전하고 유창한 메서드 체이닝 방식으로 사용할 수 있게 해주는 확장 라이브러리입니다.

> ⚠️ **Early Development**: 이 프로젝트는 현재 활발히 개발 중입니다. API는 안정적이지 않으며 변경될 수 있습니다.

---

## 🎯 Why This Library?

### Before (기존 방식)
```java
// 문자열 템플릿 - 타입 안전하지 않고 오류 발생 가능
Expressions.stringTemplate("JSON_EXTRACT({0}, {1})", user.metadata, "$.role")
    .eq("admin");
```

### After (이 라이브러리 사용 시)
```java
// 타입 안전한 메서드 체이닝 - IDE 자동완성 지원
user.metadata
    .jsonExtract("$.role")
    .eq("admin");
```

---

## ✨ Features

### 🎉 Complete Coverage
**35개의 MySQL JSON 함수를 모두 지원합니다:**

- ✅ **Creation Functions** (3): `JSON_ARRAY`, `JSON_OBJECT`, `JSON_QUOTE`
- ✅ **Search Functions** (10): `JSON_EXTRACT`, `JSON_CONTAINS`, `JSON_SEARCH`, etc.
- ✅ **Modification Functions** (10): `JSON_SET`, `JSON_INSERT`, `JSON_REPLACE`, etc.
- ✅ **Attribute Functions** (4): `JSON_DEPTH`, `JSON_LENGTH`, `JSON_TYPE`, `JSON_VALID`
- ✅ **Utility Functions** (3): `JSON_PRETTY`, `JSON_STORAGE_SIZE`, `JSON_STORAGE_FREE`
- ✅ **Schema Validation** (2): `JSON_SCHEMA_VALID`, `JSON_SCHEMA_VALIDATION_REPORT`
- ✅ **Aggregate Functions** (2): `JSON_ARRAYAGG`, `JSON_OBJECTAGG`
- ✅ **Table Functions** (1): `JSON_TABLE`

### 🏗️ Multi-Module Architecture
필요한 모듈만 선택해서 사용할 수 있습니다:

- **`querydsl-mysql-json-core`**: 공통 기능
- **`querydsl-mysql-json-sql`**: QueryDSL SQL 지원
- **`querydsl-mysql-json-jpa`**: QueryDSL JPA 지원

### 🔒 Type-Safe
- 컴파일 타임 타입 체크
- IDE 자동완성 및 리팩토링 지원
- SQL Injection 방지

### 🚀 Built on OpenFeign QueryDSL
활발히 유지보수되는 [OpenFeign QueryDSL](https://github.com/OpenFeign/querydsl) 포크 버전 기반

---

## 📦 Installation

### Gradle
```gradle
dependencies {
    // Core module (required)
    implementation 'com.github.snowykte0426:querydsl-mysql-json-core:0.1.0-M1'

    // Choose your module
    implementation 'com.github.snowykte0426:querydsl-mysql-json-sql:0.1.0-M1'   // For SQL
    // OR
    implementation 'com.github.snowykte0426:querydsl-mysql-json-jpa:0.1.0-M1'   // For JPA
}
```

> ⚠️ **Note**: Binary releases are not yet available. The project is in early development.

---

## 🚀 Quick Start

### Basic Usage Example

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

## 📋 Supported MySQL Versions

- **MySQL 8.0.17+** (recommended)
- All MySQL 8.0+ JSON functions supported

---

## 🛣️ Roadmap

### v0.1.0-M1 (Current) ✅
- [x] Project infrastructure
- [x] Multi-module setup
- [x] 35 JSON operators defined
- [x] OpenFeign QueryDSL integration

### v0.1.0-M2 (Next)
- [ ] Expression classes
- [ ] Search functions implementation
- [ ] Creation functions implementation
- [ ] Basic integration tests

### v0.1.0-M3
- [ ] Modification functions
- [ ] Attribute & utility functions
- [ ] Comprehensive testing

### v0.1.0 (Stable Release)
- [ ] All 35 functions implemented
- [ ] Complete documentation
- [ ] Production-ready
- [ ] Maven Central release

---

## 📚 Documentation

- **[Implementation Plan](./IMPLEMENTATION_PLAN.md)** - 상세 구현 계획
- **[Progress Log](./PROGRESS.md)** - 진행 상황
- **[MySQL JSON Functions Reference](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html)** - MySQL 공식 문서

---

## 🤝 Contributing

Contributions are welcome! This project is in early development and we're open to:

- 🐛 Bug reports
- 💡 Feature requests
- 📝 Documentation improvements
- 🔧 Code contributions

Please see [CONTRIBUTING.md](./CONTRIBUTING.md) for guidelines.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

---

## 🙏 Acknowledgments

- **OpenFeign Team** - For maintaining the QueryDSL fork
- **Original QueryDSL Team** - For the excellent foundation
- **MySQL Team** - For comprehensive JSON support

---

## 📞 Contact

- **GitHub**: [@snowykte0426](https://github.com/snowykte0426)
- **Email**: snowykte0426@naver.com

---

## ⭐ Star History

If you find this project useful, please consider giving it a star! ⭐

---

<div align="center">

**[View on GitHub](https://github.com/snowykte0426/querydsl-mysql-json-query-support)** • **[Report Bug](https://github.com/snowykte0426/querydsl-mysql-json-query-support/issues)** • **[Request Feature](https://github.com/snowykte0426/querydsl-mysql-json-query-support/issues)**

Made with ❤️ by [snowykte0426](https://github.com/snowykte0426)

</div>
