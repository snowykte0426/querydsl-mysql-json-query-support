package io.github.snowykte0426.querydsl.mysql.json.sql;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonTableExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.functions.*;
import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonTableColumn;

import java.util.Map;

/**
 * Single entry point for all MySQL JSON functions in SQL environment.
 *
 * <p>This class provides convenient access to all 35 MySQL JSON functions
 * for use with QueryDSL SQL. All methods delegate to the core module implementations.
 *
 * <p>Example usage in SQL queries:
 * <pre>{@code
 * // Configure with MySQLJsonTemplates
 * Configuration config = new Configuration(MySQLJsonTemplates.DEFAULT);
 * SQLQueryFactory queryFactory = new SQLQueryFactory(config, dataSource);
 *
 * QUser user = QUser.user;
 * List<Tuple> admins = queryFactory
 *     .select(user.name, user.email)
 *     .from(user)
 *     .where(SqlJsonFunctions.jsonExtract(user.metadata, "$.role").eq("\"admin\""))
 *     .fetch();
 * }</pre>
 *
 * <h2>Function Categories</h2>
 * <ul>
 *   <li><b>Creation Functions (3)</b>: jsonArray, jsonObject, jsonQuote</li>
 *   <li><b>Search Functions (10)</b>: jsonExtract, jsonContains, jsonSearch, etc.</li>
 *   <li><b>Modification Functions (9)</b>: jsonSet, jsonInsert, jsonRemove, etc.</li>
 *   <li><b>Attribute Functions (4)</b>: jsonDepth, jsonLength, jsonType, jsonValid</li>
 *   <li><b>Utility Functions (3)</b>: jsonPretty, jsonStorageSize, jsonStorageFree</li>
 *   <li><b>Schema Functions (2)</b>: jsonSchemaValid, jsonSchemaValidationReport</li>
 *   <li><b>Aggregate Functions (2)</b>: jsonArrayAgg, jsonObjectAgg</li>
 *   <li><b>Table Functions (1)</b>: jsonTable</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.2
 * @see JsonCreationFunctions
 * @see JsonSearchFunctions
 * @see JsonModifyFunctions
 * @see JsonAttributeFunctions
 * @see JsonUtilityFunctions
 * @see JsonSchemaFunctions
 * @see JsonAggregateFunctions
 * @see JsonTableFunctions
 */
public final class SqlJsonFunctions {

    private SqlJsonFunctions() {
        // Utility class - prevent instantiation
    }

    // ============================================================
    // Creation Functions (JSON_ARRAY, JSON_OBJECT, JSON_QUOTE)
    // ============================================================

    /**
     * Creates a JSON array from the given values.
     *
     * <p>SQL: {@code JSON_ARRAY(val1, val2, ...)}
     *
     * @param values the array values
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression jsonArray(Object... values) {
        return JsonCreationFunctions.jsonArray(values);
    }

    /**
     * Creates a JSON array from expressions.
     *
     * @param expressions the value expressions
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression jsonArray(Expression<?>... expressions) {
        return JsonCreationFunctions.jsonArray(expressions);
    }

    /**
     * Creates an empty JSON array.
     *
     * @return empty JsonArrayExpression
     */
    public static JsonArrayExpression emptyJsonArray() {
        return JsonCreationFunctions.emptyJsonArray();
    }

    /**
     * Creates a JSON object from key-value pairs.
     *
     * <p>SQL: {@code JSON_OBJECT(key1, val1, key2, val2, ...)}
     *
     * @param keyValuePairs alternating keys and values
     * @return JsonObjectExpression
     */
    public static JsonObjectExpression jsonObject(Object... keyValuePairs) {
        return JsonCreationFunctions.jsonObject(keyValuePairs);
    }

    /**
     * Creates an empty JSON object.
     *
     * @return empty JsonObjectExpression
     */
    public static JsonObjectExpression emptyJsonObject() {
        return JsonCreationFunctions.emptyJsonObject();
    }

    /**
     * Returns a builder for creating JSON objects fluently.
     *
     * @return JsonObjectBuilder
     */
    public static JsonObjectExpression.JsonObjectBuilder jsonObjectBuilder() {
        return JsonCreationFunctions.jsonObjectBuilder();
    }

