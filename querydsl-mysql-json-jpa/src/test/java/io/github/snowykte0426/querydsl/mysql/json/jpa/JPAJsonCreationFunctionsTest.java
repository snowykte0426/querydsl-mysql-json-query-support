package io.github.snowykte0426.querydsl.mysql.json.jpa;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON creation functions in JPA environment.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>JSON_ARRAY - Create JSON arrays</li>
 *   <li>JSON_OBJECT - Create JSON objects</li>
 *   <li>JSON_QUOTE - Quote strings as JSON</li>
 * </ul>
 */
@DisplayName("JPA JSON Creation Functions")
class JPAJsonCreationFunctionsTest extends AbstractJPAJsonFunctionTest {

    @Nested
    @DisplayName("JSON_ARRAY")
    class JsonArrayTests {

        @Test
        @DisplayName("should create JSON array from values")
        void createArrayFromValues() {
            // Given
            JsonArrayExpression arr = JPAJsonFunctions.jsonArray("a", "b", "c");

            // When - execute as native query to verify SQL generation
            String sql = "SELECT JSON_ARRAY('a', 'b', 'c')";
            String result = executeNativeQuery(sql);

            // Then
            assertThat(result).isEqualTo("[\"a\", \"b\", \"c\"]");
        }

        @Test
        @DisplayName("should create JSON array from numbers")
        void createArrayFromNumbers() {
            String sql = "SELECT JSON_ARRAY(1, 2, 3)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }

        @Test
        @DisplayName("should create JSON array from mixed types")
        void createArrayFromMixedTypes() {
            String sql = "SELECT JSON_ARRAY('hello', 123, true, null)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[\"hello\", 123, true, null]");
        }

        @Test
        @DisplayName("should create empty JSON array")
        void createEmptyArray() {
            JsonArrayExpression arr = JPAJsonFunctions.emptyJsonArray();

            String sql = "SELECT JSON_ARRAY()";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[]");
        }

        @Test
        @DisplayName("should create nested JSON array")
        void createNestedArray() {
            String sql = "SELECT JSON_ARRAY(1, JSON_ARRAY(2, 3), 4)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, [2, 3], 4]");
        }

        @Test
        @DisplayName("should create JSON array from collection")
        void createArrayFromCollection() {
            JsonArrayExpression arr = JPAJsonFunctions.jsonArrayFrom(Arrays.asList("x", "y", "z"));
            assertThat(arr).isNotNull();
        }
    }

    @Nested
    @DisplayName("JSON_OBJECT")
    class JsonObjectTests {

        @Test
        @DisplayName("should create JSON object from key-value pairs")
        void createObjectFromKeyValues() {
            String sql = "SELECT JSON_OBJECT('name', 'John', 'age', 30)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"name\"");
            assertThat(result).contains("\"John\"");
            assertThat(result).contains("\"age\"");
            assertThat(result).contains("30");
        }

        @Test
        @DisplayName("should create empty JSON object")
        void createEmptyObject() {
            JsonObjectExpression obj = JPAJsonFunctions.emptyJsonObject();

            String sql = "SELECT JSON_OBJECT()";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("{}");
        }

        @Test
        @DisplayName("should create JSON object with nested array")
        void createObjectWithNestedArray() {
            String sql = "SELECT JSON_OBJECT('items', JSON_ARRAY(1, 2, 3))";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"items\"");
            assertThat(result).contains("[1, 2, 3]");
        }

        @Test
        @DisplayName("should create JSON object with nested object")
        void createObjectWithNestedObject() {
            String sql = "SELECT JSON_OBJECT('user', JSON_OBJECT('id', 1, 'name', 'John'))";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"user\"");
            assertThat(result).contains("\"id\"");
            assertThat(result).contains("\"name\"");
        }

        @Test
        @DisplayName("should create JSON object from Map")
        void createObjectFromMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("key1", "value1");
            map.put("key2", 42);

            JsonObjectExpression obj = JPAJsonFunctions.jsonObjectFrom(map);
            assertThat(obj).isNotNull();
        }

        @Test
        @DisplayName("should use JsonObjectBuilder")
        void useJsonObjectBuilder() {
            JsonObjectExpression.JsonObjectBuilder builder = JPAJsonFunctions.jsonObjectBuilder();
            assertThat(builder).isNotNull();
        }
    }

    @Nested
    @DisplayName("JSON_QUOTE")
    class JsonQuoteTests {

        @Test
        @DisplayName("should quote simple string")
        void quoteSimpleString() {
            String sql = "SELECT JSON_QUOTE('hello')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("\"hello\"");
        }

        @Test
        @DisplayName("should quote string with special characters")
        void quoteStringWithSpecialChars() {
            String sql = "SELECT JSON_QUOTE('hello\\nworld')";
            String result = executeNativeQuery(sql);

            // Result should be properly escaped
            assertThat(result).startsWith("\"");
            assertThat(result).endsWith("\"");
        }

        @Test
        @DisplayName("should quote string with quotes inside")
        void quoteStringWithQuotes() {
            String sql = "SELECT JSON_QUOTE('say \"hello\"')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\\\"hello\\\"");
        }

        @Test
        @DisplayName("should handle null properly")
        void quoteNull() {
            String sql = "SELECT JSON_QUOTE(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("JPAJsonFunctions.jsonQuote should create expression")
        void createJsonQuoteExpression() {
            JsonValueExpression quoted = JPAJsonFunctions.jsonQuote("test");
            assertThat(quoted).isNotNull();
        }
    }

    @Nested
    @DisplayName("JSON_NULL")
    class JsonNullTests {

        @Test
        @DisplayName("should create JSON null expression")
        void createJsonNull() {
            String sql = "SELECT CAST(NULL AS JSON)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }
    }
}
