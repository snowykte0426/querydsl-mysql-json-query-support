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

**Main Code:**
1. `IMPLEMENTATION_PLAN.md` - 전체 구현 계획
2. `querydsl-mysql-json-core/src/main/java/.../JsonOperators.java` - 35개 함수 정의
3. `querydsl-mysql-json-core/src/main/java/.../JsonOperatorTemplates.java` - SQL 템플릿 정의
4. `querydsl-mysql-json-core/src/main/java/.../JsonExpression.java` - 베이스 Expression 클래스
5. `querydsl-mysql-json-core/src/main/java/.../JsonPath.java` - JSON 경로 표현
6. `querydsl-mysql-json-core/src/main/java/.../JsonPathExpression.java` - 경로 Expression
7. `querydsl-mysql-json-core/src/main/java/.../JsonArrayExpression.java` - 배열 연산
8. `querydsl-mysql-json-core/src/main/java/.../JsonObjectExpression.java` - 객체 연산
9. `querydsl-mysql-json-core/src/main/java/.../JsonValueExpression.java` - 스칼라 값 연산

**Test Infrastructure:**
10. `querydsl-mysql-json-core/src/test/java/.../AbstractJsonFunctionTest.java` - 테스트 베이스 클래스
11. `querydsl-mysql-json-core/src/test/java/.../TestDataBuilder.java` - 테스트 데이터 빌더
12. `querydsl-mysql-json-core/src/test/java/.../TestInfrastructureTest.java` - 인프라 검증 테스트

**Phase 2 Functions:**
13. `querydsl-mysql-json-core/src/main/java/.../JsonCreationFunctions.java` - Creation 함수 팩토리
14. `querydsl-mysql-json-core/src/main/java/.../JsonSearchFunctions.java` - Search 함수 팩토리
15. `querydsl-mysql-json-core/src/test/java/.../JsonCreationFunctionsTest.java` - Creation 함수 테스트
16. `querydsl-mysql-json-core/src/test/java/.../JsonSearchFunctionsTest.java` - Search 함수 테스트

**Phase 3 Functions:**
17. `querydsl-mysql-json-core/src/main/java/.../JsonModifyFunctions.java` - Modify 함수 팩토리 (9개 함수)
18. `querydsl-mysql-json-core/src/main/java/.../JsonAttributeFunctions.java` - Attribute 함수 팩토리 (4개 함수 + 6개 편의 메서드)

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

### Phase 1.4: Testing Infrastructure ✅ 완료
- ✅ Set up Testcontainers with MySQL 8.0.17+
  - MySQL 8.0.33 컨테이너 설정
  - Testcontainers BOM 1.19.3 의존성 추가
- ✅ Create `AbstractJsonFunctionTest` base test class
  - 자동 컨테이너 시작/종료
  - DB 연결 관리
  - 테스트 간 데이터 정리
- ✅ Configure test database schemas
  - users 테이블 (JSON: metadata, settings)
  - products 테이블 (JSON: attributes, tags)
  - orders 테이블 (JSON: order_data, shipping_info)
- ✅ Create `TestDataBuilder` for test data generation
  - Fluent API로 테스트 데이터 생성
  - 자동 JSON 직렬화
  - UserBuilder, ProductBuilder, OrderBuilder

### Phase 2: Creation and Search Functions ✅ 완료

#### Step 2.1: Creation Functions (3 functions) ✅
- ✅ JsonCreationFunctions 팩토리 클래스
  - jsonArray() - 배열 생성, varargs 지원
  - jsonObject() - 객체 생성, builder 패턴 지원
  - jsonQuote() - 문자열 인용
  - 편의 메서드: jsonArrayFrom(), jsonObjectFrom(), jsonNull()

#### Step 2.2: Basic Search Functions (6 functions) ✅
- ✅ JsonSearchFunctions 팩토리 클래스
  - jsonExtract() - 경로로 데이터 추출
  - jsonValue() - 스칼라 값 추출 (MySQL 8.0.21+)
  - jsonUnquoteExtract() - ->> 연산자
  - jsonContains() - 값 포함 여부 확인
  - jsonContainsPath() - 경로 존재 여부 확인

#### Step 2.3: Advanced Search Functions (4 functions) ✅
- ✅ 고급 검색 함수
  - jsonKeys() - 객체 키 추출
  - jsonSearch() - 값으로 경로 찾기
  - jsonOverlaps() - 문서 겹침 확인 (MySQL 8.0.17+)
  - memberOf() - 배열 멤버십 확인 (MySQL 8.0.17+)
  - 편의 메서드: jsonLength(), jsonIsEmpty()

#### 테스트 완료
- ✅ JsonCreationFunctionsTest - 15개 테스트 케이스
- ✅ JsonSearchFunctionsTest - 18개 테스트 케이스

### Phase 3: Modification and Attribute Functions ✅ 완료 (Implementation)

#### Step 3.1: Modification Functions (9 functions) ✅
- ✅ JsonModifyFunctions 팩토리 클래스
  - jsonSet() - 값 삽입 또는 업데이트 (단일/복수 경로)
  - jsonInsert() - 값 삽입 (교체 안 함)
  - jsonReplace() - 기존 값만 교체
  - jsonRemove() - 경로에서 데이터 제거
  - jsonArrayAppend() - 배열 끝에 추가
  - jsonArrayInsert() - 배열 특정 위치에 삽입
  - jsonMergePatch() - RFC 7386 병합
  - jsonMergePreserve() - 중복 키 보존 병합
  - jsonUnquote() - JSON 문자열 언퀴팅

#### Step 3.2: Attribute Functions (4 functions) ✅
- ✅ JsonAttributeFunctions 팩토리 클래스
  - jsonDepth() - 최대 중첩 깊이
  - jsonLength() - 요소 개수 (경로 옵션)
  - jsonType() - 타입 문자열 반환
  - jsonValid() - JSON 유효성 검증
  - 편의 메서드: isEmpty(), isNotEmpty(), isArray(), isObject(), isScalar(), isNull()

#### 테스트 상태
- [ ] JsonModifyFunctionsTest - 미작성
- [ ] JsonAttributeFunctionsTest - 미작성

### 다음 단계 (Phase 4)

#### Phase 4: Utility, Schema, and Aggregate Functions
- [ ] Implement JSON_PRETTY, JSON_STORAGE_SIZE, JSON_STORAGE_FREE
- [ ] Implement JSON_SCHEMA_VALID, JSON_SCHEMA_VALIDATION_REPORT
- [ ] Implement JSON_ARRAYAGG, JSON_OBJECTAGG
- [ ] Implement JSON_TABLE

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
**완료된 Phase**: Phase 1, 2, 3 (implementation) 완료 ✅
**구현된 함수**: 27개 / 35개 (77%)
  - Creation: 3개 ✅
  - Search: 10개 ✅
  - Modification: 9개 ✅
  - Attribute: 4개 ✅
  - Remaining: Utility (3개), Schema (2개), Aggregate (2개), Table (1개)
**다음 작업**: Phase 4 - Utility, Schema, and Aggregate Functions 구현