    /**
     * Quotes a string as a JSON value.
     *
     * <p>SQL: {@code JSON_QUOTE(string)}
     *
     * @param value the string to quote
     * @return JsonValueExpression
     */
    public static JsonValueExpression jsonQuote(String value) {
        return JsonCreationFunctions.jsonQuote(value);
    }

    /**
     * Quotes a string expression as a JSON value.
     *
     * @param expression the expression to quote
     * @return JsonValueExpression
     */
    public static JsonValueExpression jsonQuote(Expression<String> expression) {
        return JsonCreationFunctions.jsonQuote(expression);
    }

    /**
     * Creates a JSON array from a Java collection.
     *
     * @param collection the collection to convert
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression jsonArrayFrom(Iterable<?> collection) {
        return JsonCreationFunctions.jsonArrayFrom(collection);
    }

    /**
     * Creates a JSON object from a Java Map.
     *
     * @param map the map to convert
     * @return JsonObjectExpression
     */
    public static JsonObjectExpression jsonObjectFrom(Map<String, ?> map) {
        return JsonCreationFunctions.jsonObjectFrom(map);
    }

    /**
     * Creates a JSON null value.
     *
     * @return Expression representing JSON null
     */
    public static StringExpression jsonNull() {
        return JsonCreationFunctions.jsonNull();
    }

    // ============================================================
    // Search Functions (JSON_EXTRACT, JSON_CONTAINS, etc.)
    // ============================================================

    /**
     * Extracts data from a JSON document using a path expression.
     *
     * <p>SQL: {@code JSON_EXTRACT(json_doc, path)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return extracted JSON expression
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, path);
    }

    /**
     * Extracts data from a JSON document using multiple paths.
     *
     * @param jsonDoc the JSON document expression
     * @param paths the JSON paths
     * @return extracted JSON expression
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String... paths) {
        return JsonSearchFunctions.jsonExtract(jsonDoc, paths);
    }

    /**
     * Extracts and unquotes a value from a JSON document.
     *
     * <p>SQL: {@code json_doc ->> path}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return unquoted string expression
     */
    public static StringExpression jsonUnquoteExtract(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonUnquoteExtract(jsonDoc, path);
    }

    /**
     * Extracts a scalar value from a JSON document.
     *
     * <p>SQL: {@code JSON_VALUE(json_doc, path)} (MySQL 8.0.21+)
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return scalar value expression
     */
    public static JsonValueExpression jsonValue(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonValue(jsonDoc, path);
    }

    /**
     * Tests whether a JSON document contains a specific value.
     *
     * <p>SQL: {@code JSON_CONTAINS(json_doc, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param value the value to search for
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, String value) {
        return JsonSearchFunctions.jsonContains(jsonDoc, value);
    }

    /**
     * Tests whether a JSON document contains a value at a specific path.
     *
     * @param jsonDoc the JSON document expression
     * @param value the value to search for
     * @param path the JSON path
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, String value, String path) {
        return JsonSearchFunctions.jsonContains(jsonDoc, value, path);
    }

    /**
     * Tests whether a JSON document contains an expression value.
     *
     * @param jsonDoc the JSON document expression
     * @param value the value expression
     * @return boolean expression
     */
    public static BooleanExpression jsonContains(Expression<?> jsonDoc, Expression<?> value) {
        return JsonSearchFunctions.jsonContains(jsonDoc, value);
    }

    /**
     * Tests whether a JSON document contains data at the specified paths.
     *
     * <p>SQL: {@code JSON_CONTAINS_PATH(json_doc, one_or_all, path, ...)}
     *
     * @param jsonDoc the JSON document expression
     * @param oneOrAll "one" to match any path, "all" to match all paths
     * @param paths the paths to check
     * @return boolean expression
     */
    public static BooleanExpression jsonContainsPath(Expression<?> jsonDoc, String oneOrAll, String... paths) {
        return JsonSearchFunctions.jsonContainsPath(jsonDoc, oneOrAll, paths);
    }

    /**
     * Returns the keys from a JSON object.
     *
     * <p>SQL: {@code JSON_KEYS(json_doc)}
     *
     * @param jsonDoc the JSON document expression
     * @return JSON array of keys
     */
    public static JsonArrayExpression jsonKeys(Expression<?> jsonDoc) {
        return JsonSearchFunctions.jsonKeys(jsonDoc);
    }

