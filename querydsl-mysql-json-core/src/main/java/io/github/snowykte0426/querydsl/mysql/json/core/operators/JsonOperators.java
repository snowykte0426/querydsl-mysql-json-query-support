package io.github.snowykte0426.querydsl.mysql.json.core.operators;

import com.querydsl.core.types.Operator;
import org.jetbrains.annotations.NotNull;

/**
 * MySQL JSON function operators.
 * <p>
 * This enum defines all 35 MySQL JSON function operators for use with QueryDSL.
 * These operators map to MySQL 8.0.17+ JSON functions and are used with
 * QueryDSL templates.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public enum JsonOperators implements Operator {

    // ========================================
    // Creation Functions (3)
    // ========================================

    /**
     * JSON_ARRAY() - Creates a JSON array from values
     * <p>
     * Usage: JSON_ARRAY(val1, val2, ...)
     */
    JSON_ARRAY,

    /**
     * JSON_OBJECT() - Creates a JSON object from key-value pairs
     * <p>
     * Usage: JSON_OBJECT(key1, val1, key2, val2, ...)
     */
    JSON_OBJECT,

    /**
     * JSON_QUOTE() - Quotes a string as a JSON value
     * <p>
     * Usage: JSON_QUOTE(string)
     */
    JSON_QUOTE,

    // ========================================
    // Search Functions (10)
    // ========================================

    /**
     * JSON_EXTRACT() - Extracts data from JSON document
     * <p>
     * Usage: JSON_EXTRACT(json_doc, path)
     */
    JSON_EXTRACT,

    /**
     * JSON_VALUE() - Extracts a scalar value from JSON document (MySQL 8.0.21+)
     * <p>
     * Usage: JSON_VALUE(json_doc, path)
     */
    JSON_VALUE,

    /**
     * -> operator - Alias for JSON_EXTRACT
     * <p>
     * Usage: json_doc->path
     */
    JSON_EXTRACT_OP,

    /**
     * ->> operator - Extracts and unquotes value
     * <p>
     * Usage: json_doc->>path
     */
    JSON_UNQUOTE_EXTRACT_OP,

    /**
     * JSON_CONTAINS() - Whether JSON document contains value
     * <p>
     * Usage: JSON_CONTAINS(json_doc, val[, path])
     */
    JSON_CONTAINS,

    /**
     * JSON_CONTAINS_PATH() - Whether JSON document contains path
     * <p>
     * Usage: JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)
     */
    JSON_CONTAINS_PATH,

    /**
     * JSON_KEYS() - Returns keys from JSON object
     * <p>
     * Usage: JSON_KEYS(json_doc[, path])
     */
    JSON_KEYS,

    /**
     * JSON_SEARCH() - Path to value within JSON document
     * <p>
     * Usage: JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path]
     * ...])
     */
    JSON_SEARCH,

    /**
     * JSON_OVERLAPS() - Compares two JSON documents (MySQL 8.0.17+)
     * <p>
     * Usage: JSON_OVERLAPS(json_doc1, json_doc2)
     */
    JSON_OVERLAPS,

    /**
     * MEMBER OF() - Tests if value is member of JSON array (MySQL 8.0.17+)
     * <p>
     * Usage: value MEMBER OF(json_array)
     */
    MEMBER_OF,

    // ========================================
    // Modification Functions (10)
    // ========================================

    /**
     * JSON_SET() - Inserts or updates data in JSON document
     * <p>
     * Usage: JSON_SET(json_doc, path, val[, path, val] ...)
     */
    JSON_SET,

    /**
     * JSON_INSERT() - Inserts data into JSON document
     * <p>
     * Usage: JSON_INSERT(json_doc, path, val[, path, val] ...)
     */
    JSON_INSERT,

    /**
     * JSON_REPLACE() - Replaces existing values in JSON document
     * <p>
     * Usage: JSON_REPLACE(json_doc, path, val[, path, val] ...)
     */
    JSON_REPLACE,

    /**
     * JSON_REMOVE() - Removes data from JSON document
     * <p>
     * Usage: JSON_REMOVE(json_doc, path[, path] ...)
     */
    JSON_REMOVE,

    /**
     * JSON_ARRAY_APPEND() - Appends values to end of JSON arrays
     * <p>
     * Usage: JSON_ARRAY_APPEND(json_doc, path, val[, path, val] ...)
     */
    JSON_ARRAY_APPEND,

    /**
     * JSON_ARRAY_INSERT() - Inserts into JSON array
     * <p>
     * Usage: JSON_ARRAY_INSERT(json_doc, path, val[, path, val] ...)
     */
    JSON_ARRAY_INSERT,

    /**
     * JSON_MERGE_PATCH() - Merges JSON documents (RFC 7386)
     * <p>
     * Usage: JSON_MERGE_PATCH(json_doc, json_doc[, json_doc] ...)
     */
    JSON_MERGE_PATCH,

    /**
     * JSON_MERGE_PRESERVE() - Merges JSON documents, preserving duplicate keys
     * <p>
     * Usage: JSON_MERGE_PRESERVE(json_doc, json_doc[, json_doc] ...)
     */
    JSON_MERGE_PRESERVE,

    /**
     * JSON_MERGE() - Deprecated alias for JSON_MERGE_PRESERVE
     * <p>
     * Usage: JSON_MERGE(json_doc, json_doc[, json_doc] ...)
     *
     * @deprecated This function is deprecated in MySQL 8.0.3 and may be removed in
     *             a future release. Use {@link #JSON_MERGE_PRESERVE} instead for
     *             preserving duplicate keys, or {@link #JSON_MERGE_PATCH} for RFC
     *             7386 compliant merging.
     * @see <a href=
     *      "https://dev.mysql.com/doc/refman/8.0/en/json-modification-functions.html#function_json-merge">MySQL
     *      Documentation</a>
     * @since 0.1.0-Dev.1
     */
    @Deprecated(since = "0.1.0", forRemoval = false)
    JSON_MERGE,

    /**
     * JSON_UNQUOTE() - Unquotes JSON value
     * <p>
     * Usage: JSON_UNQUOTE(json_val)
     */
    JSON_UNQUOTE,

    // ========================================
    // Attribute Functions (4)
    // ========================================

    /**
     * JSON_DEPTH() - Maximum depth of JSON document
     * <p>
     * Usage: JSON_DEPTH(json_doc)
     */
    JSON_DEPTH,

    /**
     * JSON_LENGTH() - Number of elements in JSON document
     * <p>
     * Usage: JSON_LENGTH(json_doc[, path])
     */
    JSON_LENGTH,

    /**
     * JSON_TYPE() - Type of JSON value
     * <p>
     * Usage: JSON_TYPE(json_val)
     */
    JSON_TYPE,

    /**
     * JSON_VALID() - Whether value is valid JSON
     * <p>
     * Usage: JSON_VALID(val)
     */
    JSON_VALID,

    // ========================================
    // Utility Functions (3)
    // ========================================

    /**
     * JSON_PRETTY() - Prints JSON document in readable format
     * <p>
     * Usage: JSON_PRETTY(json_doc)
     */
    JSON_PRETTY,

    /**
     * JSON_STORAGE_SIZE() - Space used for storage of binary representation
     * <p>
     * Usage: JSON_STORAGE_SIZE(json_doc)
     */
    JSON_STORAGE_SIZE,

    /**
     * JSON_STORAGE_FREE() - Freed space after partial update
     * <p>
     * Usage: JSON_STORAGE_FREE(json_doc)
     */
    JSON_STORAGE_FREE,

    // ========================================
    // Schema Validation Functions (2) - MySQL 8.0.17+
    // ========================================

    /**
     * JSON_SCHEMA_VALID() - Validates JSON document against schema (MySQL 8.0.17+)
     * <p>
     * Usage: JSON_SCHEMA_VALID(schema, document)
     */
    JSON_SCHEMA_VALID,

    /**
     * JSON_SCHEMA_VALIDATION_REPORT() - Returns validation report (MySQL 8.0.17+)
     * <p>
     * Usage: JSON_SCHEMA_VALIDATION_REPORT(schema, document)
     */
    JSON_SCHEMA_VALIDATION_REPORT,

    // ========================================
    // Aggregate Functions (2)
    // ========================================

    /**
     * JSON_ARRAYAGG() - Aggregates result set as single JSON array
     * <p>
     * Usage: JSON_ARRAYAGG(col_or_expr)
     */
    JSON_ARRAYAGG,

    /**
     * JSON_OBJECTAGG() - Aggregates result set as single JSON object
     * <p>
     * Usage: JSON_OBJECTAGG(key, value)
     */
    JSON_OBJECTAGG,

    // ========================================
    // Table Functions (1)
    // ========================================

    /**
     * JSON_TABLE() - Returns data from JSON expression as relational table
     * <p>
     * Usage: JSON_TABLE(expr, path COLUMNS (column_list) ) [AS] alias
     * <p>
     * Note: JSON_TABLE requires special handling due to its complex syntax
     */
    JSON_TABLE;

    @Override
    public @NotNull Class<?> getType() {
        return Object.class;
    }
}
