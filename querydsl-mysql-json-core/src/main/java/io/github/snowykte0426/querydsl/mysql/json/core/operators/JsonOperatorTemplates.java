package io.github.snowykte0426.querydsl.mysql.json.core.operators;

import com.querydsl.core.types.Operator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL template definitions for MySQL JSON operators.
 *
 * This class provides SQL template strings for all MySQL JSON functions,
 * mapping each {@link JsonOperators} enum to its corresponding MySQL function
 * syntax.
 *
 * Template placeholders:
 * <ul>
 * <li>{0}, {1}, {2}, ... - positional parameters for function arguments</li>
 * <li>{0s} - varargs splicing for variable-length arguments</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonOperatorTemplates {

    private JsonOperatorTemplates() {
        // Utility class - prevent instantiation
    }

    /**
     * Returns a map of operator templates suitable for use with QueryDSL Templates.
     * These templates should be registered with MySQL-specific Templates
     * implementation.
     *
     * @return Map of operators to template strings
     */
    public static @NotNull Map<Operator, String> getTemplates() {
        return getOperatorTemplateMap();
    }

    /**
     * Returns a map of operators to their SQL template strings.
     *
     * @return Map of Operator to template String
     */
    private static @NotNull Map<Operator, String> getOperatorTemplateMap() {
        @NotNull
        Map<Operator, String> templates = new HashMap<>();

        // ========================================
        // Creation Functions (3)
        // ========================================
        templates.put(JsonOperators.JSON_ARRAY, "json_array({0s})");
        templates.put(JsonOperators.JSON_OBJECT, "json_object({0s})");
        templates.put(JsonOperators.JSON_QUOTE, "json_quote({0})");

        // ========================================
        // Search Functions (10)
        // ========================================
        templates.put(JsonOperators.JSON_EXTRACT, "json_extract({0}, {1s})");
        templates.put(JsonOperators.JSON_VALUE, "json_value({0}, {1})");
        templates.put(JsonOperators.JSON_EXTRACT_OP, "{0}->{1}");
        templates.put(JsonOperators.JSON_UNQUOTE_EXTRACT_OP, "{0}->>{1}");
        templates.put(JsonOperators.JSON_CONTAINS, "json_contains({0}, {1}, {2})");
        templates.put(JsonOperators.JSON_CONTAINS_PATH, "json_contains_path({0}, {1}, {2s})");
        templates.put(JsonOperators.JSON_KEYS, "json_keys({0}, {1})");
        templates.put(JsonOperators.JSON_SEARCH, "json_search({0}, {1}, {2}, {3}, {4s})");
        templates.put(JsonOperators.JSON_OVERLAPS, "json_overlaps({0}, {1})");
        templates.put(JsonOperators.MEMBER_OF, "{0} member of({1})");

        // ========================================
        // Modification Functions (10)
        // ========================================
        templates.put(JsonOperators.JSON_SET, "json_set({0}, {1s})");
        templates.put(JsonOperators.JSON_INSERT, "json_insert({0}, {1s})");
        templates.put(JsonOperators.JSON_REPLACE, "json_replace({0}, {1s})");
        templates.put(JsonOperators.JSON_REMOVE, "json_remove({0}, {1s})");
        templates.put(JsonOperators.JSON_ARRAY_APPEND, "json_array_append({0}, {1s})");
        templates.put(JsonOperators.JSON_ARRAY_INSERT, "json_array_insert({0}, {1s})");
        templates.put(JsonOperators.JSON_MERGE_PATCH, "json_merge_patch({0s})");
        templates.put(JsonOperators.JSON_MERGE_PRESERVE, "json_merge_preserve({0s})");
        templates.put(JsonOperators.JSON_MERGE, "json_merge({0s})");
        templates.put(JsonOperators.JSON_UNQUOTE, "json_unquote({0})");

        // ========================================
        // Attribute Functions (4)
        // ========================================
        templates.put(JsonOperators.JSON_DEPTH, "json_depth({0})");
        templates.put(JsonOperators.JSON_LENGTH, "json_length({0}, {1})");
        templates.put(JsonOperators.JSON_TYPE, "json_type({0})");
        templates.put(JsonOperators.JSON_VALID, "json_valid({0})");

        // ========================================
        // Utility Functions (3)
        // ========================================
        templates.put(JsonOperators.JSON_PRETTY, "json_pretty({0})");
        templates.put(JsonOperators.JSON_STORAGE_SIZE, "json_storage_size({0})");
        templates.put(JsonOperators.JSON_STORAGE_FREE, "json_storage_free({0})");

        // ========================================
        // Schema Validation Functions (2)
        // ========================================
        templates.put(JsonOperators.JSON_SCHEMA_VALID, "json_schema_valid({0}, {1})");
        templates.put(JsonOperators.JSON_SCHEMA_VALIDATION_REPORT, "json_schema_validation_report({0}, {1})");

        // ========================================
        // Aggregate Functions (2)
        // ========================================
        templates.put(JsonOperators.JSON_ARRAYAGG, "json_arrayagg({0})");
        templates.put(JsonOperators.JSON_OBJECTAGG, "json_objectagg({0}, {1})");

        // ========================================
        // Table Functions (1)
        // ========================================
        // Note: JSON_TABLE has complex syntax and requires special handling
        // Template: json_table(expr, path COLUMNS (column_list)) [AS] alias
        templates.put(JsonOperators.JSON_TABLE, "json_table({0}, {1} columns({2}))");

        return templates;
    }

    /**
     * Convenience method to get template string for a specific operator.
     *
     * @param operator
     *            the JSON operator
     * @return the SQL template string
     */
    public static String getTemplate(Operator operator) {
        return getOperatorTemplateMap().get(operator);
    }
}