    /**
     * Returns the keys from a JSON object at the specified path.
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return JSON array of keys
     */
    public static JsonArrayExpression jsonKeys(Expression<?> jsonDoc, String path) {
        return JsonSearchFunctions.jsonKeys(jsonDoc, path);
    }

    /**
     * Returns the path to the first occurrence of a string within a JSON document.
     *
     * <p>SQL: {@code JSON_SEARCH(json_doc, 'one', search_str)}
     *
     * @param jsonDoc the JSON document expression
     * @param searchString the string to search for
     * @return path expression
     */
    public static JsonValueExpression jsonSearch(Expression<?> jsonDoc, String searchString) {
        return JsonSearchFunctions.jsonSearch(jsonDoc, searchString);
    }

    /**
     * Returns the path(s) to occurrences of a string within a JSON document.
     *
     * @param jsonDoc the JSON document expression
     * @param oneOrAll "one" for first match, "all" for all matches
     * @param searchString the string to search for
     * @return path expression
     */
    public static JsonValueExpression jsonSearch(Expression<?> jsonDoc, String oneOrAll, String searchString) {
        return JsonSearchFunctions.jsonSearch(jsonDoc, oneOrAll, searchString);
    }

    /**
     * Tests whether two JSON documents have any elements in common.
     *
     * <p>SQL: {@code JSON_OVERLAPS(json_doc1, json_doc2)} (MySQL 8.0.17+)
     *
     * @param jsonDoc1 the first JSON document
     * @param jsonDoc2 the second JSON document
     * @return boolean expression
     */
    public static BooleanExpression jsonOverlaps(Expression<?> jsonDoc1, Expression<?> jsonDoc2) {
        return JsonSearchFunctions.jsonOverlaps(jsonDoc1, jsonDoc2);
    }

    /**
     * Tests whether a JSON document overlaps with a JSON string literal.
     *
     * @param jsonDoc the JSON document expression
     * @param jsonLiteral the JSON literal string
     * @return boolean expression
     */
    public static BooleanExpression jsonOverlaps(Expression<?> jsonDoc, String jsonLiteral) {
        return JsonSearchFunctions.jsonOverlaps(jsonDoc, jsonLiteral);
    }

    /**
     * Tests whether a value is a member of a JSON array.
     *
     * <p>SQL: {@code value MEMBER OF(json_array)} (MySQL 8.0.17+)
     *
     * @param value the value to test
     * @param jsonArray the JSON array expression
     * @return boolean expression
     */
    public static BooleanExpression memberOf(Object value, Expression<?> jsonArray) {
        return JsonSearchFunctions.memberOf(value, jsonArray);
    }

    /**
     * Tests whether an expression value is a member of a JSON array.
     *
     * @param valueExpr the value expression
     * @param jsonArray the JSON array expression
     * @return boolean expression
     */
    public static BooleanExpression memberOf(Expression<?> valueExpr, Expression<?> jsonArray) {
        return JsonSearchFunctions.memberOf(valueExpr, jsonArray);
    }

    /**
     * Tests whether a JSON document is empty.
     *
     * @param jsonDoc the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression jsonIsEmpty(Expression<?> jsonDoc) {
        return JsonSearchFunctions.jsonIsEmpty(jsonDoc);
    }

    // ============================================================
    // Modification Functions (JSON_SET, JSON_INSERT, etc.)
    // ============================================================

    /**
     * Inserts or updates data in a JSON document at the specified path.
     *
     * <p>SQL: {@code JSON_SET(json_doc, path, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @param value the value to set
     * @return modified JSON expression
     */
    public static JsonValueExpression jsonSet(Expression<?> jsonDoc, String path, Object value) {
        return JsonModifyFunctions.jsonSet(jsonDoc, path, value);
    }

    /**
     * Inserts or updates multiple values in a JSON document.
     *
     * @param jsonDoc the JSON document expression
     * @param pathsAndValues alternating paths and values
     * @return modified JSON expression
     */
    public static StringExpression jsonSet(Expression<?> jsonDoc, Object... pathsAndValues) {
        return JsonModifyFunctions.jsonSet(jsonDoc, pathsAndValues);
    }

