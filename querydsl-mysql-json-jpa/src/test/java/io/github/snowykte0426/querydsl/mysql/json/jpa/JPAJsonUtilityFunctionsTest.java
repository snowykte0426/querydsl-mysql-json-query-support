package io.github.snowykte0426.querydsl.mysql.json.jpa;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON utility functions in JPA environment.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>JSON_PRETTY - Format JSON for readability</li>
 * <li>JSON_STORAGE_SIZE - Storage space used</li>
 * <li>JSON_STORAGE_FREE - Freed space after update</li>
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
            @NotNull String sql = "SELECT JSON_PRETTY('{\"a\":1,\"b\":2}')";
            @Nullable String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"");
            assertThat(result).contains("\"b\"");
            assertThat(result).contains("\n");
        }

        @Test
        @DisplayName("should format nested object")
        void formatNestedObject() {
            @NotNull String sql = "SELECT JSON_PRETTY('{\"user\":{\"name\":\"John\",\"age\":30}}')";
            @Nullable String result = executeNativeQuery(sql);

            assertThat(result).contains("\"user\"");
            assertThat(result).contains("\"name\"");
            assertThat(result).contains("\n");
        }

        @Test
        @DisplayName("should format array")
        void formatArray() {
            @NotNull String sql = "SELECT JSON_PRETTY('[1,2,3]')";
            @Nullable String result = executeNativeQuery(sql);

            assertThat(result).contains("[");
            assertThat(result).contains("]");
        }

        @Test
        @DisplayName("should handle null")
        void handleNull() {
            @NotNull String sql = "SELECT JSON_PRETTY(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should preserve data")
        void preserveData() {
            @NotNull String sql = "SELECT JSON_PRETTY('{\"key\":\"value\"}')";
            @Nullable String result = executeNativeQuery(sql);

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
            @NotNull String sql = "SELECT JSON_STORAGE_SIZE('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return size for array")
        void returnSizeForArray() {
            @NotNull String sql = "SELECT JSON_STORAGE_SIZE('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should return larger size for bigger document")
        void returnLargerSizeForBiggerDocument() {
            @NotNull String sql1 = "SELECT JSON_STORAGE_SIZE('{\"a\": 1}')";
            @NotNull String sql2 = "SELECT JSON_STORAGE_SIZE('{\"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4, \"e\": 5}')";

            int size1 = ((Number) executeScalar(sql1)).intValue();
            int size2 = ((Number) executeScalar(sql2)).intValue();

            assertThat(size2).isGreaterThan(size1);
        }

        @Test
        @DisplayName("should return null for null input")
        void returnNullForNullInput() {
            @NotNull String sql = "SELECT JSON_STORAGE_SIZE(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return size for empty object")
        void returnSizeForEmptyObject() {
            @NotNull String sql = "SELECT JSON_STORAGE_SIZE('{}')";
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
            @NotNull String sql = "SELECT JSON_STORAGE_FREE('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should work with column value")
        void workWithColumnValue() {
            // Create a user with metadata
            createUser("Test", "test@example.com", "{\"data\": \"value\"}");

            // JSON_STORAGE_FREE on column should return 0 for non-updated values
            @NotNull String sql = "SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE email = 'test@example.com'";
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
            @NotNull String small = "'{}'";
            @NotNull String large = "'{\"a\":1,\"b\":2,\"c\":3,\"d\":4,\"e\":5}'";

            @NotNull String sql = String.format("SELECT JSON_STORAGE_SIZE(%s) < JSON_STORAGE_SIZE(%s)", small, large);
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should pretty print and check validity")
        void prettyPrintAndCheckValidity() {
            // Pretty printed JSON should still be valid
            @NotNull String sql = "SELECT JSON_VALID(JSON_PRETTY('{\"a\":1}'))";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }
}
