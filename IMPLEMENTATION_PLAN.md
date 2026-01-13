# QueryDSL MySQL JSON Query Support - Implementation Plan

## Project Overview

프로젝트명: querydsl-mysql-json-query-support
목적: MySQL의 JSON 관련 함수들을 QueryDSL에서 메서드 체인 방식으로 사용할 수 있도록 확장

## Current Status

- **프로젝트 상태**: 멀티 모듈 구조 설정 완료
- **빌드 설정**: Gradle 9.2.1, Java 17/25, JUnit 5
- **QueryDSL 버전**: OpenFeign QueryDSL 7.1 (io.github.openfeign.querydsl)
- **모듈 구조**: core, sql, jpa 3개 모듈
- **참고**: 원본 QueryDSL은 운영 종료, OpenFeign 포크 버전 사용

## MySQL JSON Functions (Complete List)

### 1. Functions That Create JSON Values (3개)
- `JSON_ARRAY()` - JSON 배열 생성
- `JSON_OBJECT()` - JSON 객체 생성
- `JSON_QUOTE()` - JSON 문서 인용

### 2. Functions That Search JSON Values (10개)
- `->` - JSON 컬럼에서 값 추출 (JSON_EXTRACT와 동일)
- `->>` - JSON 컬럼에서 값 추출 후 언퀴팅
- `JSON_CONTAINS()` - JSON 문서가 특정 객체를 포함하는지 확인
- `JSON_CONTAINS_PATH()` - JSON 문서가 경로에 데이터를 포함하는지 확인
- `JSON_EXTRACT()` - JSON 문서에서 데이터 반환
- `JSON_KEYS()` - JSON 문서의 키 배열 반환
- `JSON_SEARCH()` - JSON 문서 내 값의 경로 반환
- `JSON_VALUE()` - JSON 문서에서 스칼라 값 추출 (8.0.21+)
- `JSON_OVERLAPS()` - 두 JSON 문서 비교 (8.0.17+)
- `MEMBER OF()` - 첫 번째 피연산자가 JSON 배열의 요소와 일치하는지 확인 (8.0.17+)

### 3. Functions That Modify JSON Values (10개)
- `JSON_ARRAY_APPEND()` - JSON 문서에 데이터 추가
- `JSON_ARRAY_INSERT()` - JSON 배열에 삽입
- `JSON_INSERT()` - JSON 문서에 데이터 삽입
- `JSON_MERGE()` - JSON 문서 병합 (Deprecated)
- `JSON_MERGE_PATCH()` - JSON 문서 병합 (중복 키 값 교체)
- `JSON_MERGE_PRESERVE()` - JSON 문서 병합 (중복 키 보존)
- `JSON_REMOVE()` - JSON 문서에서 데이터 제거
- `JSON_REPLACE()` - JSON 문서의 값 교체
- `JSON_SET()` - JSON 문서에 데이터 삽입
- `JSON_UNQUOTE()` - JSON 값 언퀴팅

### 4. Functions That Return JSON Value Attributes (4개)
- `JSON_DEPTH()` - JSON 문서의 최대 깊이
- `JSON_LENGTH()` - JSON 문서의 요소 개수
- `JSON_TYPE()` - JSON 값의 타입
- `JSON_VALID()` - JSON 값이 유효한지 확인

### 5. JSON Utility Functions (3개)
- `JSON_PRETTY()` - JSON 문서를 읽기 쉬운 형식으로 출력
- `JSON_STORAGE_FREE()` - 부분 업데이트 후 해제된 공간
- `JSON_STORAGE_SIZE()` - JSON 문서의 저장 공간 크기

### 6. JSON Table Functions (1개)
- `JSON_TABLE()` - JSON 표현식을 관계형 테이블로 반환

### 7. JSON Schema Validation Functions (2개)
- `JSON_SCHEMA_VALID()` - JSON 스키마 검증 (8.0.17+)
- `JSON_SCHEMA_VALIDATION_REPORT()` - JSON 스키마 검증 리포트 (8.0.17+)

### 8. Aggregate JSON Functions (2개)
- `JSON_ARRAYAGG()` - 값을 JSON 배열로 집계
- `JSON_OBJECTAGG()` - 값을 JSON 객체로 집계