    /**
     * Inserts data into a JSON document without replacing existing values.
     *
     * <p>SQL: {@code JSON_INSERT(json_doc, path, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @param value the value to insert
     * @return modified JSON expression
     */
    public static JsonValueExpression jsonInsert(Expression<?> jsonDoc, String path, Object value) {
        return JsonModifyFunctions.jsonInsert(jsonDoc, path, value);
    }

    /**
     * Inserts multiple values into a JSON document.
     *
     * @param jsonDoc the JSON document expression
     * @param pathsAndValues alternating paths and values
     * @return modified JSON expression
     */
    public static StringExpression jsonInsert(Expression<?> jsonDoc, Object... pathsAndValues) {
        return JsonModifyFunctions.jsonInsert(jsonDoc, pathsAndValues);
    }

    /**
     * Replaces existing values in a JSON document.
     *
     * <p>SQL: {@code JSON_REPLACE(json_doc, path, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @param value the new value
     * @return modified JSON expression
     */
    public static JsonValueExpression jsonReplace(Expression<?> jsonDoc, String path, Object value) {
        return JsonModifyFunctions.jsonReplace(jsonDoc, path, value);
    }

    /**
     * Replaces multiple values in a JSON document.
     *
     * @param jsonDoc the JSON document expression
     * @param pathsAndValues alternating paths and values
     * @return modified JSON expression
     */
    public static StringExpression jsonReplace(Expression<?> jsonDoc, Object... pathsAndValues) {
        return JsonModifyFunctions.jsonReplace(jsonDoc, pathsAndValues);
    }

    /**
     * Removes data from a JSON document at the specified path.
     *
     * <p>SQL: {@code JSON_REMOVE(json_doc, path)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return modified JSON expression
     */
    public static StringExpression jsonRemove(Expression<?> jsonDoc, String path) {
        return JsonModifyFunctions.jsonRemove(jsonDoc, path);
    }

    /**
     * Removes data from multiple paths in a JSON document.
     *
     * @param jsonDoc the JSON document expression
     * @param paths the paths to remove
     * @return modified JSON expression
     */
    public static JsonValueExpression jsonRemove(Expression<?> jsonDoc, String... paths) {
        return JsonModifyFunctions.jsonRemove(jsonDoc, paths);
    }

    /**
     * Appends a value to a JSON array.
     *
     * <p>SQL: {@code JSON_ARRAY_APPEND(json_doc, path, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the path to the array
     * @param value the value to append
     * @return modified JSON expression
     */
    public static JsonArrayExpression jsonArrayAppend(Expression<?> jsonDoc, String path, Object value) {
        return JsonModifyFunctions.jsonArrayAppend(jsonDoc, path, value);
    }

    /**
     * Appends multiple values to JSON arrays.
     *
     * @param jsonDoc the JSON document expression
     * @param pathsAndValues alternating paths and values
     * @return modified JSON expression
     */
    public static JsonArrayExpression jsonArrayAppend(Expression<?> jsonDoc, Object... pathsAndValues) {
        return JsonModifyFunctions.jsonArrayAppend(jsonDoc, pathsAndValues);
    }

    /**
     * Inserts a value into a JSON array at a specific position.
     *
     * <p>SQL: {@code JSON_ARRAY_INSERT(json_doc, path, val)}
     *
     * @param jsonDoc the JSON document expression
     * @param path the path with array index
     * @param value the value to insert
     * @return modified JSON expression
     */
    public static JsonArrayExpression jsonArrayInsert(Expression<?> jsonDoc, String path, Object value) {
        return JsonModifyFunctions.jsonArrayInsert(jsonDoc, path, value);
    }

    /**
     * Merges JSON documents using RFC 7386 merge patch semantics.
     *
     * <p>SQL: {@code JSON_MERGE_PATCH(json_doc1, json_doc2, ...)}
     *
     * @param jsonDocs the JSON documents to merge
     * @return merged JSON expression
     */
    public static JsonObjectExpression jsonMergePatch(Expression<?>... jsonDocs) {
        return JsonModifyFunctions.jsonMergePatch(jsonDocs);
    }

    /**
     * Merges JSON document expressions and string literals.
     *
     * @param first the first JSON document
     * @param others additional documents
     * @return merged JSON expression
     */
    public static StringExpression jsonMergePatch(Expression<?> first, Object... others) {
        return JsonModifyFunctions.jsonMergePatch(first, others);
    }

