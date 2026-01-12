package com.github.snowykte0426.querydsl.mysql.json.core.operators;

import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Ops;

/**
 * MySQL JSON function operators.
 *
 * This class defines all 35 MySQL JSON function operators for use with QueryDSL.
 * These operators map to MySQL 8.0.17+ JSON functions.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public final class JsonOperators {

    private JsonOperators() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // Creation Functions (3)
    // ========================================

    /**
     * JSON_ARRAY() - Creates a JSON array from values
     * <p>Usage: JSON_ARRAY(val1, val2, ...)
     */
    public static final Operator JSON_ARRAY = Operator.create("JSON_ARRAY", Object[].class);

    /**
     * JSON_OBJECT() - Creates a JSON object from key-value pairs
     * <p>Usage: JSON_OBJECT(key1, val1, key2, val2, ...)
     */
    public static final Operator JSON_OBJECT = Operator.create("JSON_OBJECT", Object[].class);

    /**
     * JSON_QUOTE() - Quotes a string as a JSON value
     * <p>Usage: JSON_QUOTE(string)
     */
    public static final Operator JSON_QUOTE = Operator.create("JSON_QUOTE", String.class);

    // ========================================
    // Search Functions (10)
    // ========================================

    /**
     * JSON_EXTRACT() - Extracts data from JSON document
     * <p>Usage: JSON_EXTRACT(json_doc, path)
     */
    public static final Operator JSON_EXTRACT = Operator.create("JSON_EXTRACT", String.class, String.class);

    /**
     * JSON_VALUE() - Extracts a scalar value from JSON document (MySQL 8.0.21+)
     * <p>Usage: JSON_VALUE(json_doc, path)
     */
    public static final Operator JSON_VALUE = Operator.create("JSON_VALUE", String.class, String.class);

    /**
     * -> operator - Alias for JSON_EXTRACT
     * <p>Usage: json_doc->path
     */
    public static final Operator JSON_EXTRACT_OP = Operator.create("JSON_EXTRACT_OP", String.class, String.class);

    /**
     * ->> operator - Extracts and unquotes value
     * <p>Usage: json_doc->>path
     */
    public static final Operator JSON_UNQUOTE_EXTRACT_OP = Operator.create("JSON_UNQUOTE_EXTRACT_OP", String.class, String.class);

    /**
     * JSON_CONTAINS() - Whether JSON document contains value
     * <p>Usage: JSON_CONTAINS(json_doc, val[, path])
     */
    public static final Operator JSON_CONTAINS = Operator.create("JSON_CONTAINS", String.class, String.class);

    /**
     * JSON_CONTAINS_PATH() - Whether JSON document contains path
     * <p>Usage: JSON_CONTAINS_PATH(json_doc, one_or_all, path[, path] ...)
     */
    public static final Operator JSON_CONTAINS_PATH = Operator.create("JSON_CONTAINS_PATH", String.class, String.class, String[].class);

    /**
     * JSON_KEYS() - Returns keys from JSON object
     * <p>Usage: JSON_KEYS(json_doc[, path])
     */
    public static final Operator JSON_KEYS = Operator.create("JSON_KEYS", String.class);

    /**
     * JSON_SEARCH() - Path to value within JSON document
     * <p>Usage: JSON_SEARCH(json_doc, one_or_all, search_str[, escape_char[, path] ...])
     */
    public static final Operator JSON_SEARCH = Operator.create("JSON_SEARCH", String.class, String.class, String.class);

    /**
     * JSON_OVERLAPS() - Compares two JSON documents (MySQL 8.0.17+)
     * <p>Usage: JSON_OVERLAPS(json_doc1, json_doc2)
     */
    public static final Operator JSON_OVERLAPS = Operator.create("JSON_OVERLAPS", String.class, String.class);

    /**
     * MEMBER OF() - Tests if value is member of JSON array (MySQL 8.0.17+)
     * <p>Usage: value MEMBER OF(json_array)
     */
    public static final Operator MEMBER_OF = Operator.create("MEMBER_OF", String.class, String.class);

    // ========================================
    // Modification Functions (10)
    // ========================================

    /**
     * JSON_SET() - Inserts or updates data in JSON document
     * <p>Usage: JSON_SET(json_doc, path, val[, path, val] ...)
     */
    public static final Operator JSON_SET = Operator.create("JSON_SET", String.class, Object[].class);

    /**
     * JSON_INSERT() - Inserts data into JSON document
     * <p>Usage: JSON_INSERT(json_doc, path, val[, path, val] ...)
     */
    public static final Operator JSON_INSERT = Operator.create("JSON_INSERT", String.class, Object[].class);

    /**
     * JSON_REPLACE() - Replaces existing values in JSON document
     * <p>Usage: JSON_REPLACE(json_doc, path, val[, path, val] ...)
     */
    public static final Operator JSON_REPLACE = Operator.create("JSON_REPLACE", String.class, Object[].class);

    /**
     * JSON_REMOVE() - Removes data from JSON document
     * <p>Usage: JSON_REMOVE(json_doc, path[, path] ...)
     */
    public static final Operator JSON_REMOVE = Operator.create("JSON_REMOVE", String.class, String[].class);

    /**
     * JSON_ARRAY_APPEND() - Appends values to end of JSON arrays
     * <p>Usage: JSON_ARRAY_APPEND(json_doc, path, val[, path, val] ...)
     */
    public static final Operator JSON_ARRAY_APPEND = Operator.create("JSON_ARRAY_APPEND", String.class, Object[].class);

    /**
     * JSON_ARRAY_INSERT() - Inserts into JSON array
     * <p>Usage: JSON_ARRAY_INSERT(json_doc, path, val[, path, val] ...)
     */
    public static final Operator JSON_ARRAY_INSERT = Operator.create("JSON_ARRAY_INSERT", String.class, Object[].class);

    /**
     * JSON_MERGE_PATCH() - Merges JSON documents (RFC 7386)
     * <p>Usage: JSON_MERGE_PATCH(json_doc, json_doc[, json_doc] ...)
     */
    public static final Operator JSON_MERGE_PATCH = Operator.create("JSON_MERGE_PATCH", String[].class);

    /**
     * JSON_MERGE_PRESERVE() - Merges JSON documents, preserving duplicate keys
     * <p>Usage: JSON_MERGE_PRESERVE(json_doc, json_doc[, json_doc] ...)
     */
    public static final Operator JSON_MERGE_PRESERVE = Operator.create("JSON_MERGE_PRESERVE", String[].class);

    /**
     * JSON_MERGE() - Deprecated alias for JSON_MERGE_PRESERVE
     * <p>Usage: JSON_MERGE(json_doc, json_doc[, json_doc] ...)
     * @deprecated Use {@link #JSON_MERGE_PRESERVE} instead
     */
    @Deprecated
    public static final Operator JSON_MERGE = Operator.create("JSON_MERGE", String[].class);

    /**
     * JSON_UNQUOTE() - Unquotes JSON value
     * <p>Usage: JSON_UNQUOTE(json_val)
     */
    public static final Operator JSON_UNQUOTE = Operator.create("JSON_UNQUOTE", String.class);

    // ========================================
    // Attribute Functions (4)
    // ========================================

    /**
     * JSON_DEPTH() - Maximum depth of JSON document
     * <p>Usage: JSON_DEPTH(json_doc)
     */
    public static final Operator JSON_DEPTH = Operator.create("JSON_DEPTH", String.class);

    /**
     * JSON_LENGTH() - Number of elements in JSON document
     * <p>Usage: JSON_LENGTH(json_doc[, path])
     */
    public static final Operator JSON_LENGTH = Operator.create("JSON_LENGTH", String.class);

    /**
     * JSON_TYPE() - Type of JSON value
     * <p>Usage: JSON_TYPE(json_val)
     */
    public static final Operator JSON_TYPE = Operator.create("JSON_TYPE", String.class);

    /**
     * JSON_VALID() - Whether value is valid JSON
     * <p>Usage: JSON_VALID(val)
     */
    public static final Operator JSON_VALID = Operator.create("JSON_VALID", String.class);

    // ========================================
    // Utility Functions (3)
    // ========================================

    /**
     * JSON_PRETTY() - Prints JSON document in readable format
     * <p>Usage: JSON_PRETTY(json_doc)
     */
    public static final Operator JSON_PRETTY = Operator.create("JSON_PRETTY", String.class);

    /**
     * JSON_STORAGE_SIZE() - Space used for storage of binary representation
     * <p>Usage: JSON_STORAGE_SIZE(json_doc)
     */
    public static final Operator JSON_STORAGE_SIZE = Operator.create("JSON_STORAGE_SIZE", String.class);

    /**
     * JSON_STORAGE_FREE() - Freed space after partial update
     * <p>Usage: JSON_STORAGE_FREE(json_doc)
     */
    public static final Operator JSON_STORAGE_FREE = Operator.create("JSON_STORAGE_FREE", String.class);

    // ========================================
    // Schema Validation Functions (2) - MySQL 8.0.17+
    // ========================================

    /**
     * JSON_SCHEMA_VALID() - Validates JSON document against schema (MySQL 8.0.17+)
     * <p>Usage: JSON_SCHEMA_VALID(schema, document)
     */
    public static final Operator JSON_SCHEMA_VALID = Operator.create("JSON_SCHEMA_VALID", String.class, String.class);

    /**
     * JSON_SCHEMA_VALIDATION_REPORT() - Returns validation report (MySQL 8.0.17+)
     * <p>Usage: JSON_SCHEMA_VALIDATION_REPORT(schema, document)
     */
    public static final Operator JSON_SCHEMA_VALIDATION_REPORT = Operator.create("JSON_SCHEMA_VALIDATION_REPORT", String.class, String.class);

    // ========================================
    // Aggregate Functions (2)
    // ========================================

    /**
     * JSON_ARRAYAGG() - Aggregates result set as single JSON array
     * <p>Usage: JSON_ARRAYAGG(col_or_expr)
     */
    public static final Operator JSON_ARRAYAGG = Operator.create("JSON_ARRAYAGG", Object.class);

    /**
     * JSON_OBJECTAGG() - Aggregates result set as single JSON object
     * <p>Usage: JSON_OBJECTAGG(key, value)
     */
    public static final Operator JSON_OBJECTAGG = Operator.create("JSON_OBJECTAGG", Object.class, Object.class);

    // ========================================
    // Table Functions (1)
    // ========================================

    /**
     * JSON_TABLE() - Returns data from JSON expression as relational table
     * <p>Usage: JSON_TABLE(expr, path COLUMNS (column_list) ) [AS] alias
     * <p>Note: JSON_TABLE requires special handling due to its complex syntax
     */
    public static final Operator JSON_TABLE = Operator.create("JSON_TABLE", String.class, String.class);
}
