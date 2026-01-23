package io.github.snowykte0426.querydsl.mysql.json.core.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.utils.JsonEscapeUtils;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * Factory class for MySQL JSON search functions.
 *
 * <p>
 * This class provides static factory methods for searching within JSON
 * documents:
 * <ul>
 * <li>{@link #jsonExtract} - Extracts data from JSON document</li>
 * <li>{@link #jsonValue} - Extracts scalar value from JSON</li>
 * <li>{@link #jsonContains} - Tests whether JSON contains value</li>
 * <li>{@link #jsonContainsPath} - Tests whether path exists</li>
 * <li>{@link #jsonKeys} - Returns keys from JSON object</li>
 * <li>{@link #jsonSearch} - Finds path to value</li>
 * <li>{@link #jsonOverlaps} - Tests whether two JSON documents overlap</li>
 * <li>{@link #memberOf} - Tests array membership</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonSearchFunctions {

    private JsonSearchFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_EXTRACT - Extract data from JSON
    // ========================================

    /**
     * Extracts data from a JSON document using a path expression.
     *
     * <p>
     * SQL: {@code JSON_EXTRACT(json_doc, path)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonExpression result = jsonExtract(user.metadata, "$.role");
     * // Extracts the "role" field from metadata
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path (e.g., "$.key")
     * @return extracted JSON expression
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        return JsonExpression.jsonExtract(jsonDoc, path);
    }

    /**
     * Extracts data from a JSON document using multiple paths.
     *
     * <p>
     * SQL: {@code JSON_EXTRACT(json_doc, path1, path2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param paths
     *            the JSON paths
     * @return extracted JSON expression (returns array if multiple paths)
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String... paths) {
        return JsonExpression.jsonExtract(jsonDoc, paths);
    }

    /**
     * Extracts and unquotes a value from a JSON document.
     *
     * <p>
     * SQL: {@code json_doc ->> path}
     * </p>
     *
     * <p>
     * This is equivalent to {@code JSON_UNQUOTE(JSON_EXTRACT(json_doc, path))}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return unquoted string expression
     */
    public static StringExpression jsonUnquoteExtract(Expression<?> jsonDoc, String path) {
        return JsonExpression.jsonUnquoteExtract(jsonDoc, path);
    }

    // ========================================
    // JSON_VALUE - Extract scalar value (MySQL 8.0.21+)
    // ========================================

    /**
     * Extracts a scalar value from a JSON document.
     *
     * <p>
     * SQL: {@code JSON_VALUE(json_doc, path)}
     * </p>
     *
     * <p>
     * Available in MySQL 8.0.21+. This function extracts a single scalar value and
     * returns NULL for non-scalar values.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return scalar value expression
     */
    public static JsonValueExpression jsonValue(Expression<?> jsonDoc, String path) {
        return JsonValueExpression.extract(jsonDoc, path);
    }

    // ========================================
    // JSON_CONTAINS - Test if JSON contains value
    // ========================================

    /**
     * Tests whether a JSON document contains a specific value.
     *
     * <p>
     * SQL: {@code JSON_CONTAINS(json_doc, val)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * BooleanExpression hasAdmin = jsonContains(user.roles, "\"admin\"");
     * // Check if roles array contains "admin"
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param value
     *            the value to search for (must be valid JSON)
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, String value) {
        return Expressions.booleanTemplate("json_contains({0}, {1})", jsonDoc, Expressions.constant(value));
    }

    /**
     * Tests whether a JSON document contains a value at a specific path.
     *
     * <p>
     * SQL: {@code JSON_CONTAINS(json_doc, val, path)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param value
     *            the value to search for
     * @param path
     *            the JSON path to search within
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, String value, String path) {
        return Expressions.booleanTemplate("json_contains({0}, {1}, {2})",
                jsonDoc,
                Expressions.constant(value),
                Expressions.constant(path));
    }

    /**
     * Tests whether a JSON document contains an expression value.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param value
     *            the value expression to search for
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, Expression<?> value) {
        return Expressions.booleanTemplate("json_contains({0}, {1})", jsonDoc, value);
    }

    // ========================================
    // JSON_CONTAINS - Convenience methods with auto-escaping
    // ========================================

    /**
     * Tests whether a JSON document contains a plain string value.
     *
     * <p>
     * This method automatically escapes the string as a JSON string literal.
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * // Before (manual escaping):
     * jsonContains(user.roles, "\"admin\"")
     *
     * // After (automatic):
     * jsonContainsString(user.roles, "admin")
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param plainString
     *            the plain string value (will be auto-escaped)
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsString(Expression<?> jsonDoc, String plainString) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeString(plainString));
    }

    /**
     * Tests whether a JSON document contains a string value at a specific path.
     *
     * <p>
     * This method automatically escapes the string as a JSON string literal.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param plainString
     *            the plain string value (will be auto-escaped)
     * @param path
     *            the JSON path to search within
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsString(Expression<?> jsonDoc, String plainString, String path) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeString(plainString), path);
    }

    /**
     * Tests whether a JSON document contains a numeric value.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * jsonContainsNumber(product.features, 42)
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param number
     *            the number to search for
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsNumber(Expression<?> jsonDoc, Number number) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeNumber(number));
    }

    /**
     * Tests whether a JSON document contains a numeric value at a specific path.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param number
     *            the number to search for
     * @param path
     *            the JSON path to search within
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsNumber(Expression<?> jsonDoc, Number number, String path) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeNumber(number), path);
    }

    /**
     * Tests whether a JSON document contains a boolean value.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * jsonContainsBoolean(user.settings, true)
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param bool
     *            the boolean value to search for
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsBoolean(Expression<?> jsonDoc, boolean bool) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeBoolean(bool));
    }

    /**
     * Tests whether a JSON document contains a boolean value at a specific path.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param bool
     *            the boolean value to search for
     * @param path
     *            the JSON path to search within
     * @return boolean expression
     * @since 0.1.0-Beta.4
     */
    public static BooleanExpression jsonContainsBoolean(Expression<?> jsonDoc, boolean bool, String path) {
        return jsonContains(jsonDoc, JsonEscapeUtils.escapeBoolean(bool), path);
    }

    // ========================================
    // JSON_CONTAINS_PATH - Test if path exists
    // ========================================

    /**
     * Tests whether a JSON document contains data at the specified paths.
     *
     * <p>
     * SQL: {@code JSON_CONTAINS_PATH(json_doc, one_or_all, path, ...)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * // Check if at least one path exists
     * BooleanExpression hasAny = jsonContainsPath(user.metadata, "one", "$.email", "$.phone");
     *
     * // Check if all paths exist
     * BooleanExpression hasAll = jsonContainsPath(user.metadata, "all", "$.name", "$.email");
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param oneOrAll
     *            "one" to match any path, "all" to match all paths
     * @param paths
     *            the paths to check
     * @return boolean expression
     */
    public static BooleanExpression jsonContainsPath(Expression<?> jsonDoc, String oneOrAll, String... paths) {
        Object[] args = new Object[paths.length + 2];
        args[0] = jsonDoc;
        args[1] = Expressions.constant(oneOrAll);
        for (int i = 0; i < paths.length; i++) {
            args[i + 2] = Expressions.constant(paths[i]);
        }

        StringBuilder template = new StringBuilder("json_contains_path({0}, {1}");
        for (int i = 0; i < paths.length; i++) {
            template.append(", {").append(i + 2).append("}");
        }
        template.append(")");

        return Expressions.booleanTemplate(template.toString(), args);
    }

    // ========================================
    // JSON_KEYS - Get keys from JSON object
    // ========================================

    /**
     * Returns the keys from the top-level value of a JSON object as a JSON array.
     *
     * <p>
     * SQL: {@code JSON_KEYS(json_doc)}
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * JsonArrayExpression keys = jsonKeys(user.metadata);
     * // Returns: ["name", "email", "age"]
     * }</pre>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return JSON array of keys
     */
    public static JsonArrayExpression jsonKeys(Expression<?> jsonDoc) {
        return JsonArrayExpression.wrap(Expressions.stringTemplate("json_keys({0})", jsonDoc));
    }

    /**
     * Returns the keys from a JSON object at the specified path.
     *
     * <p>
     * SQL: {@code JSON_KEYS(json_doc, path)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path to the object
     * @return JSON array of keys
     */
    public static JsonArrayExpression jsonKeys(Expression<?> jsonDoc, String path) {
        return JsonArrayExpression
                .wrap(Expressions.stringTemplate("json_keys({0}, {1})", jsonDoc, Expressions.constant(path)));
    }

    // ========================================
    // JSON_SEARCH - Find path to value
    // ========================================

    /**
     * Returns the path to the first occurrence of a string within a JSON document.
     *
     * <p>
     * SQL: {@code JSON_SEARCH(json_doc, 'one', search_str)}
     * </p>
     *
     * <p>
     * The search string supports wildcards % and _.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param searchString
     *            the string to search for (supports % and _ wildcards)
     * @return path to the value, or NULL if not found
     */
    public static JsonValueExpression jsonSearch(Expression<?> jsonDoc, String searchString) {
        return JsonValueExpression.search(jsonDoc, "one", searchString);
    }

    /**
     * Returns the path(s) to occurrences of a string within a JSON document.
     *
     * <p>
     * SQL: {@code JSON_SEARCH(json_doc, one_or_all, search_str)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param oneOrAll
     *            "one" for first match, "all" for all matches
     * @param searchString
     *            the string to search for
     * @return path(s) to the value
     */
    public static JsonValueExpression jsonSearch(Expression<?> jsonDoc, String oneOrAll, String searchString) {
        return JsonValueExpression.search(jsonDoc, oneOrAll, searchString);
    }

    /**
     * Returns the path(s) to occurrences of a string, with custom escape character.
     *
     * <p>
     * SQL:
     * {@code JSON_SEARCH(json_doc, one_or_all, search_str, escape_char, path, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param oneOrAll
     *            "one" for first match, "all" for all matches
     * @param searchString
     *            the string to search for
     * @param escapeChar
     *            the escape character for wildcards
     * @param paths
     *            optional paths to search within
     * @return path(s) to the value
     */
    public static JsonValueExpression jsonSearch(Expression<?> jsonDoc,
            String oneOrAll,
            String searchString,
            String escapeChar,
            String... paths) {
        return JsonValueExpression.search(jsonDoc, oneOrAll, searchString, escapeChar, paths);
    }

    // ========================================
    // JSON_OVERLAPS - Test overlap (MySQL 8.0.17+)
    // ========================================

    /**
     * Tests whether two JSON documents have any key-value pairs or array elements
     * in common.
     *
     * <p>
     * SQL: {@code JSON_OVERLAPS(json_doc1, json_doc2)}
     * </p>
     *
     * <p>
     * Available in MySQL 8.0.17+
     * </p>
     *
     * @param jsonDoc1
     *            the first JSON document
     * @param jsonDoc2
     *            the second JSON document
     * @return boolean expression
     */
    public static BooleanExpression jsonOverlaps(Expression<?> jsonDoc1, Expression<?> jsonDoc2) {
        return Expressions.booleanTemplate("json_overlaps({0}, {1})", jsonDoc1, jsonDoc2);
    }

    /**
     * Tests whether a JSON document overlaps with a JSON string literal.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param jsonLiteral
     *            the JSON literal string
     * @return boolean expression
     */
    public static BooleanExpression jsonOverlaps(Expression<?> jsonDoc, String jsonLiteral) {
        return Expressions.booleanTemplate("json_overlaps({0}, {1})", jsonDoc, Expressions.constant(jsonLiteral));
    }

    // ========================================
    // MEMBER OF - Test array membership (MySQL 8.0.17+)
    // ========================================

    /**
     * Tests whether a value is a member of a JSON array.
     *
     * <p>
     * SQL: {@code value MEMBER OF(json_array)}
     * </p>
     *
     * <p>
     * Available in MySQL 8.0.17+
     * </p>
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * BooleanExpression isAdmin = memberOf("admin", user.roles);
     * // Check if "admin" is in the roles array
     * }</pre>
     *
     * @param value
     *            the value to test
     * @param jsonArray
     *            the JSON array expression
     * @return boolean expression
     */
    public static BooleanExpression memberOf(Object value, Expression<?> jsonArray) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return Expressions.booleanTemplate("{0} member of({1})", valueExpr, jsonArray);
    }

    /**
     * Tests whether an expression value is a member of a JSON array.
     *
     * @param valueExpr
     *            the value expression to test
     * @param jsonArray
     *            the JSON array expression
     * @return boolean expression
     */
    public static BooleanExpression memberOf(Expression<?> valueExpr, Expression<?> jsonArray) {
        return Expressions.booleanTemplate("{0} member of({1})", valueExpr, jsonArray);
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Tests whether a JSON document is empty (array or object with no elements).
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return boolean expression
     * @deprecated Use {@link JsonAttributeFunctions#isEmpty(Expression)} instead.
     *             This method is duplicated and will be removed in a future version.
     * @since 0.1.0-Dev.1
     */
    @Deprecated(since = "0.1.0", forRemoval = true)
    public static BooleanExpression jsonIsEmpty(Expression<?> jsonDoc) {
        return JsonAttributeFunctions.isEmpty(jsonDoc);
    }

    /**
     * Returns the length (number of elements) of a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return length as number expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc) {
        return Expressions.numberTemplate(Integer.class, "json_length({0})", jsonDoc);
    }

    /**
     * Returns the length of a JSON value at a specific path.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return length as number expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc, String path) {
        return Expressions.numberTemplate(Integer.class, "json_length({0}, {1})", jsonDoc, Expressions.constant(path));
    }
}
