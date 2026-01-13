package com.github.snowykte0426.querydsl.mysql.json.core.functions;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * Factory class for MySQL JSON utility functions.
 *
 * <p>This class provides static factory methods for JSON utility operations:
 * <ul>
 *   <li>{@link #jsonPretty} - Formats JSON for readability</li>
 *   <li>{@link #jsonStorageSize} - Returns storage space used</li>
 *   <li>{@link #jsonStorageFree} - Returns freed space after update</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public final class JsonUtilityFunctions {

    private JsonUtilityFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_PRETTY - Format for readability
    // ========================================

    /**
     * Formats a JSON document in a human-readable format with indentation.
     *
     * <p>SQL: {@code JSON_PRETTY(json_val)}</p>
     *
     * <p>Returns the JSON value formatted with newlines and indentation.
     * This is useful for debugging and logging JSON data.
     *
     * <p>Example:
     * <pre>
     * JSON_PRETTY('{"a":1,"b":2}')
     * Output:
     * {
     *   "a": 1,
     *   "b": 2
     * }
     * </pre>
     *
     * @param jsonValue the JSON value expression to format
     * @return formatted JSON as string expression
     */
    public static StringExpression jsonPretty(Expression<?> jsonValue) {
        return Expressions.stringTemplate("json_pretty({0})", jsonValue);
    }

    /**
     * Formats a JSON string literal in a human-readable format.
     *
     * @param jsonString the JSON string to format
     * @return formatted JSON as string expression
     */
    public static StringExpression jsonPretty(String jsonString) {
        return Expressions.stringTemplate(
            "json_pretty({0})",
            Expressions.constant(jsonString)
        );
    }

    // ========================================
    // JSON_STORAGE_SIZE - Storage space
    // ========================================

    /**
     * Returns the number of bytes used to store the binary representation of a JSON document.
     *
     * <p>SQL: {@code JSON_STORAGE_SIZE(json_val)}</p>
     *
     * <p>For a JSON column, this returns the space used to store the JSON document.
     * For a string, this returns the space that would be used if the string were
     * converted to JSON.
     *
     * <p>Example:
     * <pre>
     * JSON_STORAGE_SIZE('[1, 2, 3]')          -&gt; 16 (bytes)
     * JSON_STORAGE_SIZE('{"a": 1, "b": 2}')   -&gt; 32 (bytes)
     * </pre>
     *
     * @param jsonValue the JSON value expression
     * @return storage size in bytes as integer expression
     */
    public static NumberExpression<Integer> jsonStorageSize(Expression<?> jsonValue) {
        return Expressions.numberTemplate(Integer.class, "json_storage_size({0})", jsonValue);
    }

    /**
     * Returns the storage size for a JSON string literal.
     *
     * @param jsonString the JSON string
     * @return storage size in bytes as integer expression
     */
    public static NumberExpression<Integer> jsonStorageSize(String jsonString) {
        return Expressions.numberTemplate(
            Integer.class,
            "json_storage_size({0})",
            Expressions.constant(jsonString)
        );
    }

    // ========================================
    // JSON_STORAGE_FREE - Freed space
    // ========================================

    /**
     * Returns the amount of storage space freed in a JSON column by a partial update.
     *
     * <p>SQL: {@code JSON_STORAGE_FREE(json_val)}</p>
     *
     * <p>This function shows how much storage space was freed in a JSON column
     * value by a partial update using JSON_SET, JSON_REPLACE, or JSON_REMOVE.
     * For a value that hasn't been partially updated, this returns 0.
     *
     * <p>Example use case:
     * <pre>
     * -- Before update
     * SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE id = 1;  -&gt; 0
     *
     * -- After partial update
     * UPDATE users SET metadata = JSON_REMOVE(metadata, '$.oldField') WHERE id = 1;
     * SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE id = 1;  -&gt; 20 (bytes freed)
     * </pre>
     *
     * <p>Note: This function only works on JSON columns, not on string literals.
     *
     * @param jsonColumn the JSON column expression
     * @return freed space in bytes as integer expression
     */
    public static NumberExpression<Integer> jsonStorageFree(Expression<?> jsonColumn) {
        return Expressions.numberTemplate(Integer.class, "json_storage_free({0})", jsonColumn);
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Formats and returns a pretty-printed JSON document.
     * Alias for {@link #jsonPretty(Expression)}.
     *
     * @param jsonValue the JSON value to format
     * @return formatted JSON string
     */
    public static StringExpression format(Expression<?> jsonValue) {
        return jsonPretty(jsonValue);
    }

    /**
     * Calculates the storage efficiency ratio (freed space / total size).
     * Returns a value between 0.0 and 1.0.
     *
     * <p>This can be useful to determine if a OPTIMIZE TABLE operation
     * would be beneficial for the table.
     *
     * @param jsonColumn the JSON column expression
     * @return efficiency ratio as double expression (freed / size)
     */
    public static NumberExpression<Double> storageEfficiency(Expression<?> jsonColumn) {
        return Expressions.numberTemplate(
            Double.class,
            "json_storage_free({0}) / json_storage_size({0})",
            jsonColumn
        );
    }

    /**
     * Checks if a JSON column has significant freed space (&gt; threshold bytes).
     *
     * @param jsonColumn the JSON column expression
     * @param thresholdBytes the minimum freed bytes to consider significant
     * @return boolean expression
     */
    public static com.querydsl.core.types.dsl.BooleanExpression hasSignificantFreedSpace(
        Expression<?> jsonColumn,
        int thresholdBytes
    ) {
        return Expressions.booleanTemplate(
            "json_storage_free({0}) > {1}",
            jsonColumn,
            thresholdBytes
        );
    }
}