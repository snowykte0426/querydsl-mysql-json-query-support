# Architecture Documentation

This document explains the architecture, design decisions, and internal structure of QueryDSL MySQL JSON Query Support.

## Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Core Module Architecture](#core-module-architecture)
- [JPA Module Architecture](#jpa-module-architecture)
- [SQL Module Architecture](#sql-module-architecture)
- [Expression Type Hierarchy](#expression-type-hierarchy)
- [Operator Registration](#operator-registration)
- [Delegation Pattern](#delegation-pattern)
- [Design Decisions](#design-decisions)
- [Extension Points](#extension-points)

---

## Overview

The library follows a **multi-module architecture** with a strict delegation pattern:

```
┌─────────────────────────────────────────────┐
│         Application Code                     │
└─────────────┬───────────────────────────────┘
              │
      ┌───────┴────────┐
      │                │
┌─────▼──────┐  ┌─────▼──────┐
│ JPA Module │  │ SQL Module │
│            │  │            │
│  JPAJson   │  │  SqlJson   │
│ Functions  │  │ Functions  │
└─────┬──────┘  └─────┬──────┘
      │                │
      │   100% Delegation
      │                │
      └───────┬────────┘
              │
      ┌───────▼────────┐
      │  Core Module   │
      │                │
      │  - Operators   │
      │  - Functions   │
      │  - Expressions │
      └────────────────┘
              │
      ┌───────▼────────┐
      │ QueryDSL Core  │
      └────────────────┘
```

**Key Principles:**
1. **Zero Business Logic Duplication**: All JSON logic lives in the core module
2. **100% Delegation**: JPA and SQL modules delegate everything to core
3. **Type Safety**: Compile-time checking throughout the stack
4. **Minimal Abstraction**: Thin wrappers that preserve QueryDSL idioms

---

## Project Structure

```
querydsl-mysql-json-query-support/
├── querydsl-mysql-json-core/          # Core module (foundation)
│   ├── src/main/java/
│   │   └── com/github/snowykte0426/querydsl/mysql/json/core/
│   │       ├── expressions/           # Custom expression types
│   │       │   ├── JsonArrayExpression.java
│   │       │   ├── JsonObjectExpression.java
│   │       │   ├── JsonValueExpression.java
│   │       │   └── JsonTableExpression.java
│   │       ├── functions/             # Function implementations
│   │       │   ├── JsonCreationFunctions.java       # 3 functions
│   │       │   ├── JsonSearchFunctions.java         # 10 functions
│   │       │   ├── JsonModifyFunctions.java         # 9 functions
│   │       │   ├── JsonAttributeFunctions.java      # 4 functions
│   │       │   ├── JsonUtilityFunctions.java        # 3 functions
│   │       │   ├── JsonSchemaFunctions.java         # 2 functions
│   │       │   ├── JsonAggregateFunctions.java      # 2 functions
│   │       │   └── JsonTableFunctions.java          # 1 function + helpers
│   │       ├── operators/             # Operator definitions
│   │       │   └── JsonOperators.java # All 35 operators
│   │       └── types/                 # Type definitions
│   │           ├── JsonExpression.java
│   │           ├── JsonTableColumn.java
│   │           └── JsonOperatorTemplates.java
│   └── src/test/java/                 # 178 unit tests
│
├── querydsl-mysql-json-jpa/           # JPA integration layer
│   ├── src/main/java/
│   │   └── com/github/snowykte0426/querydsl/mysql/json/jpa/
│   │       ├── JPAJsonFunctions.java          # Unified API (delegates to core)
│   │       ├── expressions/
│   │       │   └── JPAJsonExpression.java     # Fluent wrapper
│   │       └── spring/
│   │           └── JsonFunctionRepositorySupport.java
│   └── src/test/java/                 # 178 integration tests
│
├── querydsl-mysql-json-sql/           # SQL integration layer
│   ├── src/main/java/
│   │   └── com/github/snowykte0426/querydsl/mysql/json/sql/
│   │       ├── MySQLJsonTemplates.java        # Template registration
│   │       ├── SqlJsonFunctions.java          # Unified API (delegates to core)
│   │       └── expressions/
│   │           └── SqlJsonExpression.java     # Fluent wrapper
│   └── src/test/java/                 # 106 integration tests
│
├── ARCHITECTURE.md                    # This file
├── INTEGRATION_GUIDE.md               # Framework integration guide
├── IMPLEMENTATION_PLAN.md             # Complete function reference
└── PROGRESS.md                        # Development history
```

---

## Core Module Architecture

The core module is the **foundation** that contains all JSON-related business logic.

### Operator Definitions

All 35 MySQL JSON operators are defined in `JsonOperators.java`:

```java
public final class JsonOperators {
    // Creation operators
    public static final Operator JSON_ARRAY = Operator.create("JSON_ARRAY", Object[].class);
    public static final Operator JSON_OBJECT = Operator.create("JSON_OBJECT", Object[].class);
    public static final Operator JSON_QUOTE = Operator.create("JSON_QUOTE", String.class);

    // Search operators
    public static final Operator JSON_EXTRACT = Operator.create("JSON_EXTRACT", Object.class, String.class);
    public static final Operator JSON_CONTAINS = Operator.create("JSON_CONTAINS", Object.class, String.class);
    // ... 30 more operators
}
```

### Template Mapping

`JsonOperatorTemplates.java` maps operators to MySQL syntax:

```java
public final class JsonOperatorTemplates {
    public static Map<Operator, String> getTemplates() {
        Map<Operator, String> templates = new HashMap<>();

        // Creation functions
        templates.put(JSON_ARRAY, "json_array({0})");
        templates.put(JSON_OBJECT, "json_object({0})");
        templates.put(JSON_QUOTE, "json_quote({0})");

        // Search functions
        templates.put(JSON_EXTRACT, "json_extract({0}, {1})");
        templates.put(JSON_CONTAINS, "json_contains({0}, {1})");
        templates.put(JSON_CONTAINS_PATH, "json_contains_path({0}, {1}, {2})");

        // ... all 35 mappings
        return templates;
    }
}
```

### Function Implementations

Each function category has its own class:

```java
// JsonSearchFunctions.java
public final class JsonSearchFunctions {

    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        return new JsonExpression<>(
            Expressions.operation(
                String.class,
                JsonOperators.JSON_EXTRACT,
                jsonDoc,
                Expressions.constant(path)
            )
        );
    }

    public static BooleanExpression jsonContains(Expression<?> jsonDoc, String value) {
        return Expressions.booleanOperation(
            JsonOperators.JSON_CONTAINS,
            jsonDoc,
            Expressions.constant(value)
        );
    }

    // ... 8 more search functions
}
```

**Key Design:**
- Pure static methods (no state)
- Return QueryDSL expression types
- Use core `Expressions` factory methods
- Delegate to custom operators

### Expression Types

Custom expression wrappers for JSON-specific behavior:

```java
public class JsonArrayExpression extends SimpleExpression<String> {
    private final Expression<String> mixin;

    protected JsonArrayExpression(Expression<String> mixin) {
        super(mixin);
        this.mixin = mixin;
    }

    public static JsonArrayExpression wrap(Expression<String> expr) {
        return new JsonArrayExpression(expr);
    }

    // Fluent methods for array operations
    public JsonArrayExpression append(Object value) {
        return wrap(JsonModifyFunctions.jsonArrayAppend(this, "$", value));
    }
}
```

---

## JPA Module Architecture

The JPA module provides **JPA-specific integration** with zero business logic.

### Layer Responsibilities

```
┌────────────────────────────────────────┐
│  Application Code                       │
│  (Spring Services, Controllers)         │
└──────────────┬─────────────────────────┘
               │
┌──────────────▼─────────────────────────┐
│  JPAJsonFunctions (Static API)          │
│  - Single entry point for all 35 funcs  │
│  - 100% delegates to core               │
└──────────────┬─────────────────────────┘
               │
┌──────────────▼─────────────────────────┐
│  JPAJsonExpression (Fluent API)         │
│  - Wraps entity paths                   │
│  - Provides method chaining             │
│  - Delegates to core functions          │
└──────────────┬─────────────────────────┘
               │
┌──────────────▼─────────────────────────┐
│  JsonFunctionRepositorySupport          │
│  - Spring Data JPA integration          │
│  - Convenience methods                  │
│  - Extends QuerydslRepositorySupport    │
└──────────────┬─────────────────────────┘
               │
       ┌───────┴────────┐
       │                │
┌──────▼──────┐  ┌─────▼──────┐
│ Core Module │  │ QueryDSL   │
│  Functions  │  │ JPA Core   │
└─────────────┘  └────────────┘
```

### JPAJsonFunctions Implementation

```java
public final class JPAJsonFunctions {

    // Direct delegation - no logic
    public static JsonArrayExpression jsonArray(Object... values) {
        return JsonCreationFunctions.jsonArray(values);
    }

    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }

    // All 35 functions follow this pattern
}
```

**Key Design:**
- Pure delegation methods (no business logic)
- Same method signatures as core
- JPA-specific imports for convenience
- Static utility class (private constructor)

### JPAJsonExpression Wrapper

```java
public class JPAJsonExpression extends SimpleExpression<String> {

    private final Expression<?> jsonDoc;

    public static JPAJsonExpression of(Expression<?> expression) {
        return new JPAJsonExpression(expression);
    }

    // Fluent methods delegate to core
    public JsonExpression<String> extract(String path) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }

    public BooleanExpression contains(String value) {
        return JsonSearchFunctions.jsonContains(jsonDoc, value);
    }

    public NumberExpression<Integer> depth() {
        return JsonAttributeFunctions.jsonDepth(jsonDoc);
    }
}
```

**Key Design:**
- Wraps entity paths for method chaining
- Each method delegates to core functions
- Maintains fluent API style
- No business logic

### Spring Data Integration

```java
public abstract class JsonFunctionRepositorySupport
        extends QuerydslRepositorySupport {

    protected JPAJsonExpression jsonExpression(Expression<?> expression) {
        return JPAJsonExpression.of(expression);
    }

    protected BooleanExpression jsonContains(Expression<?> jsonDoc, String value) {
        return JPAJsonFunctions.jsonContains(jsonDoc, value);
    }

    // Convenience methods for common operations
}
```

---

## SQL Module Architecture

The SQL module provides **SQL-specific integration** for direct queries.

### MySQLJsonTemplates

The core component that registers JSON operators:

```java
public class MySQLJsonTemplates extends MySQLTemplates {

    public static final MySQLJsonTemplates DEFAULT = new MySQLJsonTemplates();

    protected MySQLJsonTemplates() {
        super();
        registerJsonOperators();
    }

    private void registerJsonOperators() {
        // Get all operator-template mappings from core
        Map<Operator, String> jsonTemplates = JsonOperatorTemplates.getTemplates();

        // Register each with QueryDSL SQL
        jsonTemplates.forEach((operator, template) -> {
            add(operator, template, -1);
        });
    }
}
```

**How It Works:**
1. Extends `MySQLTemplates` from QueryDSL SQL
2. Calls `registerJsonOperators()` in constructor
3. Retrieves all 35 operator templates from core
4. Registers each with QueryDSL's template system
5. QueryDSL SQL now knows how to generate JSON function SQL

### SqlJsonFunctions

```java
public final class SqlJsonFunctions {

    // Direct delegation to core
    public static JsonArrayExpression jsonArray(Object... values) {
        return JsonCreationFunctions.jsonArray(values);
    }

    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }

    // All 35 functions...
}
```

Identical pattern to JPA module - pure delegation.

### SqlJsonExpression

```java
public class SqlJsonExpression extends SimpleExpression<String> {

    private final Expression<?> jsonDoc;

    public static SqlJsonExpression of(Expression<?> expression) {
        return new SqlJsonExpression(expression);
    }

    // Fluent API - delegates to core
    public JsonExpression<String> extract(String path) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }
}
```

Same wrapper pattern as JPA module.

---

## Expression Type Hierarchy

```
QueryDSL Core Types
       │
       ├─ Expression<T>
       │    │
       │    ├─ SimpleExpression<T>
       │    │    │
       │    │    ├─ StringExpression
       │    │    ├─ NumberExpression<N>
       │    │    └─ BooleanExpression
       │    │
       │    └─ ComparableExpression<T>
       │
       └─ Operation<T>

Custom JSON Types (in core module)
       │
       ├─ JsonExpression<T> extends SimpleExpression<T>
       │    └─ Used for generic JSON returns
       │
       ├─ JsonArrayExpression extends SimpleExpression<String>
       │    └─ For JSON arrays with array-specific methods
       │
       ├─ JsonObjectExpression extends SimpleExpression<String>
       │    └─ For JSON objects with object-specific methods
       │
       ├─ JsonValueExpression extends SimpleExpression<String>
       │    └─ For scalar JSON values
       │
       └─ JsonTableExpression extends Expression<Object>
            └─ For JSON_TABLE with builder pattern

Wrapper Types (in JPA/SQL modules)
       │
       ├─ JPAJsonExpression extends SimpleExpression<String>
       │    └─ Wraps entity paths for fluent API
       │
       └─ SqlJsonExpression extends SimpleExpression<String>
            └─ Wraps column paths for fluent API
```

**Type Relationships:**
- All extend QueryDSL's base types
- Custom types add JSON-specific methods
- Wrappers provide fluent API convenience
- Type safety preserved throughout chain

---

## Operator Registration

### Registration Flow

```
1. Application Startup
   │
   ├─ JPA Module:
   │   └─ Uses standard QueryDSL JPA
   │       └─ Operators automatically handled by QueryDSL
   │
   └─ SQL Module:
       └─ Creates SQLQueryFactory with MySQLJsonTemplates
           │
           ├─ MySQLJsonTemplates constructor called
           │   │
           │   └─ registerJsonOperators() executes
           │       │
           │       ├─ Calls JsonOperatorTemplates.getTemplates()
           │       │   └─ Returns Map<Operator, String>
           │       │
           │       └─ For each (operator, template):
           │           └─ Calls add(operator, template, -1)
           │               └─ Registers with QueryDSL SQL
           │
           └─ All 35 JSON operators now available
```

### Template Format

Templates use `{0}`, `{1}`, etc. as placeholders:

```java
// Simple function
"json_array({0})"              // JSON_ARRAY(value)

// Function with multiple args
"json_extract({0}, {1})"       // JSON_EXTRACT(doc, path)

// Operator syntax
"{0} MEMBER OF({1})"           // value MEMBER OF(array)

// Variadic function
"json_object({0})"             // JSON_OBJECT(k1, v1, k2, v2, ...)
```

---

## Delegation Pattern

### Why 100% Delegation?

**Problem:** Duplication leads to:
- Inconsistent behavior between modules
- Double maintenance burden
- Increased bug surface area
- Version skew issues

**Solution:** Single source of truth in core module

### Delegation Example

```java
// WRONG: Business logic in integration module
public class JPAJsonFunctions {
    public static JsonExpression<String> jsonExtract(
            Expression<?> jsonDoc, String path) {
        // ❌ Logic here creates duplication
        return new JsonExpression<>(
            Expressions.operation(
                String.class,
                JsonOperators.JSON_EXTRACT,
                jsonDoc,
                Expressions.constant(path)
            )
        );
    }
}

// CORRECT: Pure delegation
public class JPAJsonFunctions {
    public static JsonExpression<String> jsonExtract(
            Expression<?> jsonDoc, String path) {
        // ✅ Just delegate to core
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }
}
```

### Delegation Benefits

1. **Single Source of Truth**: All logic in core module
2. **Consistency**: JPA and SQL behave identically
3. **Easy Maintenance**: Fix once, affects all modules
4. **Clear Boundaries**: Integration vs. business logic
5. **Testability**: Test core once, integration tests verify delegation

---

## Design Decisions

### 1. Multi-Module Structure

**Decision:** Split into core, JPA, and SQL modules

**Rationale:**
- Users only depend on what they need
- Clear separation of concerns
- Easier to maintain and test
- Allows independent versioning

**Trade-off:** More complex project structure

### 2. Static Function API

**Decision:** Provide static entry point (`JPAJsonFunctions`, `SqlJsonFunctions`)

**Rationale:**
- Matches QueryDSL idioms (`Expressions`, `Projections`)
- Easy to discover with IDE autocomplete
- No instance state to manage
- Clear API surface

**Trade-off:** Not object-oriented, can't be mocked easily

### 3. Fluent Expression Wrappers

**Decision:** Provide optional fluent API (`JPAJsonExpression`, `SqlJsonExpression`)

**Rationale:**
- Method chaining feels natural for JSON operations
- Reduces nested function calls
- Better readability for complex queries
- Users can choose style (static vs. fluent)

**Trade-off:** Two ways to do the same thing

### 4. 100% Delegation Pattern

**Decision:** Integration modules delegate everything to core

**Rationale:**
- Single source of truth
- Consistency across modules
- Easier maintenance
- Testability

**Trade-off:** Extra function call overhead (negligible)

### 5. Custom Expression Types

**Decision:** Create `JsonArrayExpression`, `JsonObjectExpression`, etc.

**Rationale:**
- Type-specific methods (e.g., `append()` for arrays)
- Better type safety
- Clearer API
- Matches MySQL JSON type system

**Trade-off:** More classes to maintain

### 6. Template-Based Operator Registration

**Decision:** Use QueryDSL's template system for SQL module

**Rationale:**
- Leverages existing QueryDSL infrastructure
- Clean separation of operator definition and SQL generation
- Easy to customize templates if needed
- Consistent with QueryDSL architecture

**Trade-off:** Requires understanding QueryDSL templates

### 7. String-Based JSON in Entities

**Decision:** Store JSON as `String` in JPA entities

**Rationale:**
- Maximum compatibility across JPA providers
- MySQL JSON type maps to VARCHAR in JDBC
- Simple to work with
- Explicit control over JSON content

**Trade-off:** No compile-time validation of JSON structure

---

## Extension Points

### Adding New Functions

To add a new MySQL JSON function:

1. **Define operator** in `JsonOperators.java`:
```java
public static final Operator MY_NEW_FUNC = Operator.create("MY_NEW_FUNC", Object.class);
```

2. **Add template** in `JsonOperatorTemplates.java`:
```java
templates.put(MY_NEW_FUNC, "my_new_func({0}, {1})");
```

3. **Implement function** in appropriate category class:
```java
public static StringExpression myNewFunc(Expression<?> arg1, String arg2) {
    return Expressions.stringTemplate(
        MY_NEW_FUNC,
        arg1,
        Expressions.constant(arg2)
    );
}
```

4. **Expose in integration modules**:
```java
// JPAJsonFunctions.java
public static StringExpression myNewFunc(Expression<?> arg1, String arg2) {
    return JsonCategoryFunctions.myNewFunc(arg1, arg2);
}
```

5. **Add tests** for core, JPA, and SQL modules

### Custom Expression Types

To add a custom expression type:

```java
public class MyJsonExpression extends SimpleExpression<String> {

    private final Expression<String> mixin;

    public MyJsonExpression(Expression<String> mixin) {
        super(mixin);
        this.mixin = mixin;
    }

    public static MyJsonExpression wrap(Expression<String> expr) {
        return new MyJsonExpression(expr);
    }

    // Custom methods
    public MyJsonExpression customOperation() {
        // Implementation
        return this;
    }
}
```

### Custom Templates

To customize SQL generation:

```java
public class CustomMySQLJsonTemplates extends MySQLJsonTemplates {

    protected CustomMySQLJsonTemplates() {
        super();
        customizeTemplates();
    }

    private void customizeTemplates() {
        // Override specific templates
        add(JsonOperators.JSON_EXTRACT, "custom_json_extract({0}, {1})", -1);
    }
}
```

---

## Performance Considerations

### Query Execution

- **Core Module**: Zero runtime overhead (compile-time composition)
- **Delegation**: Single extra method call (JIT-optimized to zero)
- **Expression Creation**: Minimal object allocation
- **Template Resolution**: Happens once at QueryFactory creation

### Optimization Tips

1. **Reuse QueryFactory instances**:
```java
// Good: Singleton
private static final JPAQueryFactory queryFactory = ...;

// Bad: Create per query
public List<User> find() {
    JPAQueryFactory qf = new JPAQueryFactory(em);  // ❌ Wasteful
}
```

2. **Use indexes on JSON paths**:
```sql
CREATE INDEX idx_role ON users((CAST(JSON_EXTRACT(metadata, '$.role') AS CHAR(50))));
```

3. **Prefer static API for simple queries**:
```java
// Faster (no wrapper object)
JPAJsonFunctions.jsonExtract(user.metadata, "$.role")

// Slightly slower (creates wrapper)
JPAJsonExpression.of(user.metadata).extract("$.role")
```

---

## Testing Architecture

### Test Structure

```
Each module has its own test suite:

Core Module (178 tests):
- Unit tests for each function
- No database required
- Fast execution

JPA Module (178 tests):
- Integration tests with Testcontainers
- Tests delegation to core
- Tests JPA-specific features

SQL Module (106 tests):
- Integration tests with Testcontainers
- Tests template registration
- Tests SQL-specific features
```

### Test Pattern

```java
@Testcontainers
public abstract class AbstractJsonFunctionTest {

    @Container
    protected static final MySQLContainer<?> mysql = ...;

    @BeforeAll
    static void setup() {
        // Infrastructure setup
    }

    @Test
    void testJsonFunction() {
        // Arrange
        // Act
        // Assert
    }
}
```

---

## Related Documentation

- [Main README](./README.md) - Project overview
- [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) - Framework integration
- [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - Function reference
- [PROGRESS.md](./PROGRESS.md) - Development history

---

**Questions?** Open an issue on GitHub or check the module-specific READMEs.