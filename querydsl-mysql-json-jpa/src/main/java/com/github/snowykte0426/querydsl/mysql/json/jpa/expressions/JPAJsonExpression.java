package com.github.snowykte0426.querydsl.mysql.json.jpa.expressions;

import com.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import com.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import com.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import com.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.*;
import org.jetbrains.annotations.Nullable;

/**
 * JPA-specific JSON expression wrapper that provides fluent method chaining
 * for JSON operations on JPA entity properties.
 *
 * <p>This class wraps a JPA entity path (typically a String column containing JSON)
 * and provides convenient methods for all MySQL JSON functions.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Given a JPA entity with a JSON column
 * @Entity
 * public class User {
 *     @Column(columnDefinition = "JSON")
 *     private String metadata;
 * }
 *
 * // Create a JSON expression wrapper
 * JPAJsonExpression jsonMetadata = JPAJsonExpression.of(QUser.user.metadata);
 *
 * // Use fluent API for queries
 * List<User> admins = queryFactory
 *     .selectFrom(user)
 *     .where(jsonMetadata.extract("$.role").eq("\"admin\""))
 *     .fetch();
 *
 * // Or use static method style
 * List<User> users = queryFactory
 *     .selectFrom(user)
 *     .where(JPAJsonExpression.of(user.settings).contains("\"dark\"", "$.theme"))
 *     .fetch();
 * }</pre>
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public class JPAJsonExpression extends SimpleExpression<String> {

    private static final long serialVersionUID = 1L;

    private final Expression<?> jsonDoc;

    /**
     * Creates a new JPAJsonExpression wrapping the given expression.
     *
     * @param jsonDoc the underlying JSON document expression
     */
    protected JPAJsonExpression(Expression<?> jsonDoc) {
        super(jsonDoc instanceof Path ? (Path<String>) jsonDoc : Expressions.stringPath("json_doc"));
        this.jsonDoc = jsonDoc;
    }

    /**
     * Creates a JPAJsonExpression from any expression.
     *
     * @param expression the expression (typically a JPA path)
     * @return JPAJsonExpression wrapper
     */
    public static JPAJsonExpression of(Expression<?> expression) {
        return new JPAJsonExpression(expression);
    }

    /**
     * Creates a JPAJsonExpression from a StringPath (Q-class property).
     *
     * @param path the string path
     * @return JPAJsonExpression wrapper
     */
    public static JPAJsonExpression of(StringPath path) {
        return new JPAJsonExpression(path);
    }

    /**
     * Gets the underlying JSON document expression.
     *
     * @return the wrapped expression
     */
    public Expression<?> getJsonDoc() {
        return jsonDoc;
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return jsonDoc.accept(v, context);
    }

    // ============================================================
    // Extraction Methods
    // ============================================================

    /**
     * Extracts data from this JSON document at the specified path.
     *
     * <p>SQL: {@code JSON_EXTRACT(json_doc, path)}
     *
     * @param path JSON path expression (e.g., "$.key")
     * @return extracted JSON expression
     */
    public JsonExpression<String> extract(String path) {
        return JsonExpression.jsonExtract(jsonDoc, path);
    }

    /**
     * Extracts data from this JSON document at multiple paths.
     *
     * @param paths JSON path expressions
     * @return extracted JSON expression
     */
    public JsonExpression<String> extract(String... paths) {
        return JsonExpression.jsonExtract(jsonDoc, paths);
    }

    /**
     * Extracts and unquotes a value from this JSON document.
     *
     * <p>SQL: {@code json_doc ->> path}
     *
     * @param path JSON path expression
     * @return unquoted string expression
     */
    public StringExpression extractUnquoted(String path) {
        return JsonExpression.jsonUnquoteExtract(jsonDoc, path);
    }

    /**
     * Extracts a scalar value from this JSON document.
     *
     * <p>SQL: {@code JSON_VALUE(json_doc, path)} (MySQL 8.0.21+)
     *
     * @param path JSON path expression
     * @return scalar value expression
     */
    public JsonValueExpression value(String path) {
        return JsonValueExpression.extract(jsonDoc, path);
    }

    // ============================================================
    // Search/Test Methods
    // ============================================================

    /**
     * Tests whether this JSON document contains a specific value.
     *
     * <p>SQL: {@code JSON_CONTAINS(json_doc, val)}
     *
     * @param value the value to search for
     * @return boolean expression
     */
    public BooleanExpression contains(String value) {
        return Expressions.booleanTemplate("json_contains({0}, {1})", jsonDoc, Expressions.constant(value));
    }

    /**
     * Tests whether this JSON document contains a value at a specific path.
     *
     * <p>SQL: {@code JSON_CONTAINS(json_doc, val, path)}
     *
     * @param value the value to search for
     * @param path the JSON path
     * @return boolean expression
     */
    public BooleanExpression contains(String value, String path) {
        return Expressions.booleanTemplate(
            "json_contains({0}, {1}, {2})",
            jsonDoc,
            Expressions.constant(value),
            Expressions.constant(path)
        );
    }

    /**
     * Tests whether this JSON document contains data at the specified paths.
     *
     * <p>SQL: {@code JSON_CONTAINS_PATH(json_doc, one_or_all, path, ...)}
     *
     * @param oneOrAll "one" or "all"
     * @param paths the paths to check
     * @return boolean expression
     */
    public BooleanExpression containsPath(String oneOrAll, String... paths) {
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

    /**
     * Returns the path to the first occurrence of a string.
     *
     * <p>SQL: {@code JSON_SEARCH(json_doc, 'one', search_str)}
     *
     * @param searchString the string to search for
     * @return path expression
     */
    public JsonValueExpression search(String searchString) {
        return JsonValueExpression.search(jsonDoc, "one", searchString);
    }

    /**
     * Returns the path(s) to occurrences of a string.
     *
     * @param oneOrAll "one" or "all"
     * @param searchString the string to search for
     * @return path expression
     */
    public JsonValueExpression search(String oneOrAll, String searchString) {
        return JsonValueExpression.search(jsonDoc, oneOrAll, searchString);
    }

    /**
     * Returns the keys from this JSON object.
     *
     * <p>SQL: {@code JSON_KEYS(json_doc)}
     *
     * @return JSON array of keys
     */
    public JsonArrayExpression keys() {
        return JsonArrayExpression.wrap(Expressions.stringTemplate("json_keys({0})", jsonDoc));
    }

    /**
     * Returns the keys from this JSON object at the specified path.
     *
     * @param path JSON path
     * @return JSON array of keys
     */
    public JsonArrayExpression keys(String path) {
        return JsonArrayExpression.wrap(
            Expressions.stringTemplate("json_keys({0}, {1})", jsonDoc, Expressions.constant(path))
        );
    }

    /**
     * Tests whether this JSON document overlaps with another.
     *
     * <p>SQL: {@code JSON_OVERLAPS(json_doc1, json_doc2)} (MySQL 8.0.17+)
     *
     * @param other the other JSON document
     * @return boolean expression
     */
    public BooleanExpression overlaps(Expression<?> other) {
        return Expressions.booleanTemplate("json_overlaps({0}, {1})", jsonDoc, other);
    }

    /**
     * Tests whether this JSON document overlaps with a JSON string literal.
     *
     * @param jsonLiteral the JSON literal
     * @return boolean expression
     */
    public BooleanExpression overlaps(String jsonLiteral) {
        return Expressions.booleanTemplate(
            "json_overlaps({0}, {1})",
            jsonDoc,
            Expressions.constant(jsonLiteral)
        );
    }

    // ============================================================
    // Modification Methods (return new expressions)
    // ============================================================

    /**
     * Creates a modified JSON document with the value set at the path.
     *
     * <p>SQL: {@code JSON_SET(json_doc, path, val)}
     *
     * @param path the JSON path
     * @param value the value to set
     * @return modified JSON expression
     */
    public JsonValueExpression set(String path, Object value) {
        return JsonValueExpression.set(jsonDoc, path, value);
    }

    /**
     * Creates a modified JSON document with the value inserted at the path.
     *
     * <p>SQL: {@code JSON_INSERT(json_doc, path, val)}
     *
     * @param path the JSON path
     * @param value the value to insert
     * @return modified JSON expression
     */
    public JsonValueExpression insert(String path, Object value) {
        return JsonValueExpression.insert(jsonDoc, path, value);
    }

    /**
     * Creates a modified JSON document with the value replaced at the path.
     *
     * <p>SQL: {@code JSON_REPLACE(json_doc, path, val)}
     *
     * @param path the JSON path
     * @param value the new value
     * @return modified JSON expression
     */
    public JsonValueExpression replace(String path, Object value) {
        return JsonValueExpression.replace(jsonDoc, path, value);
    }

    /**
     * Creates a modified JSON document with the path removed.
     *
     * <p>SQL: {@code JSON_REMOVE(json_doc, path)}
     *
     * @param paths the paths to remove
     * @return modified JSON expression
     */
    public JsonValueExpression remove(String... paths) {
        return JsonValueExpression.remove(jsonDoc, paths);
    }

    /**
     * Appends a value to an array in this JSON document.
     *
     * <p>SQL: {@code JSON_ARRAY_APPEND(json_doc, path, val)}
     *
     * @param path the path to the array
     * @param value the value to append
     * @return modified JSON expression
     */
    public JsonArrayExpression arrayAppend(String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression
            ? (Expression<?>) value
            : Expressions.constant(value);

        return JsonArrayExpression.wrap(
            Expressions.stringTemplate(
                "json_array_append({0}, {1}, {2})",
                jsonDoc,
                Expressions.constant(path),
                valueExpr
            )
        );
    }

    /**
     * Inserts a value into an array in this JSON document.
     *
     * <p>SQL: {@code JSON_ARRAY_INSERT(json_doc, path, val)}
     *
     * @param path the path with array index
     * @param value the value to insert
     * @return modified JSON expression
     */
    public JsonArrayExpression arrayInsert(String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression
            ? (Expression<?>) value
            : Expressions.constant(value);

        return JsonArrayExpression.wrap(
            Expressions.stringTemplate(
                "json_array_insert({0}, {1}, {2})",
                jsonDoc,
                Expressions.constant(path),
                valueExpr
            )
        );
    }

    /**
     * Merges this JSON document with another using RFC 7386 semantics.
     *
     * <p>SQL: {@code JSON_MERGE_PATCH(json_doc1, json_doc2)}
     *
     * @param other the other JSON document
     * @return merged JSON expression
     */
    public JsonObjectExpression mergePatch(Expression<?> other) {
        return JsonObjectExpression.wrap(
            Expressions.stringTemplate("json_merge_patch({0}, {1})", jsonDoc, other)
        );
    }

    /**
     * Merges this JSON document with another, preserving duplicates.
     *
     * <p>SQL: {@code JSON_MERGE_PRESERVE(json_doc1, json_doc2)}
     *
     * @param other the other JSON document
     * @return merged JSON expression
     */
    public StringExpression mergePreserve(Expression<?> other) {
        return Expressions.stringTemplate("json_merge_preserve({0}, {1})", jsonDoc, other);
    }

    // ============================================================
    // Attribute Methods
    // ============================================================

    /**
     * Returns the maximum depth of this JSON document.
     *
     * <p>SQL: {@code JSON_DEPTH(json_doc)}
     *
     * @return depth as integer expression
     */
    public NumberExpression<Integer> depth() {
        return Expressions.numberTemplate(Integer.class, "json_depth({0})", jsonDoc);
    }

    /**
     * Returns the number of elements in this JSON document.
     *
     * <p>SQL: {@code JSON_LENGTH(json_doc)}
     *
     * @return length as integer expression
     */
    public NumberExpression<Integer> length() {
        return Expressions.numberTemplate(Integer.class, "json_length({0})", jsonDoc);
    }

    /**
     * Returns the number of elements at the specified path.
     *
     * @param path the JSON path
     * @return length as integer expression
     */
    public NumberExpression<Integer> length(String path) {
        return Expressions.numberTemplate(
            Integer.class,
            "json_length({0}, {1})",
            jsonDoc,
            Expressions.constant(path)
        );
    }

    /**
     * Returns the type of this JSON value.
     *
     * <p>SQL: {@code JSON_TYPE(json_val)}
     *
     * @return type as string expression
     */
    public StringExpression type() {
        return Expressions.stringTemplate("json_type({0})", jsonDoc);
    }

    /**
     * Tests whether this value is valid JSON.
     *
     * <p>SQL: {@code JSON_VALID(val)}
     *
     * @return boolean expression
     */
    public BooleanExpression isValid() {
        return Expressions.booleanTemplate("json_valid({0})", jsonDoc);
    }

    /**
     * Tests whether this JSON document is empty.
     *
     * @return boolean expression
     */
    public BooleanExpression isEmpty() {
        return Expressions.booleanTemplate("json_length({0}) = 0", jsonDoc);
    }

    /**
     * Tests whether this JSON document is not empty.
     *
     * @return boolean expression
     */
    public BooleanExpression isNotEmpty() {
        return Expressions.booleanTemplate("json_length({0}) > 0", jsonDoc);
    }

    /**
     * Tests whether this JSON value is an array.
     *
     * @return boolean expression
     */
    public BooleanExpression isArray() {
        return Expressions.booleanTemplate("json_type({0}) = 'ARRAY'", jsonDoc);
    }

    /**
     * Tests whether this JSON value is an object.
     *
     * @return boolean expression
     */
    public BooleanExpression isObject() {
        return Expressions.booleanTemplate("json_type({0}) = 'OBJECT'", jsonDoc);
    }

    /**
     * Tests whether this JSON value is a scalar.
     *
     * @return boolean expression
     */
    public BooleanExpression isScalar() {
        return Expressions.booleanTemplate("json_type({0}) NOT IN ('ARRAY', 'OBJECT')", jsonDoc);
    }

    // ============================================================
    // Utility Methods
    // ============================================================

    /**
     * Formats this JSON document in a human-readable format.
     *
     * <p>SQL: {@code JSON_PRETTY(json_val)}
     *
     * @return formatted JSON string
     */
    public StringExpression pretty() {
        return Expressions.stringTemplate("json_pretty({0})", jsonDoc);
    }

    /**
     * Returns the storage size of this JSON document in bytes.
     *
     * <p>SQL: {@code JSON_STORAGE_SIZE(json_val)}
     *
     * @return storage size in bytes
     */
    public NumberExpression<Integer> storageSize() {
        return Expressions.numberTemplate(Integer.class, "json_storage_size({0})", jsonDoc);
    }

    /**
     * Returns the freed space after a partial update.
     *
     * <p>SQL: {@code JSON_STORAGE_FREE(json_val)}
     *
     * @return freed space in bytes
     */
    public NumberExpression<Integer> storageFree() {
        return Expressions.numberTemplate(Integer.class, "json_storage_free({0})", jsonDoc);
    }

    /**
     * Unquotes this JSON string value.
     *
     * <p>SQL: {@code JSON_UNQUOTE(json_val)}
     *
     * @return unquoted string expression
     */
    public StringExpression unquote() {
        return Expressions.stringTemplate("json_unquote({0})", jsonDoc);
    }

    // ============================================================
    // Schema Validation Methods (MySQL 8.0.17+)
    // ============================================================

    /**
     * Validates this JSON document against a schema.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALID(schema, json_doc)}
     *
     * @param schema the JSON schema expression
     * @return boolean expression
     */
    public BooleanExpression schemaValid(Expression<?> schema) {
        return Expressions.booleanTemplate("json_schema_valid({0}, {1})", schema, jsonDoc);
    }

    /**
     * Validates this JSON document against a schema string.
     *
     * @param schemaJson the JSON schema as string
     * @return boolean expression
     */
    public BooleanExpression schemaValid(String schemaJson) {
        return Expressions.booleanTemplate(
            "json_schema_valid({0}, {1})",
            Expressions.constant(schemaJson),
            jsonDoc
        );
    }

    /**
     * Returns a validation report for this JSON document.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALIDATION_REPORT(schema, json_doc)}
     *
     * @param schema the JSON schema expression
     * @return validation report as string
     */
    public StringExpression schemaValidationReport(Expression<?> schema) {
        return Expressions.stringTemplate("json_schema_validation_report({0}, {1})", schema, jsonDoc);
    }

    /**
     * Returns a validation report for this JSON document.
     *
     * @param schemaJson the JSON schema as string
     * @return validation report as string
     */
    public StringExpression schemaValidationReport(String schemaJson) {
        return Expressions.stringTemplate(
            "json_schema_validation_report({0}, {1})",
            Expressions.constant(schemaJson),
            jsonDoc
        );
    }
}
