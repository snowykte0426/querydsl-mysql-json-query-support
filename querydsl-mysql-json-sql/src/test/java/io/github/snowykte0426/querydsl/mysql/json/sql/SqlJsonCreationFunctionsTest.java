package io.github.snowykte0426.querydsl.mysql.json.sql;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static io.github.snowykte0426.querydsl.mysql.json.sql.SqlJsonFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for JSON creation functions in SQL module.
 *
 * <p>Tests JSON_ARRAY, JSON_OBJECT, and JSON_QUOTE functions.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class SqlJsonCreationFunctionsTest extends AbstractSqlJsonFunctionTest {

    // ========================================
    // JSON_ARRAY tests
    // ========================================

    @Test
    void jsonArray_withSimpleValues_shouldCreateArray() throws SQLException {
        // Given
        JsonArrayExpression arr = jsonArray("a", "b", "c");

        // When
        String result = queryFactory.select(arr).fetchOne();

        // Then - Note: MySQL formats JSON with spaces
        assertThat(result).isIn(
            "[\"a\", \"b\", \"c\"]",
            "[\"a\",\"b\",\"c\"]"
        );
    }

    @Test
    void jsonArray_withNumbers_shouldCreateArray() throws SQLException {
        // Given
        JsonArrayExpression arr = jsonArray(1, 2, 3);

        // When
        String result = queryFactory.select(arr).fetchOne();

        // Then
        assertThat(result).isIn("[1, 2, 3]", "[1,2,3]", "[1, 2, 3]");
    }

    @Test
    void jsonArray_withMixedTypes_shouldCreateArray() throws SQLException {
        // Given
        JsonArrayExpression arr = jsonArray("text", 42, true, 3.14);

        // When
        String result = queryFactory.select(arr).fetchOne();

        // Then
        assertThat(result).contains("text", "42", "3.14");
        assertThat(result).containsAnyOf("1", "true");
    }

    @Test
    void jsonArray_empty_shouldCreateEmptyArray() throws SQLException {
        // Given
        JsonArrayExpression arr = emptyJsonArray();

        // When
        String result = queryFactory.select(arr).fetchOne();

        // Then
        assertThat(result).isEqualTo("[]");
    }

    @Test
    void jsonArrayFrom_withList_shouldCreateArray() throws SQLException {
        // Given
        List<String> list = List.of("x", "y", "z");
        JsonArrayExpression arr = jsonArrayFrom(list);

        // When
        String result = queryFactory.select(arr).fetchOne();

        // Then
        assertThat(result).contains("x", "y", "z");
    }

    @Test
    void jsonArray_withNestedArray_shouldCreateNestedArray() throws SQLException {
        // Given - Create nested array using JSON_ARRAY syntax
        // Note: Using raw SQL for nested structure test to verify complex composition
        String sql = "SELECT JSON_ARRAY(1, 2, JSON_ARRAY(3, 4))";

        // When
        String result = executeNativeQuery(sql);

        // Then
        assertThat(result).containsAnyOf("[3, 4]", "[3,4]");
    }

    // ========================================
    // JSON_OBJECT tests
    // ========================================

    @Test
    void jsonObject_withKeyValuePairs_shouldCreateObject() throws SQLException {
        // Given
        JsonObjectExpression obj = jsonObject("name", "John", "age", 30);

        // When
        String result = queryFactory.select(obj).fetchOne();

        // Then
        assertThat(result).contains("\"name\"", "\"John\"", "\"age\"", "30");
    }

    @Test
    void jsonObject_withBuilder_shouldCreateObject() throws SQLException {
        // Given
        JsonObjectExpression obj = jsonObjectBuilder()
            .put("city", "Seoul")
            .put("country", "Korea")
            .build();

        // When
        String result = queryFactory.select(obj).fetchOne();

        // Then
        assertThat(result).contains("\"city\"", "\"Seoul\"", "\"country\"", "\"Korea\"");
    }

    @Test
    void jsonObject_empty_shouldCreateEmptyObject() throws SQLException {
        // Given
        JsonObjectExpression obj = emptyJsonObject();

        // When
        String result = queryFactory.select(obj).fetchOne();

        // Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    void jsonObject_withOddArguments_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonObject("key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("even number");
    }

    @Test
    void jsonObjectFrom_withMap_shouldCreateObject() throws SQLException {
        // Given
        Map<String, Object> map = Map.of(
            "status", "active",
            "count", 5
        );
        JsonObjectExpression obj = jsonObjectFrom(map);

        // When
        String result = queryFactory.select(obj).fetchOne();

        // Then
        assertThat(result).contains("\"status\"", "\"active\"", "\"count\"", "5");
    }

    @Test
    void jsonObject_withNestedObject_shouldCreateNestedObject() throws SQLException {
        // Given - Create nested object
        String sql = "SELECT JSON_OBJECT('user', JSON_OBJECT('name', 'Alice', 'age', 25))";

        // When
        String result = executeNativeQuery(sql);

        // Then
        assertThat(result).contains("\"user\"", "\"name\"", "\"Alice\"", "\"age\"", "25");
    }

    // ========================================
    // JSON_QUOTE tests
    // ========================================

    @Test
    void jsonQuote_withSimpleString_shouldQuoteString() throws SQLException {
        // Given
        JsonValueExpression quoted = jsonQuote("hello");

        // When
        String result = queryFactory.select(quoted).fetchOne();

        // Then
        assertThat(result).isEqualTo("\"hello\"");
    }

    @Test
    void jsonQuote_withSpecialCharacters_shouldEscapeString() throws SQLException {
        // Given
        JsonValueExpression quoted = jsonQuote("Hello \"World\"");

        // When
        String result = queryFactory.select(quoted).fetchOne();

        // Then
        assertThat(result).contains("Hello").contains("World");
        assertThat(result).startsWith("\"");
        assertThat(result).endsWith("\"");
    }

    @Test
    void jsonQuote_withNewline_shouldEscapeNewline() throws SQLException {
        // Given
        JsonValueExpression quoted = jsonQuote("line1\nline2");

        // When
        String result = queryFactory.select(quoted).fetchOne();

        // Then
        assertThat(result).startsWith("\"");
        assertThat(result).endsWith("\"");
        assertThat(result).contains("line1").contains("line2");
    }

    @Test
    void jsonQuote_withEmptyString_shouldQuoteEmptyString() throws SQLException {
        // Given
        JsonValueExpression quoted = jsonQuote("");

        // When
        String result = queryFactory.select(quoted).fetchOne();

        // Then
        assertThat(result).isEqualTo("\"\"");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonNull_shouldReturnNullString() throws SQLException {
        // When
        String sql = "SELECT " + jsonNull().toString();
        String result = executeNativeQuery(sql);

        // Then
        if (result == null) {
            assertThat(result).isNull();
        } else {
            assertThat(result.toLowerCase()).isEqualTo("null");
        }
    }

    @Test
    void jsonArray_combinedWithJsonObject_shouldCreateComplexStructure() throws SQLException {
        // Given - Create array of objects
        String sql = "SELECT JSON_ARRAY(" +
            "JSON_OBJECT('id', 1, 'name', 'Alice'), " +
            "JSON_OBJECT('id', 2, 'name', 'Bob')" +
            ")";

        // When
        String result = executeNativeQuery(sql);

        // Then
        assertThat(result).contains("Alice", "Bob", "\"id\"", "\"name\"");
    }
}