**총 35개 함수**

## Module Architecture (멀티 모듈 구조)

### Module Hierarchy
```
querydsl-mysql-json-query-support (Root Project)
├── querydsl-mysql-json-core
│   └── 공통 기능: Operator, Expression, Path, Functions
├── querydsl-mysql-json-sql
│   └── SQL 모듈 전용: MySQLTemplates, SQLJsonExpression
└── querydsl-mysql-json-jpa
    └── JPA 모듈 전용: JPAJsonFunctions, JPAJsonExpression
```

### Gradle Settings Configuration
```kotlin
// settings.gradle.kts
rootProject.name = "querydsl-mysql-json-query-support"

include(
    "querydsl-mysql-json-core",
    "querydsl-mysql-json-sql",
    "querydsl-mysql-json-jpa"
)
```

### Module Dependencies
```kotlin
// querydsl-mysql-json-sql/build.gradle.kts
dependencies {
    api(project(":querydsl-mysql-json-core"))
    api("com.querydsl:querydsl-sql:5.1.0")
}

// querydsl-mysql-json-jpa/build.gradle.kts
dependencies {
    api(project(":querydsl-mysql-json-core"))
    api("com.querydsl:querydsl-jpa:5.1.0")
}
```

## Detailed Package Structure

```
com.github.snowykte0426.querydsl.mysql.json/
├── core/                                    # Shared core functionality
│   ├── operators/
│   │   ├── JsonOperators.java              # Central registry of all JSON operators
│   │   └── JsonOperatorTemplates.java      # SQL template definitions
│   ├── types/
│   │   ├── JsonExpression.java             # Base JSON expression class
│   │   ├── JsonPath.java                   # JSON path representation
│   │   └── JsonPathExpression.java         # Expression for JSON paths
│   ├── expressions/
│   │   ├── JsonArrayExpression.java        # For JSON array operations
│   │   ├── JsonObjectExpression.java       # For JSON object operations
│   │   └── JsonValueExpression.java        # For JSON scalar values
│   └── functions/
│       ├── JsonCreationFunctions.java      # Factory methods for creation functions
│       ├── JsonSearchFunctions.java        # Factory methods for search functions
│       ├── JsonModifyFunctions.java        # Factory methods for modify functions
│       ├── JsonAttributeFunctions.java     # Factory methods for attribute functions
│       ├── JsonUtilityFunctions.java       # Factory methods for utility functions
│       ├── JsonSchemaFunctions.java        # Factory methods for schema functions
│       └── JsonAggregateFunctions.java     # Factory methods for aggregate functions
│
├── sql/                                     # SQL-specific implementation
│   ├── MySQLJsonTemplates.java             # MySQL-specific SQL templates
│   ├── MySQLJsonQuery.java                 # Custom query extension for SQL module
│   ├── expressions/
│   │   ├── SQLJsonExpression.java          # SQL-specific JSON expression
│   │   ├── SQLJsonPathExpression.java      # SQL path expressions
│   │   └── SQLJsonTableExpression.java     # For JSON_TABLE function
│   └── mixins/
│       ├── JsonExpressionMixin.java        # Mixin interface for SQL JSON operations
│       └── JsonPathMixin.java              # Mixin for path-based operations
│
├── jpa/                                     # JPA-specific implementation
│   ├── JPAJsonFunctions.java               # JPA function registration
│   ├── expressions/
│   │   ├── JPAJsonExpression.java          # JPA-specific JSON expression
│   │   └── JPAJsonPathExpression.java      # JPA path expressions
│   └── mixins/
│       ├── JsonExpressionMixin.java        # Mixin interface for JPA JSON operations
│       └── JsonPathMixin.java              # Mixin for path-based operations
│
└── util/
    ├── JsonPathBuilder.java                # Builder for JSON path construction
    ├── JsonValidation.java                 # Validation utilities
    └── JsonTypeConverter.java              # Type conversion utilities
```

## Implementation Phases

### Phase 1: Foundation and Core Infrastructure (Week 1-2)

**Step 1.1: Project Setup and Dependencies**
- [x] Update build.gradle.kts with all required dependencies (OpenFeign QueryDSL 7.1)
- [x] Configure annotation processors for code generation
- [x] Set up module structure (core, sql, jpa)

