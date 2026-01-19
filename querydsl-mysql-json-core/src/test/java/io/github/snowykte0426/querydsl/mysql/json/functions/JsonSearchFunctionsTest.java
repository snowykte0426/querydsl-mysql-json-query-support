package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import io.github.snowykte0426.querydsl.mysql.json.test.TestDataBuilder;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON search functions.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonSearchFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_EXTRACT tests
    // ========================================

    @Test
    void jsonExtract_withSimplePath_shouldExtractValue() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("John Doe")
            .email("john@example.com")
            .metadata("role", "admin")
            .metadata("department", "IT")
            .insert();

        // When
        String role = executeScalar(
            "SELECT JSON_EXTRACT(metadata, '$.role') FROM users WHERE id = " + userId
        );

        // Then
        assertThat(role).isEqualTo("\"admin\"");
    }

    @Test
    void jsonExtract_withNestedPath_shouldExtractValue() throws SQLException {
        // Given
        long productId = TestDataBuilder.products(connection)
            .name("Laptop")
            .price(999.99)
            .attribute("specs", TestDataBuilder.json(
                "cpu", "Intel i7",
                "ram", "16GB"
            ))
            .insert();

        // When
        String cpu = executeScalar(
            "SELECT JSON_EXTRACT(attributes, '$.specs.cpu') FROM products WHERE id = " + productId
        );

        // Then
        assertThat(cpu).isEqualTo("\"Intel i7\"");
    }

    @Test
    void jsonExtract_withArrayIndex_shouldExtractElement() throws SQLException {
        // Given
        long productId = TestDataBuilder.products(connection)
            .name("Product")
            .price(100.0)
            .tags("electronics", "sale", "featured")
            .insert();

        // When
        String firstTag = executeScalar(
            "SELECT JSON_EXTRACT(tags, '$[0]') FROM products WHERE id = " + productId
        );

        // Then
        assertThat(firstTag).isEqualTo("\"electronics\"");
    }

    @Test
    void jsonUnquoteExtract_shouldReturnUnquotedValue() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("Jane")
            .email("jane@example.com")
            .metadata("city", "Seoul")
            .insert();

        // When
        String city = executeScalar(
            "SELECT metadata->>'$.city' FROM users WHERE id = " + userId
        );

        // Then
        assertThat(city).isEqualTo("Seoul");
    }

    // ========================================
    // JSON_VALUE tests (MySQL 8.0.21+)
    // ========================================

    @Test
    void jsonValue_withScalarValue_shouldExtractValue() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("Test")
            .email("test@example.com")
            .metadata("age", 25)
            .insert();

        // When
        String age = executeScalar(
            "SELECT JSON_VALUE(metadata, '$.age') FROM users WHERE id = " + userId
        );

        // Then
        assertThat(age).isEqualTo("25");
    }

    // ========================================
    // JSON_CONTAINS tests
    // ========================================

    @Test
    void jsonContains_withMatchingValue_shouldReturnTrue() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("Admin")
            .email("admin@example.com")
            .settingsJson(TestDataBuilder.json("roles", TestDataBuilder.jsonArray("admin", "user")))
            .insert();

        // When
        String result = executeScalar(
            "SELECT JSON_CONTAINS(settings, '\"admin\"', '$.roles') FROM users WHERE id = " + userId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonContains_inWholeDocument_shouldWork() throws SQLException {
        // Given
        long productId = TestDataBuilder.products(connection)
            .name("Product")
            .price(100.0)
            .tags("electronics", "featured")
            .insert();

        // When
        String result = executeScalar(
            "SELECT JSON_CONTAINS(tags, '\"electronics\"') FROM products WHERE id = " + productId
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    // ========================================
    // JSON_KEYS tests
    // ========================================

    @Test
    void jsonKeys_shouldReturnObjectKeys() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("Test")
            .email("test@example.com")
            .metadata("name", "John")
            .metadata("age", 30)
            .metadata("city", "Seoul")
            .insert();

        // When
        String keys = executeScalar(
            "SELECT JSON_KEYS(metadata) FROM users WHERE id = " + userId
        );

        // Then
        assertThat(keys).contains("name", "age", "city");
    }

    // ========================================
    // JSON_OVERLAPS tests (MySQL 8.0.17+)
    // ========================================

    @Test
    void jsonOverlaps_withCommonElements_shouldReturnTrue() throws SQLException {
        // When
        String result = executeScalar(
            "SELECT JSON_OVERLAPS('[1,2,3]', '[3,4,5]')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonOverlaps_withNoCommonElements_shouldReturnFalse() throws SQLException {
        // When
        String result = executeScalar(
            "SELECT JSON_OVERLAPS('[1,2]', '[3,4]')"
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // MEMBER OF tests (MySQL 8.0.17+)
    // ========================================

    @Test
    void memberOf_withMatchingValue_shouldReturnTrue() throws SQLException {
        // When
        String result = executeScalar(
            "SELECT 'admin' MEMBER OF('[\"admin\", \"user\", \"guest\"]')"
        );

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void memberOf_withNonMatchingValue_shouldReturnFalse() throws SQLException {
        // When
        String result = executeScalar(
            "SELECT 'superadmin' MEMBER OF('[\"admin\", \"user\"]')"
        );

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // Convenience method tests
    // ========================================

    @Test
    void jsonLength_shouldReturnElementCount() throws SQLException {
        // Given
        long productId = TestDataBuilder.products(connection)
            .name("Product")
            .price(100.0)
            .tags("tag1", "tag2", "tag3", "tag4")
            .insert();

        // When
        String length = executeScalar(
            "SELECT JSON_LENGTH(tags) FROM products WHERE id = " + productId
        );

        // Then
        assertThat(length).isEqualTo("4");
    }
}