    /**
     * Merges JSON documents, preserving duplicate keys as arrays.
     *
     * <p>SQL: {@code JSON_MERGE_PRESERVE(json_doc1, json_doc2, ...)}
     *
     * @param jsonDocs the JSON documents to merge
     * @return merged JSON expression
     */
    public static StringExpression jsonMergePreserve(Expression<?>... jsonDocs) {
        return JsonModifyFunctions.jsonMergePreserve(jsonDocs);
    }

    /**
     * Unquotes a JSON string value.
     *
     * <p>SQL: {@code JSON_UNQUOTE(json_val)}
     *
     * @param jsonValue the JSON value expression
     * @return unquoted string expression
     */
    public static StringExpression jsonUnquote(Expression<?> jsonValue) {
        return JsonModifyFunctions.jsonUnquote(jsonValue);
    }

    /**
     * Unquotes a JSON string literal.
     *
     * @param jsonString the JSON string literal
     * @return unquoted string expression
     */
    public static StringExpression jsonUnquote(String jsonString) {
        return JsonModifyFunctions.jsonUnquote(jsonString);
    }

    // ============================================================
    // Attribute Functions (JSON_DEPTH, JSON_LENGTH, etc.)
    // ============================================================

    /**
     * Returns the maximum depth of a JSON document.
     *
     * <p>SQL: {@code JSON_DEPTH(json_doc)}
     *
     * @param jsonDoc the JSON document expression
     * @return depth as integer expression
     */
    public static NumberExpression<Integer> jsonDepth(Expression<?> jsonDoc) {
        return JsonAttributeFunctions.jsonDepth(jsonDoc);
    }

    /**
     * Returns the number of elements in a JSON document.
     *
     * <p>SQL: {@code JSON_LENGTH(json_doc)}
     *
     * @param jsonDoc the JSON document expression
     * @return length as integer expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc) {
        return JsonAttributeFunctions.jsonLength(jsonDoc);
    }

    /**
     * Returns the number of elements at a specific path.
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return length as integer expression
     */
    public static NumberExpression<Integer> jsonLength(Expression<?> jsonDoc, String path) {
        return JsonAttributeFunctions.jsonLength(jsonDoc, path);
    }

    /**
     * Returns the type of a JSON value as a string.
     *
     * <p>SQL: {@code JSON_TYPE(json_val)}
     *
     * @param jsonValue the JSON value expression
     * @return type as string expression
     */
    public static StringExpression jsonType(Expression<?> jsonValue) {
        return JsonAttributeFunctions.jsonType(jsonValue);
    }

    /**
     * Tests whether a value is valid JSON.
     *
     * <p>SQL: {@code JSON_VALID(val)}
     *
     * @param value the value to validate
     * @return boolean expression
     */
    public static BooleanExpression jsonValid(Expression<?> value) {
        return JsonAttributeFunctions.jsonValid(value);
    }

    /**
     * Tests whether a string literal is valid JSON.
     *
     * @param jsonString the string to validate
     * @return boolean expression
     */
    public static BooleanExpression jsonValid(String jsonString) {
        return JsonAttributeFunctions.jsonValid(jsonString);
    }

    /**
     * Tests whether a JSON document is empty.
     *
     * @param jsonDoc the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression isEmpty(Expression<?> jsonDoc) {
        return JsonAttributeFunctions.isEmpty(jsonDoc);
    }

    /**
     * Tests whether a JSON document is not empty.
     *
     * @param jsonDoc the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression isNotEmpty(Expression<?> jsonDoc) {
        return JsonAttributeFunctions.isNotEmpty(jsonDoc);
    }

    /**
     * Tests whether a JSON value is an array.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isArray(Expression<?> jsonValue) {
        return JsonAttributeFunctions.isArray(jsonValue);
    }

    /**
     * Tests whether a JSON value is an object.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isObject(Expression<?> jsonValue) {
        return JsonAttributeFunctions.isObject(jsonValue);
    }

    /**
     * Tests whether a JSON value is a scalar.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isScalar(Expression<?> jsonValue) {
        return JsonAttributeFunctions.isScalar(jsonValue);
    }

    /**
     * Tests whether a JSON value is null.
     *
     * @param jsonValue the JSON value expression
     * @return boolean expression
     */
    public static BooleanExpression isJsonNull(Expression<?> jsonValue) {
        return JsonAttributeFunctions.isNull(jsonValue);
    }

