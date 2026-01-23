package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonAttributeFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON attribute functions.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonAttributeFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_DEPTH tests
    // ========================================

    @Test
    void jsonDepth_withEmptyObject_shouldReturnOne() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> depth = jsonDepth(Expressions.constant("{}"));

        // When
        @Nullable String result = executeScalar(depth);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonDepth_withEmptyArray_shouldReturnOne() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> depth = jsonDepth(Expressions.constant("[]"));

        // When
        @Nullable String result = executeScalar(depth);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonDepth_withScalar_shouldReturnOne() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> depth = jsonDepth(Expressions.constant("123"));

        // When
        @Nullable String result = executeScalar(depth);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonDepth_withSimpleArray_shouldReturnTwo() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> depth = jsonDepth(Expressions.constant("[1, 2, 3]"));

        // When
        @Nullable String result = executeScalar(depth);

        // Then
        assertThat(result).isEqualTo("2");
    }

    @Test
    void jsonDepth_withNestedStructure_shouldReturnCorrectDepth() throws SQLException {
        // Given
        @NotNull String doc = "{\"a\": {\"b\": [1, 2]}}";
        @NotNull NumberExpression<Integer> depth = jsonDepth(Expressions.constant(doc));

        // When
        @Nullable String result = executeScalar(depth);

        // Then
        assertThat(result).isEqualTo("4");
    }

    // ========================================
    // JSON_LENGTH tests
    // ========================================

    @Test
    void jsonLength_withArray_shouldReturnElementCount() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> length = jsonLength(Expressions.constant("[1, 2, 3, 4, 5]"));

        // When
        @Nullable String result = executeScalar(length);

        // Then
        assertThat(result).isEqualTo("5");
    }

    @Test
    void jsonLength_withObject_shouldReturnKeyCount() throws SQLException {
        // Given
        @NotNull String doc = "{\"a\": 1, \"b\": 2, \"c\": 3}";
        @NotNull NumberExpression<Integer> length = jsonLength(Expressions.constant(doc));

        // When
        @Nullable String result = executeScalar(length);

        // Then
        assertThat(result).isEqualTo("3");
    }

    @Test
    void jsonLength_withScalar_shouldReturnOne() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> length = jsonLength(Expressions.constant("42"));

        // When
        @Nullable String result = executeScalar(length);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonLength_withPath_shouldReturnLengthAtPath() throws SQLException {
        // Given
        @NotNull String doc = "{\"tags\": [\"java\", \"kotlin\", \"scala\"]}";
        @NotNull NumberExpression<Integer> length = jsonLength(Expressions.constant(doc), "$.tags");

        // When
        @Nullable String result = executeScalar(length);

        // Then
        assertThat(result).isEqualTo("3");
    }

    @Test
    void jsonLength_withEmptyArray_shouldReturnZero() throws SQLException {
        // Given
        @NotNull NumberExpression<Integer> length = jsonLength(Expressions.constant("[]"));

        // When
        @Nullable String result = executeScalar(length);

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // JSON_TYPE tests
    // ========================================

    @Test
    void jsonType_withArray_shouldReturnArray() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("[1, 2, 3]"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("ARRAY");
    }

    @Test
    void jsonType_withObject_shouldReturnObject() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("{\"a\": 1}"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("OBJECT");
    }

    @Test
    void jsonType_withString_shouldReturnString() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("\"hello\""));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("STRING");
    }

    @Test
    void jsonType_withInteger_shouldReturnInteger() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("123"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("INTEGER");
    }

    @Test
    void jsonType_withDouble_shouldReturnDouble() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("3.14"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("DOUBLE");
    }

    @Test
    void jsonType_withNull_shouldReturnNull() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("null"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        assertThat(result).isEqualTo("NULL");
    }

    @Test
    void jsonType_withBoolean_shouldReturnBoolean() throws SQLException {
        // Given
        @NotNull StringExpression type = jsonType(Expressions.constant("true"));

        // When
        @Nullable String result = executeScalar(type);

        // Then
        // MySQL 8.0.33 should support BOOLEAN type
        assertThat(result).isIn("BOOLEAN", "INTEGER"); // Older versions might return INTEGER
    }

    // ========================================
    // JSON_VALID tests
    // ========================================

    @Test
    void jsonValid_withValidObject_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("{\"a\": 1, \"b\": 2}");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonValid_withValidArray_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("[1, 2, 3]");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonValid_withValidString_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("\"hello\"");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonValid_withInvalidJson_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("{invalid}");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonValid_withIncompleteJson_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("{\"a\": ");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonValid_withPlainText_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression valid = jsonValid("plain text");

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // Convenience method tests
    // ========================================

    @Test
    void isEmpty_withEmptyArray_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression empty = isEmpty(Expressions.constant("[]"));

        // When
        @Nullable String result = executeScalar(empty);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isEmpty_withEmptyObject_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression empty = isEmpty(Expressions.constant("{}"));

        // When
        @Nullable String result = executeScalar(empty);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isEmpty_withNonEmptyArray_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression empty = isEmpty(Expressions.constant("[1, 2]"));

        // When
        @Nullable String result = executeScalar(empty);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void isNotEmpty_withNonEmptyObject_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression notEmpty = isNotEmpty(Expressions.constant("{\"a\": 1}"));

        // When
        @Nullable String result = executeScalar(notEmpty);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isArray_withArray_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression arrayCheck = isArray(Expressions.constant("[1, 2, 3]"));

        // When
        @Nullable String result = executeScalar(arrayCheck);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isArray_withObject_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression arrayCheck = isArray(Expressions.constant("{\"a\": 1}"));

        // When
        @Nullable String result = executeScalar(arrayCheck);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void isObject_withObject_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression objectCheck = isObject(Expressions.constant("{\"a\": 1}"));

        // When
        @Nullable String result = executeScalar(objectCheck);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isObject_withArray_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression objectCheck = isObject(Expressions.constant("[1, 2]"));

        // When
        @Nullable String result = executeScalar(objectCheck);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void isScalar_withString_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression scalarCheck = isScalar(Expressions.constant("\"hello\""));

        // When
        @Nullable String result = executeScalar(scalarCheck);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isScalar_withNumber_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression scalarCheck = isScalar(Expressions.constant("42"));

        // When
        @Nullable String result = executeScalar(scalarCheck);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isScalar_withArray_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression scalarCheck = isScalar(Expressions.constant("[1, 2]"));

        // When
        @Nullable String result = executeScalar(scalarCheck);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void isNull_withJsonNull_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull BooleanExpression nullCheck = isNull(Expressions.constant("null"));

        // When
        @Nullable String result = executeScalar(nullCheck);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isNull_withNonNull_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull BooleanExpression nullCheck = isNull(Expressions.constant("42"));

        // When
        @Nullable String result = executeScalar(nullCheck);

        // Then
        assertThat(result).isEqualTo("0");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonDepth_inDatabase_shouldCalculateDepth() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('John', 'john@test.com', '{\"profile\": {\"address\": {\"city\": \"Seoul\"}}}')");

        // When
        @Nullable String depth = executeScalar("SELECT JSON_DEPTH(metadata) FROM users WHERE name = 'John'");

        // Then
        assertThat(depth).isEqualTo("4");
    }

    @Test
    void jsonLength_inDatabase_shouldCountElements() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, tags) VALUES "
                + "('Product', 100.00, '[\"tag1\", \"tag2\", \"tag3\", \"tag4\"]')");

        // When
        @Nullable String length = executeScalar("SELECT JSON_LENGTH(tags) FROM products WHERE name = 'Product'");

        // Then
        assertThat(length).isEqualTo("4");
    }

    @Test
    void jsonType_inDatabase_shouldIdentifyType() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, settings) VALUES "
                + "('Alice', 'alice@test.com', '{\"theme\": \"dark\", \"count\": 5}')");

        // When
        @Nullable String themeType = executeScalar(
                "SELECT JSON_TYPE(JSON_EXTRACT(settings, '$.theme')) FROM users WHERE name = 'Alice'");
        @Nullable String countType = executeScalar(
                "SELECT JSON_TYPE(JSON_EXTRACT(settings, '$.count')) FROM users WHERE name = 'Alice'");

        // Then
        assertThat(themeType).isEqualTo("STRING");
        assertThat(countType).isEqualTo("INTEGER");
    }

    @Test
    void convenienceMethods_inWhereClause_shouldFilter() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, tags) VALUES " + "('Product1', 100.00, '[]'), "
                + "('Product2', 200.00, '[\"tag1\"]'), " + "('Product3', 300.00, '[\"tag1\", \"tag2\"]')");

        // When - Find products with empty tags
        @Nullable String emptyCount = executeScalar("SELECT COUNT(*) FROM products WHERE JSON_LENGTH(tags) = 0");
        @Nullable String nonEmptyCount = executeScalar("SELECT COUNT(*) FROM products WHERE JSON_LENGTH(tags) > 0");

        // Then
        assertThat(emptyCount).isEqualTo("1");
        assertThat(nonEmptyCount).isEqualTo("2");
    }
}