**Step 1.2: Core Operator Registry**
- [x] Create `JsonOperators.java` with all 35 MySQL JSON functions (as enum)
- [x] Define operator precedence and argument types
- [x] Implement `JsonOperatorTemplates.java` with SQL template strings

**Step 1.3: Base Expression Classes**
- [x] Implement `JsonExpression` as base class
- [x] Create `JsonPath` and `JsonPathExpression` for path handling
- [x] Build `JsonArrayExpression`, `JsonObjectExpression`, `JsonValueExpression`

**Step 1.4: Testing Infrastructure**
- [x] Set up Testcontainers with MySQL 8.0.17+
- [x] Create `AbstractJsonFunctionTest` base test class
- [x] Configure test database schemas
- [x] Create `TestDataBuilder` for test data generation

### Phase 2: Creation and Search Functions (Week 3-4)

**Step 2.1: Creation Functions (3 functions)**
- [x] Implement `JSON_ARRAY()` with varargs support
- [x] Implement `JSON_OBJECT()` with key-value pairs
- [x] Implement `JSON_QUOTE()` for string escaping
- [x] Create `JsonCreationFunctions.java` factory class
- [x] Write integration tests for creation functions

**Step 2.2: Basic Search Functions (6 functions)**
- [x] Implement `JSON_EXTRACT()` with path expressions
- [x] Implement `JSON_VALUE()` with type conversion
- [x] Implement `->` and `->>` operators
- [x] Implement `JSON_CONTAINS()` and `JSON_CONTAINS_PATH()`
- [x] Create `JsonSearchFunctions.java` factory class
- [x] Write comprehensive tests for each function

**Step 2.3: Advanced Search Functions (4 functions)**
- [x] Implement `JSON_KEYS()` with optional path
- [x] Implement `JSON_SEARCH()` with pattern matching
- [x] Implement `JSON_OVERLAPS()` (MySQL 8.0.17+)
- [x] Implement `MEMBER OF()` operator (MySQL 8.0.17+)
- [x] Write advanced search function tests

### Phase 3: Modification and Attribute Functions (Week 5-6)

**Step 3.1: Modification Functions (10 functions)**
- [x] Implement `JSON_SET()`, `JSON_INSERT()`, `JSON_REPLACE()`
- [x] Implement `JSON_REMOVE()` with multiple paths
- [x] Implement `JSON_ARRAY_APPEND()` and `JSON_ARRAY_INSERT()`
- [x] Implement `JSON_MERGE_PATCH()` and `JSON_MERGE_PRESERVE()`
- [x] Implement `JSON_UNQUOTE()`
- [x] Create `JsonModifyFunctions.java` factory class
- [ ] Write modification function tests

**Step 3.2: Attribute Functions (4 functions)**
- [x] Implement `JSON_DEPTH()` for nesting level
- [x] Implement `JSON_LENGTH()` with optional path
- [x] Implement `JSON_TYPE()` for type inspection
- [x] Implement `JSON_VALID()` for validation
- [x] Create `JsonAttributeFunctions.java` factory class
- [ ] Write attribute function tests

### Phase 4: Utility, Schema, and Aggregate Functions (Week 7-8)

**Step 4.1: Utility Functions (3 functions)**
- [x] Implement `JSON_PRETTY()` for formatting
- [x] Implement `JSON_STORAGE_SIZE()` for size calculation
- [x] Implement `JSON_STORAGE_FREE()` for optimization info
- [x] Create `JsonUtilityFunctions.java` factory class
- [ ] Write utility function tests

**Step 4.2: Schema Functions (2 functions)**
- [x] Implement `JSON_SCHEMA_VALID()` (MySQL 8.0.17+)
- [x] Implement `JSON_SCHEMA_VALIDATION_REPORT()` (MySQL 8.0.17+)
- [x] Create `JsonSchemaFunctions.java` factory class
- [ ] Write schema validation tests

**Step 4.3: Aggregate Functions (2 functions)**
- [x] Implement `JSON_ARRAYAGG()` for array aggregation
- [x] Implement `JSON_OBJECTAGG()` for object aggregation
- [x] Create `JsonAggregateFunctions.java` factory class
- [ ] Write aggregate function tests

