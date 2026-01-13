package com.github.snowykte0426.querydsl.mysql.json.core.functions;

import com.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonTableExpression;
import com.github.snowykte0426.querydsl.mysql.json.core.types.JsonTableColumn;
import com.querydsl.core.types.Expression;

/**
 * Factory class for MySQL JSON_TABLE function.
 *
 * <p>JSON_TABLE converts JSON data into a relational table format,
 * enabling querying of JSON documents using standard SQL operations.
 *
 * <p>This is one of the most powerful JSON functions in MySQL,
 * allowing complex JSON documents to be decomposed into rows and columns.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public final class JsonTableFunctions {

    private JsonTableFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_TABLE - Convert JSON to table
    // ========================================

    /**
     * Creates a JSON_TABLE expression builder.
     *
     * <p>JSON_TABLE extracts data from a JSON document and returns it as a
     * relational table with rows and columns.
     *
     * <p>SQL: {@code JSON_TABLE(json_doc, path COLUMNS(...)) AS alias}
     *
     * <p>Example:
     * <pre>
     * -- JSON document: [{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]
     *
     * SELECT jt.* FROM JSON_TABLE(
     *     '[{"id": 1, "name": "John"}, {"id": 2, "name": "Jane"}]',
     *     '$[*]' COLUMNS(
     *         id INT PATH '$.id',
     *         name VARCHAR(100) PATH '$.name'
     *     )
     * ) AS jt;
     *
     * Result:
     * +----+------+
     * | id | name |
     * +----+------+
     * |  1 | John |
     * |  2 | Jane |
     * +----+------+
     * </pre>
     *
     * <p>Usage in QueryDSL:
     * <pre>
     * JsonTableExpression table = jsonTable()
     *     .jsonDoc(expression)
     *     .path("$[*]")
     *     .column("id", "INT", "$.id")
     *     .column("name", "VARCHAR(100)", "$.name")
     *     .alias("jt")
     *     .build();
     * </pre>
     *
     * @return JSON_TABLE expression builder
     */
    public static JsonTableExpression.Builder jsonTable() {
        return JsonTableExpression.builder();
    }

    /**
     * Creates a JSON_TABLE expression with JSON document and path.
     *
     * @param jsonDoc the JSON document expression
     * @param path the root JSON path (e.g., "$", "$[*]")
     * @return builder for adding columns
     */
    public static JsonTableExpression.Builder jsonTable(Expression<?> jsonDoc, String path) {
        return JsonTableExpression.builder()
            .jsonDoc(jsonDoc)
            .path(path);
    }

    /**
     * Creates a JSON_TABLE expression with JSON string and path.
     *
     * @param jsonString the JSON string literal
     * @param path the root JSON path
     * @return builder for adding columns
     */
    public static JsonTableExpression.Builder jsonTable(String jsonString, String path) {
        return JsonTableExpression.builder()
            .jsonDoc(jsonString)
            .path(path);
    }

    // ========================================
    // Column definition helpers
    // ========================================

    /**
     * Creates a standard column definition.
     *
     * <p>Maps a JSON value to a typed SQL column.
     *
     * @param columnName the column name in result
     * @param sqlType the SQL data type (e.g., "INT", "VARCHAR(100)", "JSON")
     * @param jsonPath the JSON path to extract value
     * @return column definition
     */
    public static JsonTableColumn column(String columnName, String sqlType, String jsonPath) {
        return JsonTableColumn.column(columnName, sqlType, jsonPath);
    }

    /**
     * Creates an EXISTS column.
     *
     * <p>Returns 1 if the path exists in the JSON document, 0 otherwise.
     *
     * <p>Example:
     * <pre>
     * existsColumn("has_email", "$.email")
     * -- Returns 1 if $.email exists, 0 if not
     * </pre>
     *
     * @param columnName the column name
     * @param jsonPath the JSON path to check
     * @return EXISTS column definition
     */
    public static JsonTableColumn existsColumn(String columnName, String jsonPath) {
        return JsonTableColumn.exists(columnName, jsonPath);
    }

    /**
     * Creates an ORDINALITY column.
     *
     * <p>Provides sequential numbering for rows (1, 2, 3, ...).
     * This is useful for assigning unique identifiers to rows extracted from arrays.
     *
     * <p>Example:
     * <pre>
     * ordinalityColumn("row_num")
     * -- Generates: row_num FOR ORDINALITY
     * </pre>
     *
     * @param columnName the column name
     * @return ORDINALITY column definition
     */
    public static JsonTableColumn ordinalityColumn(String columnName) {
        return JsonTableColumn.ordinality(columnName);
    }

    /**
     * Creates a column with custom builder for advanced options.
     *
     * <p>Allows setting ON EMPTY and ON ERROR clauses.
     *
     * <p>Example:
     * <pre>
     * columnBuilder()
     *     .columnName("age")
     *     .sqlType("INT")
     *     .jsonPath("$.age")
     *     .onEmpty("DEFAULT 0")
     *     .onError("NULL")
     *     .build()
     * </pre>
     *
     * @return column builder
     */
    public static JsonTableColumn.Builder columnBuilder() {
        return JsonTableColumn.builder();
    }

    // ========================================
    // Common column types
    // ========================================

    /**
     * Creates an INT column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return INT column definition
     */
    public static JsonTableColumn intColumn(String columnName, String jsonPath) {
        return column(columnName, "INT", jsonPath);
    }

    /**
     * Creates a BIGINT column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return BIGINT column definition
     */
    public static JsonTableColumn bigIntColumn(String columnName, String jsonPath) {
        return column(columnName, "BIGINT", jsonPath);
    }

    /**
     * Creates a VARCHAR column.
     *
     * @param columnName column name
     * @param length maximum length
     * @param jsonPath JSON path
     * @return VARCHAR column definition
     */
    public static JsonTableColumn varcharColumn(String columnName, int length, String jsonPath) {
        return column(columnName, "VARCHAR(" + length + ")", jsonPath);
    }

    /**
     * Creates a TEXT column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return TEXT column definition
     */
    public static JsonTableColumn textColumn(String columnName, String jsonPath) {
        return column(columnName, "TEXT", jsonPath);
    }

    /**
     * Creates a JSON column.
     *
     * <p>Useful for extracting nested JSON objects or arrays.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return JSON column definition
     */
    public static JsonTableColumn jsonColumn(String columnName, String jsonPath) {
        return column(columnName, "JSON", jsonPath);
    }

    /**
     * Creates a DECIMAL column.
     *
     * @param columnName column name
     * @param precision total digits
     * @param scale decimal places
     * @param jsonPath JSON path
     * @return DECIMAL column definition
     */
    public static JsonTableColumn decimalColumn(
        String columnName,
        int precision,
        int scale,
        String jsonPath
    ) {
        return column(columnName, "DECIMAL(" + precision + "," + scale + ")", jsonPath);
    }

    /**
     * Creates a DATE column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return DATE column definition
     */
    public static JsonTableColumn dateColumn(String columnName, String jsonPath) {
        return column(columnName, "DATE", jsonPath);
    }

    /**
     * Creates a DATETIME column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return DATETIME column definition
     */
    public static JsonTableColumn datetimeColumn(String columnName, String jsonPath) {
        return column(columnName, "DATETIME", jsonPath);
    }

    /**
     * Creates a BOOLEAN/TINYINT(1) column.
     *
     * @param columnName column name
     * @param jsonPath JSON path
     * @return BOOLEAN column definition
     */
    public static JsonTableColumn booleanColumn(String columnName, String jsonPath) {
        return column(columnName, "TINYINT(1)", jsonPath);
    }
}