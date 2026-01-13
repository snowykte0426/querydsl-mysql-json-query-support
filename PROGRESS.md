# QueryDSL MySQL JSON Query Support - Progress Log

## Session 1 - 2026-01-13

### 완료된 작업 ✅

#### 1. 프로젝트 구조 설정
- ✅ 멀티 모듈 Gradle 프로젝트 구조 설정 완료
  - `querydsl-mysql-json-core` (공통 기능)
  - `querydsl-mysql-json-sql` (SQL 모듈)
  - `querydsl-mysql-json-jpa` (JPA 모듈)
- ✅ settings.gradle.kts 설정
- ✅ 각 모듈의 build.gradle.kts 설정
- ✅ root build.gradle.kts 공통 설정

#### 2. OpenFeign QueryDSL 마이그레이션
- ✅ 원본 QueryDSL 대신 OpenFeign 포크 버전으로 결정
- ✅ 모든 의존성을 `io.github.openfeign.querydsl:*:7.1`로 업데이트
  - core: `querydsl-core:7.1`
  - sql: `querydsl-sql:7.1`
  - jpa: `querydsl-jpa:7.1:jakarta`
- ✅ IMPLEMENTATION_PLAN.md 업데이트

#### 3. JsonOperators 구현
- ✅ 35개 MySQL JSON 함수를 enum으로 정의
- ✅ Operator 인터페이스 구현
- ✅ 함수 카테고리별로 정리:
  - Creation Functions (3개)
  - Search Functions (10개)
  - Modification Functions (10개)
  - Attribute Functions (4개)
  - Utility Functions (3개)
  - Schema Validation Functions (2개)
  - Aggregate Functions (2개)
  - Table Functions (1개)

#### 4. 빌드 검증
- ✅ 전체 프로젝트 빌드 성공
- ✅ JsonOperators 컴파일 성공
- ✅ 모든 모듈 JAR 생성 성공

### 현재 상태

**프로젝트 위치**: `E:\Programming\querydsl-mysql-json-query-support`

**빌드 도구**: Gradle 9.2.1
**Java 버전**: Java 25 (타겟 17)
**QueryDSL 버전**: OpenFeign QueryDSL 7.1

**완성된 파일**:
1. `IMPLEMENTATION_PLAN.md` - 전체 구현 계획
2. `querydsl-mysql-json-core/src/main/java/.../JsonOperators.java` - 35개 함수 정의
3. `querydsl-mysql-json-core/src/main/java/.../JsonOperatorTemplates.java` - SQL 템플릿 정의
4. `querydsl-mysql-json-core/src/main/java/.../JsonExpression.java` - 베이스 Expression 클래스
5. `querydsl-mysql-json-core/src/main/java/.../JsonPath.java` - JSON 경로 표현
6. `querydsl-mysql-json-core/src/main/java/.../JsonPathExpression.java` - 경로 Expression
7. `querydsl-mysql-json-core/src/main/java/.../JsonArrayExpression.java` - 배열 연산
8. `querydsl-mysql-json-core/src/main/java/.../JsonObjectExpression.java` - 객체 연산
9. `querydsl-mysql-json-core/src/main/java/.../JsonValueExpression.java` - 스칼라 값 연산

**빌드 상태**: ✅ 성공

### Phase 1.2: Core Operator Registry ✅ 완료
- ✅ Implement `JsonOperatorTemplates.java` with SQL template strings
  - 35개 MySQL JSON 함수에 대한 SQL 템플릿 정의
  - 파라미터 바인딩 템플릿 정의

### Phase 1.3: Base Expression Classes ✅ 완료
- ✅ Implement `JsonExpression` as base class
  - QueryDSL SimpleExpression 확장
  - JSON 공통 연산 메서드 (jsonExtract, jsonDepth, jsonLength 등)
- ✅ Create `JsonPath` and `JsonPathExpression` for path handling
  - MySQL JSON 경로 문법 지원 ($.key, $[0], $[*] 등)
  - Fluent API 지원 (member(), arrayElement(), wildcard() 등)
- ✅ Build `JsonArrayExpression`, `JsonObjectExpression`, `JsonValueExpression`
  - 배열 연산: create, append, insert, memberOf, overlaps
  - 객체 연산: create, keys, contains, mergePatch, mergePreserve
  - 값 연산: extract, quote, unquote, search, set, insert, replace, remove

### 다음 단계 (Phase 1.4)

#### Phase 1.4: Testing Infrastructure
- [ ] Set up Testcontainers with MySQL 8.0.17+
- [ ] Create `AbstractJsonFunctionTest` base test class
- [ ] Configure test database schemas
- [ ] Create `TestDataBuilder` for test data generation

### 기술 스택

- **Build**: Gradle 9.2.1
- **Language**: Java 17/25
- **QueryDSL**: OpenFeign QueryDSL 7.1 (maintained fork)
- **Database**: MySQL 8.0.17+
- **Testing**: JUnit 5, Testcontainers, AssertJ
- **JSON Processing**: Jackson 2.16.1

### 중요 링크

- [OpenFeign QueryDSL GitHub](https://github.com/OpenFeign/querydsl)
- [MySQL 8.0 JSON Functions](https://dev.mysql.com/doc/refman/8.0/en/json-function-reference.html)
- [프로젝트 계획서](./IMPLEMENTATION_PLAN.md)

### 참고 사항

1. **QueryDSL 버전**: 원본 QueryDSL이 운영 종료되어 OpenFeign 포크 버전 사용
2. **Operator 패턴**: enum으로 구현하여 Operator 인터페이스 준수
3. **모듈 구조**: core-sql-jpa 3단계 구조로 의존성 분리
4. **빌드 명령어**:
   ```bash
   export JAVA_HOME="/e/Programming/SDK/JDK"
   export PATH="$JAVA_HOME/bin:$PATH"
   ./gradlew build
   ```

### 다음 세션 시작 방법

1. 프로젝트 디렉토리로 이동
2. JAVA_HOME 설정 (위 명령어)
3. `./gradlew projects` 로 모듈 확인
4. IMPLEMENTATION_PLAN.md의 Phase 1.2부터 진행
5. 이 PROGRESS.md 파일 업데이트

---
**마지막 업데이트**: 2026-01-13
**완료된 Phase**: Phase 1.1, 1.2, 1.3 완료
**다음 작업**: Phase 1.4 - Testing Infrastructure 또는 Phase 2 - Creation and Search Functions 구현
