package io.github.snowykte0426.querydsl.mysql.json.core.functions;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * Factory class for MySQL JSON attribute functions.
 *
 * <p>This class provides static factory methods for inspecting JSON value attributes:
 * <ul>
 *   <li>{@link #jsonDepth} - Returns maximum depth of JSON document</li>
 *   <li>{@link #jsonLength} - Returns number of elements</li>
 *   <li>{@link #jsonType} - Returns type of JSON value</li>
 *   <li>{@link #jsonValid} - Tests if value is valid JSON</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonAttributeFunctions {

    private JsonAttributeFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_DEPTH - Maximum depth
    // ========================================

    /**
     * Returns the maximum depth of a JSON document.
     *
     * <p>SQL: {@code JSON_DEPTH(json_doc)}</p>
     *
     * <p>Depth is defined as:
     * <ul>
     *   <li>Empty array, empty object, or scalar: depth 1</li>
     *   <li>Non-empty array/object with only depth-1 values: depth 2</li>
     *   <li>And so on...</li>
     * </ul>
     *
     * <p>Example:
     * <pre>
     * JSON_DEPTH('{}')           -&gt; 1
     * JSON_DEPTH('[1, 2]')       -&gt; 2
     * JSON_DEPTH('{"a": [1]}')   -&gt; 3
     * </pre>
     *
     * @param jsonDoc the JSON document expression
     * @return depth as integer expression
     */
    public static NumberExpression<Integer> jsonDepth(Expression<?> jsonDoc) {
        return Expressions.numberTemplate(Integer.class, "json_depth({0})", jsonDoc);
    }

    // ========================================
    // JSON_LENGTH - Number of elements
    // ========================================

    /**
     * Returns the number of elements in a JSON document.
     *
     * <p>SQL: {@code JSON_LENGTH(json_doc)}</p>
     *
     * <p>Length is defined as:
     * <ul>
     *   <li>Scalar: length 1</li>
     *   <li>Array: number of array elements</li>
     *   <li>Object: number of keys</li>
     * </ul>
     *
     * <p>Example:
     * <pre>
     * JSON_LENGTH('[1, 2, 3]')           -&gt; 3
     * JSON_LENGTH('{"a": 1, "b": 2}')    -&gt; 2
     * JSON_LENGTH('5')                   -&gt; 1
     * </pre>
     *
     * @param jsonDoc the JSON document expression
     * @return length as integer expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc) {
        return Expressions.numberTemplate(Integer.class, "json_length({0})", jsonDoc);
    }

    /**
     * Returns the number of elements at a specific path in a JSON document.
     *
     * <p>SQL: {@code JSON_LENGTH(json_doc, path)}</p>
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return length as integer expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc, String path) {
        return Expressions.numberTemplate(
            Integer.class,
            "json_length({0}, {1})",
            jsonDoc,
            Expressions.constant(path)
        );
    }

    // ========================================
    // JSON_TYPE - Type of JSON value
    // ========================================

    /**
     * Returns the type of a JSON value as a string.
     *
     * <p>SQL: {@code JSON_TYPE(json_val)}</p>
     *
     * <p>Possible return values:
     * <ul>
     *   <li>{@code NULL} - JSON null or SQL NULL</li>
     *   <li>{@code INTEGER} - MySQL integer or boolean</li>
     *   <li>{@code DOUBLE} - MySQL double</li>
     *   <li>{@code STRING} - MySQL string</li>
     *   <li>{@code OBJECT} - JSON object</li>
     *   <li>{@code ARRAY} - JSON array</li>
     *   <li>{@code BOOLEAN} - MySQL true/false (MySQL 8.0.21+)</li>
     *   <li>{@code DATE} - MySQL date (MySQL 8.0.21+)</li>
     *   <li>{@code TIME} - MySQL time (MySQL 8.0.21+)</li>
     *   <li>{@code DATETIME} - MySQL datetime (MySQL 8.0.21+)</li>
     *   <li>{@code TIMESTAMP} - MySQL timestamp (MySQL 8.0.21+)</li>
     * </ul>
     *
     * <p>Example:
     * <pre>
     * JSON_TYPE('[1, 2]')        -&gt; "ARRAY"
     * JSON_TYPE('{"a": 1}')      -&gt; "OBJECT"
     * JSON_TYPE('"hello"')       -&gt; "STRING"
     * JSON_TYPE('123')           -&gt; "INTEGER"
     * JSON_TYPE('null')          -&gt; "NULL"
     * </pre>
     *
     * @param jsonValue the JSON value expression
     * @return type as string expression
     */
    public static StringExpression jsonType(Expression<?> jsonValue) {
        return Expressions.stringTemplate("json_type({0})", jsonValue);
    }

    // ========================================
    // JSON_VALID - Validate JSON
    // ========================================

    /**
     * Tests whether a value is valid JSON.
     *
     * <p>SQL: {@code JSON_VALID(val)}</p>
     *
     * <p>Returns:
     * <ul>
     *   <li>{@code true} (1) if the value is valid JSON</li>
     *   <li>{@code false} (0) if not</li>
     *   <li>{@code NULL} if the argument is NULL</li>
     * </ul>
     *
     * <p>Example:
     * <pre>
     * JSON_VALID('{"a": 1}')     -&gt; true
     * JSON_VALID('[1, 2, 3]')    -&gt; true
     * JSON_VALID('"hello"')      -&gt; true
     * JSON_VALID('invalid')      -&gt; false
     * JSON_VALID('{incomplete')  -&gt; false
     * </pre>
     *
     * @param value the value to validate
     * @return boolean expression indicating validity
     */
    public static BooleanExpression jsonValid(Expression<?> value) {
        return Expressions.booleanTemplate("json_valid({0})", value);
    }

    /**
     * Tests whether a string literal is valid JSON.
     *
     * @param jsonString the string to validate
     * @return boolean expression
     */
    public static BooleanExpression jsonValid(String jsonString) {
        return Expressions.booleanTemplate(
            "json_valid({0})",
            Expressions.constant(jsonString)
        );
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Tests whether a JSON document is empty (has no elements).
     *
     * <p>Returns true if:
     * <ul>
     *   <li>Array with 0 elements: {@code []}</li>
     *   <li>Object with 0 keys: {@code {}}</li>
     * </ul>
     *
     * <p>Note: Scalars (including null) have length 1, so they are NOT empty.</p>
     *
     * @param jsonDoc the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression isEmpty(Expression<?> jsonDoc) {
        return Expressions.booleanTemplate("json_length({0}) = 0", jsonDoc);
    }

    /**
     * Tests whether a JSON document is not empty.
     *
     * @param jsonDoc the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression isNotEmpty(Expression<?> jsonDoc) {
        return Expressions.booleanTemplate("json_length({0}) > 0", jsonDoc);
    }

    /**
     * Tests whether a JSON value is an array.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isArray(Expression<?> jsonValue) {
        return Expressions.booleanTemplate("json_type({0}) = 'ARRAY'", jsonValue);
    }

    /**
     * Tests whether a JSON value is an object.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isObject(Expression<?> jsonValue) {
        return Expressions.booleanTemplate("json_type({0}) = 'OBJECT'", jsonValue);
    }

    /**
     * Tests whether a JSON value is a scalar (not array or object).
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isScalar(Expression<?> jsonValue) {
        return Expressions.booleanTemplate(
            "json_type({0}) NOT IN ('ARRAY', 'OBJECT')",
            jsonValue
        );
    }

    /**
     * Tests whether a JSON value is null.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isNull(Expression<?> jsonValue) {
        return Expressions.booleanTemplate("json_type({0}) = 'NULL'", jsonValue);
    }
}