**Step 4.4: Table Function (1 function)**
- [ ] Design and implement `JSON_TABLE()` with column definitions
- [ ] Create `SQLJsonTableExpression` class
- [ ] Create `JsonTableBuilder` for fluent API
- [ ] Handle complex nested path specifications
- [ ] Write comprehensive JSON_TABLE tests

### Phase 5: Module Separation and Integration (Week 9-10)

**Step 5.1: SQL Module Implementation**
- [ ] Create `MySQLJsonTemplates` extending MySQLTemplates
- [ ] Implement `MySQLJsonQuery` with custom methods
- [ ] Create SQL-specific mixins and expression wrappers
- [ ] Integrate with existing QueryDSL SQL module
- [ ] Write SQL module integration tests

**Step 5.2: JPA Module Implementation**
- [ ] Register JSON functions with JPA dialect
- [ ] Create `JPAJsonFunctions` registration class
- [ ] Create JPA-specific expression wrappers
- [ ] Implement JPA mixins for method chaining
- [ ] Handle JPA-specific type conversions
- [ ] Write JPA module integration tests

**Step 5.3: Shared Core Optimization**
- [ ] Extract common functionality to core module
- [ ] Ensure both SQL and JPA modules depend on core
- [ ] Implement proper type hierarchy and inheritance
- [ ] Refactor for code reuse

### Phase 6: Testing, Documentation, and Polish (Week 11-12)

**Step 6.1: Comprehensive Testing**
- [ ] Unit tests for all 35 functions
- [ ] Integration tests with Testcontainers
- [ ] Performance benchmarking
- [ ] Edge case and error handling tests
- [ ] MySQL version compatibility tests

**Step 6.2: Documentation**
- [ ] JavaDoc for all public APIs
- [ ] README.md with comprehensive examples
- [ ] Function reference guide (all 35 functions)
- [ ] Migration guides
- [ ] Performance tuning guide
- [ ] Spring Boot integration examples

**Step 6.3: Final Polish**
- [ ] Code review and refactoring
- [ ] Performance optimization
- [ ] API stabilization
- [ ] Version 1.0 release preparation
- [ ] JitPack publishing test

## API Usage Examples

### Example 1: Simple Search
```java
// WHERE JSON_EXTRACT(metadata, '$.role') = 'admin'
queryFactory
    .select(user)
    .from(user)
    .where(user.metadata.jsonExtract("$.role").eq("admin"))
    .fetch();
```

### Example 2: JSON_CONTAINS
```java
// WHERE JSON_CONTAINS(settings, '"notifications"', '$.preferences')
queryFactory
    .select(user)
    .from(user)
    .where(JsonFunctions.jsonContains(
        user.settings,
        "\"notifications\"",
        "$.preferences"
    ))
    .fetch();
```

### Example 3: Modification
```java
// UPDATE user SET metadata = JSON_SET(metadata, '$.lastLogin', NOW())
queryFactory
    .update(user)
    .set(user.metadata, JsonFunctions.jsonSet(
        user.metadata,
        "$.lastLogin",
        LocalDateTime.now()
    ))
    .where(user.id.eq(userId))
    .execute();
```

## Success Criteria

✅ 전체 35개 MySQL JSON 함수 구현 완료
✅ QueryDSL SQL 및 JPA 모듈 모두 지원
✅ 메서드 체이닝 방식의 Fluent API 제공
✅ Testcontainers 기반 통합 테스트 통과
✅ 타입 안정성 및 컴파일 타임 검증
✅ 포괄적인 문서화 및 예제
✅ JitPack을 통한 배포 가능

## References

- [MySQL 8.0 JSON Function Reference](https://dev.mysql.com/doc/refman/8.0/en/json-function-reference.html)
- [OpenFeign QueryDSL (Maintained Fork)](https://github.com/OpenFeign/querydsl)
- [OpenFeign QueryDSL Maven Central](https://central.sonatype.com/artifact/io.github.openfeign.querydsl/querydsl-core)
- [QueryDSL Reference Documentation](http://querydsl.com/static/querydsl/latest/reference/html/)
- [Testcontainers MySQL Module](https://testcontainers.com/modules/mysql/)