    // ============================================================
    // Utility Functions (JSON_PRETTY, JSON_STORAGE_SIZE, etc.)
    // ============================================================

    /**
     * Formats a JSON document in a human-readable format.
     *
     * <p>SQL: {@code JSON_PRETTY(json_val)}
     *
     * @param jsonValue the JSON value expression
     * @return formatted JSON as string expression
     */
    public static StringExpression jsonPretty(Expression<?> jsonValue) {
        return JsonUtilityFunctions.jsonPretty(jsonValue);
    }

    /**
     * Formats a JSON string literal.
     *
     * @param jsonString the JSON string
     * @return formatted JSON as string expression
     */
    public static StringExpression jsonPretty(String jsonString) {
        return JsonUtilityFunctions.jsonPretty(jsonString);
    }

    /**
     * Returns the storage size of a JSON document in bytes.
     *
     * <p>SQL: {@code JSON_STORAGE_SIZE(json_val)}
     *
     * @param jsonValue the JSON value expression
     * @return storage size in bytes
     */
    public static NumberExpression<Integer> jsonStorageSize(Expression<?> jsonValue) {
        return JsonUtilityFunctions.jsonStorageSize(jsonValue);
    }

    /**
     * Returns the storage size of a JSON string literal.
     *
     * @param jsonString the JSON string
     * @return storage size in bytes
     */
    public static NumberExpression<Integer> jsonStorageSize(String jsonString) {
        return JsonUtilityFunctions.jsonStorageSize(jsonString);
    }

    /**
     * Returns the freed space after a partial JSON update.
     *
     * <p>SQL: {@code JSON_STORAGE_FREE(json_val)}
     *
     * @param jsonColumn the JSON column expression
     * @return freed space in bytes
     */
    public static NumberExpression<Integer> jsonStorageFree(Expression<?> jsonColumn) {
        return JsonUtilityFunctions.jsonStorageFree(jsonColumn);
    }

    // ============================================================
    // Schema Validation Functions (MySQL 8.0.17+)
    // ============================================================

    /**
     * Validates a JSON document against a JSON schema.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALID(schema, document)} (MySQL 8.0.17+)
     *
     * @param schema the JSON schema expression
     * @param document the JSON document to validate
     * @return boolean expression
     */
    public static BooleanExpression jsonSchemaValid(Expression<?> schema, Expression<?> document) {
        return JsonSchemaFunctions.jsonSchemaValid(schema, document);
    }

    /**
     * Validates a JSON document against a schema string literal.
     *
     * @param schemaJson the JSON schema as string
     * @param document the JSON document expression
     * @return boolean expression
     */
    public static BooleanExpression jsonSchemaValid(String schemaJson, Expression<?> document) {
        return JsonSchemaFunctions.jsonSchemaValid(schemaJson, document);
    }

    /**
     * Returns a detailed validation report.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALIDATION_REPORT(schema, document)} (MySQL 8.0.17+)
     *
     * @param schema the JSON schema expression
     * @param document the JSON document to validate
     * @return validation report as string expression
     */
    public static StringExpression jsonSchemaValidationReport(Expression<?> schema, Expression<?> document) {
        return JsonSchemaFunctions.jsonSchemaValidationReport(schema, document);
    }

    /**
     * Returns a validation report with schema as string literal.
     *
     * @param schemaJson the JSON schema as string
     * @param document the JSON document expression
     * @return validation report as string expression
     */
    public static StringExpression jsonSchemaValidationReport(String schemaJson, Expression<?> document) {
        return JsonSchemaFunctions.jsonSchemaValidationReport(schemaJson, document);
    }

    // ============================================================
    // Aggregate Functions (JSON_ARRAYAGG, JSON_OBJECTAGG)
    // ============================================================

    /**
     * Aggregates values from multiple rows into a JSON array.
     *
     * <p>SQL: {@code JSON_ARRAYAGG(value)}
     *
     * @param value the expression to aggregate
     * @return JSON array expression
     */
    public static JsonArrayExpression jsonArrayAgg(Expression<?> value) {
        return JsonAggregateFunctions.jsonArrayAgg(value);
    }

