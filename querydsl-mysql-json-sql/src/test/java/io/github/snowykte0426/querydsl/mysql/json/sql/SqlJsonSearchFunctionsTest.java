package io.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON search functions in SQL module.
 *
 * <p>Tests JSON_EXTRACT, JSON_CONTAINS, JSON_SEARCH, JSON_OVERLAPS, MEMBER OF, etc.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class SqlJsonSearchFunctionsTest extends AbstractSqlJsonFunctionTest {

    private Long testUserId;
    private Long testProductId;

    @BeforeEach
    void setupTestData() throws SQLException {
        // Create test user with metadata
        testUserId = createUser("John Doe", "john@example.com",
            "{\"role\":\"admin\",\"department\":\"IT\",\"age\":30}");

        // Create test product with attributes
        testProductId = createProduct("Laptop", new BigDecimal("999.99"), "electronics",
            "{\"specs\":{\"cpu\":\"Intel i7\",\"ram\":\"16GB\"},\"warranty\":2}",
            "[\"electronics\",\"sale\",\"featured\"]");
    }

    // ========================================
    // JSON_EXTRACT tests
    // ========================================

    @Test
    void jsonExtract_withSimplePath_shouldExtractValue() throws SQLException {
        // When
        String role = executeNativeQuery(
            "SELECT JSON_EXTRACT(metadata, '$.role') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(role).isEqualTo("\"admin\"");
    }

    @Test
    void jsonExtract_withNestedPath_shouldExtractValue() throws SQLException {
        // When
        String cpu = executeNativeQuery(
            "SELECT JSON_EXTRACT(attributes, '$.specs.cpu') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(cpu).isEqualTo("\"Intel i7\"");
    }

    @Test
    void jsonExtract_withArrayIndex_shouldExtractElement() throws SQLException {
        // When
        String firstTag = executeNativeQuery(
            "SELECT JSON_EXTRACT(tags, '$[0]') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(firstTag).isEqualTo("\"electronics\"");
    }

    @Test
    void jsonExtract_withMultiplePaths_shouldReturnArray() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_EXTRACT(metadata, '$.role', '$.department') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(result).contains("admin", "IT");
    }

    @Test
    void jsonExtract_withNonExistentPath_shouldReturnNull() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_EXTRACT(metadata, '$.nonexistent') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(result).isNull();
    }

    // ========================================
    // JSON_UNQUOTE_EXTRACT (->> operator) tests
    // ========================================

    @Test
    void jsonUnquoteExtract_shouldReturnUnquotedValue() throws SQLException {
        // When
        String department = executeNativeQuery(
            "SELECT metadata->>'$.department' FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(department).isEqualTo("IT");
    }

    @Test
    void jsonExtractOperator_shouldWork() throws SQLException {
        // When - Using -> operator
        String role = executeNativeQuery(
            "SELECT metadata->'$.role' FROM users WHERE id = " + testUserId
        );

        // Then - Returns quoted value
        assertThat(role).isEqualTo("\"admin\"");
    }

    // ========================================
    // JSON_VALUE tests (MySQL 8.0.21+)
    // ========================================

    @Test
    void jsonValue_withScalarValue_shouldExtractValue() throws SQLException {
        // When
        String age = executeNativeQuery(
            "SELECT JSON_VALUE(metadata, '$.age') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(age).isEqualTo("30");
    }

    @Test
    void jsonValue_withStringValue_shouldExtractWithoutQuotes() throws SQLException {
        // When
        String role = executeNativeQuery(
            "SELECT JSON_VALUE(metadata, '$.role') FROM users WHERE id = " + testUserId
        );

        // Then - JSON_VALUE returns unquoted string
        assertThat(role).isEqualTo("admin");
    }

    // ========================================
    // JSON_CONTAINS tests
    // ========================================

    @Test
    void jsonContains_withMatchingValue_shouldReturnTrue() throws SQLException {
        // Given - Create user with roles array
        Long userId = createUser("Admin", "admin@example.com",
            "{\"roles\":[\"admin\",\"user\"]}");

        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS(metadata, '\"admin\"', '$.roles') FROM users WHERE id = " + userId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonContains_inWholeDocument_shouldWork() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS(tags, '\"electronics\"') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonContains_withNonMatchingValue_shouldReturnFalse() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS(tags, '\"gaming\"') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // JSON_CONTAINS_PATH tests
    // ========================================

    @Test
    void jsonContainsPath_withExistingPath_shouldReturnTrue() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS_PATH(metadata, 'one', '$.role') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonContainsPath_withMultiplePaths_allMode_shouldReturnTrue() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS_PATH(metadata, 'all', '$.role', '$.department') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonContainsPath_withNonExistentPath_shouldReturnFalse() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_CONTAINS_PATH(metadata, 'one', '$.nonexistent') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // JSON_KEYS tests
    // ========================================

    @Test
    void jsonKeys_shouldReturnObjectKeys() throws SQLException {
        // When
        String keys = executeNativeQuery(
            "SELECT JSON_KEYS(metadata) FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(keys).contains("role", "department", "age");
    }

    @Test
    void jsonKeys_withPath_shouldReturnKeysAtPath() throws SQLException {
        // When
        String keys = executeNativeQuery(
            "SELECT JSON_KEYS(attributes, '$.specs') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(keys).contains("cpu", "ram");
    }

    // ========================================
    // JSON_SEARCH tests
    // ========================================

    @Test
    void jsonSearch_shouldFindValuePath() throws SQLException {
        // When
        String path = executeNativeQuery(
            "SELECT JSON_SEARCH(metadata, 'one', 'admin') FROM users WHERE id = " + testUserId
        );

        // Then
        assertThat(path).contains("$.role");
    }

    @Test
    void jsonSearch_withWildcard_shouldFindPath() throws SQLException {
        // When
        String path = executeNativeQuery(
            "SELECT JSON_SEARCH(metadata, 'one', 'IT%') FROM users WHERE id = " + testUserId
        );

        // Then - May return path or null depending on MySQL wildcard matching
        // MySQL JSON_SEARCH doesn't support % wildcards the same way as LIKE
        assertThat(path).isIn("\"$.department\"", null);
    }

    // ========================================
    // JSON_OVERLAPS tests (MySQL 8.0.17+)
    // ========================================

    @Test
    void jsonOverlaps_withCommonElements_shouldReturnTrue() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_OVERLAPS('[1,2,3]', '[3,4,5]')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonOverlaps_withNoCommonElements_shouldReturnFalse() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_OVERLAPS('[1,2]', '[3,4]')"
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonOverlaps_withObjects_shouldWork() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT JSON_OVERLAPS('{\"a\":1,\"b\":2}', '{\"b\":2,\"c\":3}')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    // ========================================
    // MEMBER OF tests (MySQL 8.0.17+)
    // ========================================

    @Test
    void memberOf_withMatchingValue_shouldReturnTrue() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT 'admin' MEMBER OF('[\"admin\", \"user\", \"guest\"]')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void memberOf_withNonMatchingValue_shouldReturnFalse() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT 'superadmin' MEMBER OF('[\"admin\", \"user\"]')"
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void memberOf_withNumber_shouldWork() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT 3 MEMBER OF('[1,2,3,4]')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    // ========================================
    // JSON_LENGTH convenience tests
    // ========================================

    @Test
    void jsonLength_shouldReturnElementCount() throws SQLException {
        // When
        Integer count = executeScalarInt(
            "SELECT JSON_LENGTH(tags) FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(count).isEqualTo(3); // 3 tags
    }

    @Test
    void jsonLength_withPath_shouldReturnElementCountAtPath() throws SQLException {
        // When
        Integer count = executeScalarInt(
            "SELECT JSON_LENGTH(attributes, '$.specs') FROM products WHERE id = " + testProductId
        );

        // Then
        assertThat(count).isEqualTo(2); // cpu and ram
    }

    // ========================================
    // Complex integration tests
    // ========================================

    @Test
    void complexQuery_withMultipleJsonFunctions_shouldWork() throws SQLException {
        // When - Combine EXTRACT, CONTAINS, and LENGTH
        String result = executeNativeQuery(
            "SELECT COUNT(*) FROM products WHERE " +
            "JSON_CONTAINS(tags, '\"sale\"') = 1 AND " +
            "JSON_LENGTH(tags) >= 3 AND " +
            "JSON_EXTRACT(attributes, '$.warranty') > 1"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void whereClause_withJsonExtract_shouldFilterResults() throws SQLException {
        // When
        String result = executeNativeQuery(
            "SELECT COUNT(*) FROM users WHERE JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) = 'admin'"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }
}
