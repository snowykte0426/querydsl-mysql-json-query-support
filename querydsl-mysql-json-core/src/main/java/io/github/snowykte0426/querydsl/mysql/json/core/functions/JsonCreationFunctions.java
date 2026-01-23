package io.github.snowykte0426.querydsl.mysql.json.core.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for MySQL JSON creation functions.
 *
 * <p>
 * This class provides static factory methods for creating JSON values:
 * <ul>
 * <li>{@link #jsonArray(Object...)} - Creates JSON arrays</li>
 * <li>{@link #jsonObject(Object...)} - Creates JSON objects</li>
 * <li>{@link #jsonQuote(String)} - Quotes strings as JSON values</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Create JSON array
 * JsonArrayExpression arr = JsonCreationFunctions.jsonArray("a", "b", "c");
 * // SQL: JSON_ARRAY('a', 'b', 'c')
 *
 * // Create JSON object
 * JsonObjectExpression obj = JsonCreationFunctions.jsonObject("name", "John", "age", 30);
 * // SQL: JSON_OBJECT('name', 'John', 'age', 30)
 *
 * // Quote string as JSON
 * JsonValueExpression quoted = JsonCreationFunctions.jsonQuote("Hello \"World\"");
 * // SQL: JSON_QUOTE('Hello "World"')
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonCreationFunctions {

    private JsonCreationFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_ARRAY - Creates JSON arrays
    // ========================================

    /**
     * Creates a JSON array from the given values.
     *
     * <p>
     * SQL: {@code JSON_ARRAY(val1, val2, ...)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonArrayExpression arr = jsonArray("a", "b", "c");
     * // Result: ["a", "b", "c"]
     * }</pre>
     *
     * @param values
     *            the array values
     * @return JsonArrayExpression
     */
    @NotNull
    public static JsonArrayExpression jsonArray(@NotNull Object... values) {
        return JsonArrayExpression.create(values);
    }

    /**
     * Creates a JSON array from expressions.
     *
     * <p>
     * SQL: {@code JSON_ARRAY(expr1, expr2, ...)}
     * </p>
     *
     * @param expressions
     *            the value expressions
     * @return JsonArrayExpression
     */
    @NotNull
    public static JsonArrayExpression jsonArray(@NotNull Expression<?>... expressions) {
        return JsonArrayExpression.create((Object[]) expressions);
    }

    /**
     * Creates an empty JSON array.
     *
     * <p>
     * SQL: {@code JSON_ARRAY()}
     * </p>
     *
     * @return empty JsonArrayExpression
     */
    @NotNull
    public static JsonArrayExpression emptyJsonArray() {
        return JsonArrayExpression.empty();
    }

    // ========================================
    // JSON_OBJECT - Creates JSON objects
    // ========================================

    /**
     * Creates a JSON object from key-value pairs.
     *
     * <p>
     * SQL: {@code JSON_OBJECT(key1, val1, key2, val2, ...)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonObjectExpression obj = jsonObject("name", "John", "age", 30, "active", true);
     * // Result: {"name": "John", "age": 30, "active": true}
     * }</pre>
     *
     * @param keyValuePairs
     *            alternating keys and values
     * @return JsonObjectExpression
     * @throws IllegalArgumentException
     *             if odd number of arguments
     */
    @NotNull
    public static JsonObjectExpression jsonObject(@NotNull Object... keyValuePairs) {
        return JsonObjectExpression.create(keyValuePairs);
    }

    /**
     * Creates an empty JSON object.
     *
     * <p>
     * SQL: {@code JSON_OBJECT()}
     * </p>
     *
     * @return empty JsonObjectExpression
     */
    @NotNull
    public static JsonObjectExpression emptyJsonObject() {
        return JsonObjectExpression.empty();
    }

    /**
     * Returns a builder for creating JSON objects fluently.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonObjectExpression obj = jsonObjectBuilder().put("name", "John").put("age", 30).build();
     * }</pre>
     *
     * @return JsonObjectBuilder
     */
    @NotNull
    public static JsonObjectExpression.JsonObjectBuilder jsonObjectBuilder() {
        return JsonObjectExpression.builder();
    }

    // ========================================
    // JSON_QUOTE - Quotes strings as JSON
    // ========================================

    /**
     * Quotes a string as a JSON value by wrapping it with quote characters and
     * escaping interior quotes and special characters.
     *
     * <p>
     * SQL: {@code JSON_QUOTE(string)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonValueExpression quoted = jsonQuote("Hello \"World\"");
     * // Result: "Hello \"World\""
     * }</pre>
     *
     * @param value
     *            the string to quote
     * @return JsonValueExpression with quoted string
     */
    @NotNull
    public static JsonValueExpression jsonQuote(@NotNull String value) {
        return JsonValueExpression.quote(value);
    }

    /**
     * Quotes a string expression as a JSON value.
     *
     * <p>
     * SQL: {@code JSON_QUOTE(expr)}
     * </p>
     *
     * @param expression
     *            the string expression to quote
     * @return JsonValueExpression with quoted string
     */
    @NotNull
    public static JsonValueExpression jsonQuote(@NotNull StringExpression expression) {
        return JsonValueExpression.quote(expression);
    }

    /**
     * Quotes a string expression as a JSON value.
     *
     * <p>
     * SQL: {@code JSON_QUOTE(expr)}
     * </p>
     *
     * @param expression
     *            the expression to quote
     * @return JsonValueExpression with quoted string
     */
    @NotNull
    public static JsonValueExpression jsonQuote(@NotNull Expression<String> expression) {
        return JsonValueExpression.quote(expression);
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Creates a JSON array from a Java collection.
     *
     * @param collection
     *            the collection to convert
     * @return JsonArrayExpression
     */
    @NotNull
    public static JsonArrayExpression jsonArrayFrom(@NotNull Iterable<?> collection) {
        if (collection instanceof java.util.Collection) {
            return jsonArray(((java.util.Collection<?>) collection).toArray());
        }

        java.util.@NotNull List<Object> list = new java.util.ArrayList<>();
        for (Object item : collection) {
            list.add(item);
        }
        return jsonArray(list.toArray());
    }

    /**
     * Creates a JSON object from a Java Map.
     *
     * @param map
     *            the map to convert
     * @return JsonObjectExpression
     */
    @NotNull
    public static JsonObjectExpression jsonObjectFrom(@NotNull java.util.Map<String, ?> map) {
        Object @NotNull [] keyValuePairs = new Object[map.size() * 2];
        int i = 0;
        for (java.util.Map.@NotNull Entry<String, ?> entry : map.entrySet()) {
            keyValuePairs[i++] = entry.getKey();
            keyValuePairs[i++] = entry.getValue();
        }
        return jsonObject(keyValuePairs);
    }

    /**
     * Creates a JSON null value.
     *
     * <p>
     * SQL: {@code CAST(NULL AS JSON)}
     * </p>
     *
     * @return Expression representing JSON null
     */
    @NotNull
    public static StringExpression jsonNull() {
        return Expressions.stringTemplate("CAST(NULL AS JSON)");
    }
}