    /**
     * Aggregates key-value pairs into a JSON object.
     *
     * <p>SQL: {@code JSON_OBJECTAGG(key, value)}
     *
     * @param key the key expression
     * @param value the value expression
     * @return JSON object expression
     */
    public static JsonObjectExpression jsonObjectAgg(Expression<?> key, Expression<?> value) {
        return JsonAggregateFunctions.jsonObjectAgg(key, value);
    }

    /**
     * Aggregates key-value pairs with string literal key.
     *
     * @param key the key as string literal
     * @param value the value expression
     * @return JSON object expression
     */
    public static JsonObjectExpression jsonObjectAgg(String key, Expression<?> value) {
        return JsonAggregateFunctions.jsonObjectAgg(key, value);
    }

    /**
     * Alias for jsonArrayAgg.
     *
     * @param value the expression to aggregate
     * @return JSON array expression
     */
    public static JsonArrayExpression arrayAgg(Expression<?> value) {
        return JsonAggregateFunctions.arrayAgg(value);
    }

    /**
     * Alias for jsonObjectAgg.
     *
     * @param key the key expression
     * @param value the value expression
     * @return JSON object expression
     */
    public static JsonObjectExpression objectAgg(Expression<?> key, Expression<?> value) {
        return JsonAggregateFunctions.objectAgg(key, value);
    }

    // ============================================================
    // Table Functions (JSON_TABLE)
    // ============================================================

    /**
     * Creates a JSON_TABLE expression builder.
     *
     * <p>SQL: {@code JSON_TABLE(json_doc, path COLUMNS(...))}
     *
     * @return JsonTableExpression builder
     */
    public static JsonTableExpression.Builder jsonTable() {
        return JsonTableFunctions.jsonTable();
    }

    /**
     * Creates a JSON_TABLE expression builder with document and path.
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return JsonTableExpression builder
     */
    public static JsonTableExpression.Builder jsonTable(
            Expression<?> jsonDoc, String path) {
        return JsonTableFunctions.jsonTable(jsonDoc, path);
    }

    /**
     * Creates a JSON_TABLE expression builder with JSON string and path.
     *
     * @param jsonString the JSON string
     * @param path the JSON path
     * @return JsonTableExpression builder
     */
    public static JsonTableExpression.Builder jsonTable(
            String jsonString, String path) {
        return JsonTableFunctions.jsonTable(jsonString, path);
    }

    // ============================================================
    // JSON_TABLE Column Helpers
    // ============================================================

    /**
     * Creates a column definition for JSON_TABLE.
     *
     * @param columnName the column name
     * @param sqlType the SQL type
     * @param jsonPath the JSON path
     * @return column definition
     */
    public static JsonTableColumn column(
            String columnName, String sqlType, String jsonPath) {
        return JsonTableFunctions.column(columnName, sqlType, jsonPath);
    }

    /**
     * Creates an INT column for JSON_TABLE.
     *
     * @param columnName the column name
     * @param jsonPath the JSON path
     * @return column definition
     */
    public static JsonTableColumn intColumn(
            String columnName, String jsonPath) {
        return JsonTableFunctions.intColumn(columnName, jsonPath);
    }

    /**
     * Creates a VARCHAR column for JSON_TABLE.
     *
     * @param columnName the column name
     * @param length the maximum length
     * @param jsonPath the JSON path
     * @return column definition
     */
    public static JsonTableColumn varcharColumn(
            String columnName, int length, String jsonPath) {
        return JsonTableFunctions.varcharColumn(columnName, length, jsonPath);
    }

    /**
     * Creates a JSON column for JSON_TABLE.
     *
     * @param columnName the column name
     * @param jsonPath the JSON path
     * @return column definition
     */
    public static JsonTableColumn jsonColumn(
            String columnName, String jsonPath) {
        return JsonTableFunctions.jsonColumn(columnName, jsonPath);
    }

    /**
     * Creates an EXISTS column for JSON_TABLE.
     *
     * @param columnName the column name
     * @param jsonPath the JSON path
     * @return column definition
     */
    public static JsonTableColumn existsColumn(
            String columnName, String jsonPath) {
        return JsonTableFunctions.existsColumn(columnName, jsonPath);
    }

    /**
     * Creates an ordinality column for JSON_TABLE.
     *
     * @param columnName the column name
     * @return column definition
     */
    public static JsonTableColumn ordinalityColumn(
            String columnName) {
        return JsonTableFunctions.ordinalityColumn(columnName);
    }
}
