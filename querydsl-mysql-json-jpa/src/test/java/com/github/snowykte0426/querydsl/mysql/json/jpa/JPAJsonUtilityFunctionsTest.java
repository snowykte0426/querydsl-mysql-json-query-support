package com.github.snowykte0426.querydsl.mysql.json.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON utility functions in JPA environment.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>JSON_PRETTY - Format JSON for readability</li>
 *   <li>JSON_STORAGE_SIZE - Storage space used</li>
 *   <li>JSON_STORAGE_FREE - Freed space after update</li>
 * </ul>
 */
@DisplayName("JPA JSON Utility Functions")
class JPAJsonUtilityFunctionsTest extends AbstractJPAJsonFunctionTest {

    @Nested
    @DisplayName("JSON_PRETTY")
    class JsonPrettyTests {

        @Test
        @DisplayName("should format simple object")
        void formatSimpleObject() {
            String sql = "SELECT JSON_PRETTY('{\"a\":1,\"b\":2}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"");
            assertThat(result).contains("\"b\"");
            assertThat(result).contains("\n");
        }

        @Test
        @DisplayName("should format nested object")
        void formatNestedObject() {
            String sql = "SELECT JSON_PRETTY('{\"user\":{\"name\":\"John\",\"age\":30}}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"user\"");
            assertThat(result).contains("\"name\"");
            assertThat(result).contains("\n");
        }

        @Test
        @DisplayName("should format array")
        void formatArray() {
            String sql = "SELECT JSON_PRETTY('[1,2,3]')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("[");
            assertThat(result).contains("]");
        }

        @Test
        @DisplayName("should handle null")
        void handleNull() {
            String sql = "SELECT JSON_PRETTY(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should preserve data")
        void preserveData() {
            String sql = "SELECT JSON_PRETTY('{\"key\":\"value\"}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"key\"");
            assertThat(result).contains("\"value\"");
        }
    }

    @Nested
    @DisplayName("JSON_STORAGE_SIZE")
    class JsonStorageSizeTests {

        @Test
        @DisplayName("should return size for simple object")
        void returnSizeForSimpleObject() {
            String sql = "SELECT JSON_STORAGE_SIZE('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return size for array")
        void returnSizeForArray() {
            String sql = "SELECT JSON_STORAGE_SIZE('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return larger size for bigger document")
        void returnLargerSizeForBiggerDocument() {
            String sql1 = "SELECT JSON_STORAGE_SIZE('{\"a\": 1}')";
            String sql2 = "SELECT JSON_STORAGE_SIZE('{\"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4, \"e\": 5}')";

            int size1 = ((Number) executeScalar(sql1)).intValue();
            int size2 = ((Number) executeScalar(sql2)).intValue();

            assertThat(size2).isGreaterThan(size1);
        }

        @Test
        @DisplayName("should return null for null input")
        void returnNullForNullInput() {
            String sql = "SELECT JSON_STORAGE_SIZE(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return size for empty object")
        void returnSizeForEmptyObject() {
            String sql = "SELECT JSON_STORAGE_SIZE('{}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("JSON_STORAGE_FREE")
    class JsonStorageFreeTests {

        @Test
        @DisplayName("should return 0 for literal")
        void return0ForLiteral() {
            // JSON_STORAGE_FREE returns 0 for literals (not column values)
            String sql = "SELECT JSON_STORAGE_FREE('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should work with column value")
        void workWithColumnValue() {
            // Create a user with metadata
            createUser("Test", "test@example.com", "{\"data\": \"value\"}");

            // JSON_STORAGE_FREE on column should return 0 for non-updated values
            String sql = "SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE email = 'test@example.com'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Combined Utility Tests")
    class CombinedUtilityTests {

        @Test
        @DisplayName("should compare storage sizes")
        void compareStorageSizes() {
            String small = "'{}'";
            String large = "'{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5}'";

            String sql = String.format(
                "SELECT JSON_STORAGE_SIZE(%s) < JSON_STORAGE_SIZE(%s)",
                small, large
            );
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should pretty print and check validity")
        void prettyPrintAndCheckValidity() {
            // Pretty printed JSON should still be valid
            String sql = "SELECT JSON_VALID(JSON_PRETTY('{\"a\":1}'))";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }
}
