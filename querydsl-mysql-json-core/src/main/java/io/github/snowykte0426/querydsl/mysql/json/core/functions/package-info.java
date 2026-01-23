/**
 * MySQL JSON function implementations for QueryDSL.
 *
 * <p>
 * This package provides static factory methods for all MySQL JSON functions,
 * organized by category. Each class corresponds to a specific category of MySQL
 * JSON operations.
 * </p>
 *
 * <h2>Main Classes</h2>
 * <ul>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonCreationFunctions}
 * - Functions for creating JSON values (JSON_ARRAY, JSON_OBJECT, etc.)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonSearchFunctions}
 * - Functions for searching JSON documents (JSON_CONTAINS, JSON_SEARCH,
 * etc.)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonModifyFunctions}
 * - Functions for modifying JSON documents (JSON_SET, JSON_INSERT,
 * JSON_REPLACE, etc.)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonAttributeFunctions}
 * - Functions for JSON attributes (JSON_LENGTH, JSON_DEPTH, JSON_TYPE,
 * etc.)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonUtilityFunctions}
 * - Utility functions (JSON_PRETTY, JSON_STORAGE_SIZE, JSON_QUOTE, etc.)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonAggregateFunctions}
 * - Aggregate functions (JSON_ARRAYAGG, JSON_OBJECTAGG)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonSchemaFunctions}
 * - Schema validation functions (JSON_SCHEMA_VALID,
 * JSON_SCHEMA_VALIDATION_REPORT)</li>
 * <li>{@link io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonTableFunctions}
 * - JSON_TABLE function for converting JSON to tabular format</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0
 */
package io.github.snowykte0426.querydsl.mysql.json.core.functions;
