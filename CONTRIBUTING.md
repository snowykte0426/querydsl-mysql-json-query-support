# Contributing to QueryDSL MySQL JSON Query Support

Thank you for your interest in contributing! We welcome all kinds of contributions including code, documentation, bug reports, and feature suggestions.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Code Style & Conventions](#code-style--conventions)
- [Testing](#testing)
- [Contribution Workflow](#contribution-workflow)
- [CI/CD Pipeline](#cicd-pipeline)
- [Documentation](#documentation)
- [Bug Reports](#bug-reports)
- [License](#license)
- [Code of Conduct](#code-of-conduct)

---

## Getting Started

This project provides comprehensive MySQL JSON function support for QueryDSL, enabling type-safe JSON operations in both JPA and SQL environments.

**Ways to contribute:**
- Add new features or enhance existing ones
- Fix bugs
- Improve documentation
- Write tests
- Report issues
- Suggest improvements

---

## Development Setup

### Prerequisites

- **Java**: 17, 21, or 25
- **Gradle**: 9.0.0+
- **Docker**: Required for integration tests (Testcontainers)
- **Git**: Version control

### Clone and Build

```bash
git clone https://github.com/snowykte0426/querydsl-mysql-json-query-support.git
cd querydsl-mysql-json-query-support
./gradlew build
```

### IDE Setup

**Recommended: IntelliJ IDEA**

1. Install the Spotless plugin
2. Enable auto-import cleanup
3. Configure Java SDK (17, 21, or 25)

---

## Project Structure

```
querydsl-mysql-json-query-support/
├── querydsl-mysql-json-core/    # Core module: All 35 MySQL JSON functions
├── querydsl-mysql-json-jpa/     # JPA integration module
└── querydsl-mysql-json-sql/     # SQL integration module
```

### Module Descriptions

- **Core**: Contains all MySQL JSON function implementations (35 functions)
  - Pure QueryDSL expressions
  - No JPA or SQL dependencies
  - All business logic resides here

- **JPA**: JPA/Hibernate integration
  - Delegates to Core module
  - Provides `JPAJsonFunctions` facade
  - Includes `FunctionContributor` for Hibernate 6.4+

- **SQL**: QueryDSL SQL integration
  - Delegates to Core module
  - Provides `SqlJsonFunctions` facade
  - Includes `MySQLJsonTemplates`

---

## Code Style & Conventions

### Code Formatting

We use Spotless to maintain consistent code style:

**Before committing, always run:**
```bash
./gradlew spotlessApply
```

This formats all Java code according to project standards. The CI pipeline automatically checks formatting.

### Naming Conventions

- **Classes**: PascalCase (e.g., `JsonSearchFunctions`)
- **Methods**: camelCase (e.g., `jsonContains`)
- **Constants**: UPPER_SNAKE_CASE
- **MySQL Functions**: Method names should match MySQL function names (e.g., `JSON_CONTAINS` → `jsonContains`)

### JavaDoc Requirements

All public APIs must have JavaDoc comments including:

```java
/**
 * Brief description of what the method does.
 *
 * <p>Additional details, usage notes, or warnings.</p>
 *
 * <p>Example:
 * <pre>{@code
 * jsonContainsString(user.roles, "admin")
 * }</pre>
 *
 * @param paramName description
 * @return return value description
 * @since version when this was added (e.g., 0.1.0-Beta.4)
 */
```

---

## Testing

### Unit Tests

Write unit tests for utility classes and pure functions:

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :querydsl-mysql-json-core:test

# Run specific test class
./gradlew test --tests JsonEscapeUtilsTest
```

**Example**: See `JsonEscapeUtilsTest.java`

### Integration Tests

Integration tests use Testcontainers to run against real MySQL instances:

- Extend `AbstractJPAJsonFunctionTest` (for JPA tests)
- Extend `AbstractSqlJsonFunctionTest` (for SQL tests)
- Test various scenarios: success cases, failure cases, edge cases

**Example**: See `JPAJsonContainsConvenienceTest.java`

### Test Coverage

- Add tests for all new features
- Test edge cases and error conditions
- Ensure tests pass on all supported Java/Gradle/MySQL versions

---

## Contribution Workflow

### 1. Create an Issue (Optional but Recommended)

Before starting work, consider creating an issue to:
- Discuss the proposed change
- Get feedback from maintainers
- Avoid duplicate work

### 2. Fork and Create a Branch

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/querydsl-mysql-json-query-support.git
cd querydsl-mysql-json-query-support

# Create a descriptive branch
git checkout -b your-descriptive-branch-name
```

**Branch Naming:**

Use descriptive branch names that clearly indicate what you're working on. There's no strict convention, but make sure the purpose is clear.

**Good Examples:**
- `add-json-contains-string-method`
- `fix-hibernate-function-registration`
- `update-readme-examples`
- `test-json-escape-utils`
- `refactor-core-expressions`

**Bad Examples:**
- `fix`
- `update`
- `my-branch`
- `temp`

### 3. Write Clear Commit Messages

Write descriptive commit messages that clearly explain what was done and why.

**Good Examples:**

```
Add jsonContainsString convenience method for automatic JSON escaping

Fix production issue where users got "Invalid JSON text" errors
when using plain strings with jsonContains(). The new method
automatically escapes plain strings as JSON literals.
```

```
Fix Hibernate 6.4+ function registration for variadic JSON functions

Resolve CI build failures by using registerBinaryTernaryPattern()
instead of registerFunction() for multi-arity functions like
json_extract and json_search.
```

**Bad Examples:**

```
fix bug
```

```
update code
```

```
changes
```

### 4. Code and Test

```bash
# Format code
./gradlew spotlessApply

# Run tests
./gradlew test

# Full build
./gradlew build
```

### 5. Submit a Pull Request

**PR Checklist:**

- [ ] Code follows Spotless formatting rules
- [ ] All tests pass locally
- [ ] New features have corresponding tests
- [ ] Public APIs have JavaDoc
- [ ] README updated (if applicable)
- [ ] Breaking changes are clearly documented

**PR Description Should Include:**

- Summary of changes
- Related issue number (e.g., "Fixes #123")
- How to test the changes
- Screenshots or examples (if UI changes)

---

## CI/CD Pipeline

### Test Matrix

Our CI pipeline tests against multiple versions to ensure compatibility:

- **Java**: 17, 21, 25
- **Gradle**: 8.5, 8.10.2, 9.2.1
- **MySQL**: 8.0.33, 8.4, 9.2

**Total**: 21 test combinations (excluding incompatible combinations)

**Note**: Java 25 requires Gradle 9.2.1+, so incompatible combinations are excluded.

### CI Checks

All PRs must pass:
- ✅ Build succeeds on all combinations
- ✅ All tests pass
- ✅ Spotless formatting check

---

## Documentation

### README Updates

When adding new features:
- Add usage examples to the relevant README
- Update API reference if needed
- Keep examples concise and clear

### JavaDoc Guidelines

- Use `@param` for all parameters
- Use `@return` to describe return values
- Use `@throws` for checked exceptions
- Use `@since` to mark when API was added
- Include code examples with `<pre>{@code ... }</pre>`
- Link to MySQL documentation when relevant

---

## Bug Reports

When reporting bugs, please include:

**Required Information:**
- Clear description of the problem
- Steps to reproduce
- Expected behavior vs. actual behavior
- Environment details:
  - Java version
  - Gradle version
  - MySQL version
  - QueryDSL version
- Minimal reproducible code example
- Error logs or stack traces

**Template:**

```markdown
## Description
[Clear description of the bug]

## Steps to Reproduce
1. Step 1
2. Step 2
3. Step 3

## Expected Behavior
[What should happen]

## Actual Behavior
[What actually happens]

## Environment
- Java: 17
- Gradle: 9.2.1
- MySQL: 8.0.33
- QueryDSL: 7.1

## Code Sample
```java
// Minimal reproducible example
```

## Error Log
```
[Stack trace or error message]
```
```

---

## License

This project is licensed under the **MIT License**.

By contributing to this project, you agree that your contributions will be licensed under the MIT License.

See the [LICENSE](LICENSE) file for details.

---

## Code of Conduct

We are committed to providing a welcoming and inclusive environment for all contributors.

**Our Standards:**
- Be respectful and considerate
- Provide constructive feedback
- Welcome newcomers
- Focus on what is best for the community
- Show empathy towards other community members

**Unacceptable Behavior:**
- Harassment or discrimination
- Trolling or insulting comments
- Public or private harassment
- Publishing others' private information
- Other conduct which could reasonably be considered inappropriate

---

## Need Help?

If you have questions or need assistance:

- **GitHub Issues**: Ask questions or start discussions
- **Documentation**: Check existing docs (README, JavaDoc)
- **Maintainers**: Reach out to project maintainers

---

Thank you for contributing to QueryDSL MySQL JSON Query Support! 🎉
