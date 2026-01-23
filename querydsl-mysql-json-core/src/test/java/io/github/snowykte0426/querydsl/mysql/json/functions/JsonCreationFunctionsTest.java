package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonCreationFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for JSON creation functions.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonCreationFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_ARRAY tests
    // ========================================

    @Test
    void jsonArray_withSimpleValues_shouldCreateArray() throws SQLException {
        // Given
        @NotNull JsonArrayExpression arr = jsonArray("a", "b", "c");

        // When
        @Nullable String result = executeScalar(arr);

        // Then - Note: MySQL formats JSON with spaces
        assertThat(result).isIn("[\"a\", \"b\", \"c\"]", "[\"a\",\"b\",\"c\"]");
    }

    @Test
    void jsonArray_withNumbers_shouldCreateArray() throws SQLException {
        // Given
        @NotNull JsonArrayExpression arr = jsonArray(1, 2, 3);

        // When
        @Nullable String result = executeScalar(arr);

        // Then
        assertThat(result).isIn("[1, 2, 3]", "[1,2,3]");
    }

    @Test
    void jsonArray_withMixedTypes_shouldCreateArray() throws SQLException {
        // Given
        @NotNull JsonArrayExpression arr = jsonArray("text", 42, true, 3.14);

        // When
        @Nullable String result = executeScalar(arr);

        // Then
        // Note: MySQL JSON_ARRAY converts boolean true/false to 1/0
        assertThat(result).contains("text", "42", "3.14");
        assertThat(result).containsAnyOf("1", "true"); // MySQL may convert true to 1
    }

    @Test
    void jsonArray_empty_shouldCreateEmptyArray() throws SQLException {
        // Given
        @NotNull JsonArrayExpression arr = emptyJsonArray();

        // When
        @Nullable String result = executeScalar(arr);

        // Then
        assertThat(result).isEqualTo("[]");
    }

    @Test
    void jsonArrayFrom_withList_shouldCreateArray() throws SQLException {
        // Given
        @NotNull List<String> list = List.of("x", "y", "z");
        @NotNull JsonArrayExpression arr = jsonArrayFrom(list);

        // When
        @Nullable String result = executeScalar(arr);

        // Then
        assertThat(result).contains("x", "y", "z");
    }

    // ========================================
    // JSON_OBJECT tests
    // ========================================

    @Test
    void jsonObject_withKeyValuePairs_shouldCreateObject() throws SQLException {
        // Given
        @NotNull JsonObjectExpression obj = jsonObject("name", "John", "age", 30);

        // When
        @Nullable String result = executeScalar(obj);

        // Then
        assertThat(result).contains("\"name\"", "\"John\"", "\"age\"", "30");
    }

    @Test
    void jsonObject_withBuilder_shouldCreateObject() throws SQLException {
        // Given
        @NotNull JsonObjectExpression obj = jsonObjectBuilder().put("city", "Seoul").put("country", "Korea").build();

        // When
        @Nullable String result = executeScalar(obj);

        // Then
        assertThat(result).contains("\"city\"", "\"Seoul\"", "\"country\"", "\"Korea\"");
    }

    @Test
    void jsonObject_empty_shouldCreateEmptyObject() throws SQLException {
        // Given
        @NotNull JsonObjectExpression obj = emptyJsonObject();

        // When
        @Nullable String result = executeScalar(obj);

        // Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    void jsonObject_withOddArguments_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonObject("key")).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("even number");
    }

    @Test
    void jsonObjectFrom_withMap_shouldCreateObject() throws SQLException {
        // Given
        @NotNull Map<String, Object> map = Map.of("status", "active", "count", 5);
        @NotNull JsonObjectExpression obj = jsonObjectFrom(map);

        // When
        @Nullable String result = executeScalar(obj);

        // Then
        assertThat(result).contains("\"status\"", "\"active\"", "\"count\"", "5");
    }

    // ========================================
    // JSON_QUOTE tests
    // ========================================

    @Test
    void jsonQuote_withSimpleString_shouldQuoteString() throws SQLException {
        // Given
        @NotNull JsonValueExpression quoted = jsonQuote("hello");

        // When
        @Nullable String result = executeScalar(quoted);

        // Then
        assertThat(result).isEqualTo("\"hello\"");
    }

    @Test
    void jsonQuote_withSpecialCharacters_shouldEscapeString() throws SQLException {
        // Given
        @NotNull JsonValueExpression quoted = jsonQuote("Hello \"World\"");

        // When
        @Nullable String result = executeScalar(quoted);

        // Then
        assertThat(result).contains("Hello").contains("World");
        assertThat(result).startsWith("\"");
        assertThat(result).endsWith("\"");
    }

    @Test
    void jsonQuote_withNewline_shouldEscapeNewline() throws SQLException {
        // Given
        @NotNull JsonValueExpression quoted = jsonQuote("line1\nline2");

        // When
        @Nullable String result = executeScalar(quoted);

        // Then
        assertThat(result).contains("line1").contains("line2");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonArray_insertedIntoDatabase_shouldBeRetrievable() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('Test User', 'test@example.com', JSON_ARRAY('tag1', 'tag2', 'tag3'))");

        // When
        @Nullable String tags = executeScalar("SELECT metadata FROM users WHERE name = 'Test User'");

        // Then
        assertThat(tags).isNotNull();
        assertThat(tags).contains("tag1", "tag2", "tag3");
    }

    @Test
    void jsonObject_insertedIntoDatabase_shouldBeRetrievable() throws SQLException {
        // Given
        long userId = executeUpdate("INSERT INTO users (name, email, settings) VALUES "
                + "('Test User', 'test@example.com', JSON_OBJECT('theme', 'dark', 'lang', 'en'))");

        // When
        @Nullable String settings = executeScalar("SELECT settings FROM users WHERE id = " + userId);

        // Then
        assertThat(settings).contains("\"theme\"", "\"dark\"", "\"lang\"", "\"en\"");
    }

    @Test
    void nestedJsonStructures_shouldWork() throws SQLException {
        // Given - Create nested JSON: {"user": {"name": "John", "tags": ["a", "b"]}}
        @NotNull String sql = "SELECT JSON_OBJECT(" + "'user', JSON_OBJECT(" + "'name', 'John', "
                + "'tags', JSON_ARRAY('a', 'b')" + ")" + ")";

        // When
        @Nullable String result = executeScalar(sql);

        // Then
        assertThat(result).contains("\"user\"", "\"name\"", "\"John\"", "\"tags\"");
        assertThat(result).contains("\"a\"", "\"b\"");
    }

    @Test
    void jsonNull_shouldCreateNullValue() throws SQLException {
        // When
        @Nullable String result = executeScalar("SELECT " + jsonNull());

        // Then
        assertThat(result).isNull();
    }
}
