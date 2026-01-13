package com.github.snowykte0426.querydsl.mysql.json.core.functions;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * Factory class for MySQL JSON schema validation functions (MySQL 8.0.17+).
 *
 * <p>This class provides static factory methods for JSON schema validation:
 * <ul>
 *   <li>{@link #jsonSchemaValid} - Validates JSON against schema</li>
 *   <li>{@link #jsonSchemaValidationReport} - Returns validation report</li>
 * </ul>
 *
 * <p>JSON Schema validation follows the JSON Schema specification.
 * These functions are available in MySQL 8.0.17 and later.
 *
 * @author snowykte0426
 * @since 1.0.0
 * @see <a href="https://json-schema.org/">JSON Schema Specification</a>
 */
public final class JsonSchemaFunctions {

    private JsonSchemaFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_SCHEMA_VALID - Validate against schema
    // ========================================

    /**
     * Validates a JSON document against a JSON schema.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALID(schema, document)}</p>
     *
     * <p>MySQL 8.0.17+</p>
     *
     * <p>Returns true if the document is valid according to the schema,
     * false otherwise. The schema must be a valid JSON Schema object.
     *
     * <p>Example:
     * <pre>
     * -- Define a schema
     * {
     *   "type": "object",
     *   "properties": {
     *     "name": {"type": "string"},
     *     "age": {"type": "number", "minimum": 0}
     *   },
     *   "required": ["name"]
     * }
     *
     * -- Valid document
     * JSON_SCHEMA_VALID(schema, '{"name": "John", "age": 30}')  -&gt; true
     *
     * -- Invalid document (missing required field)
     * JSON_SCHEMA_VALID(schema, '{"age": 30}')                  -&gt; false
     * </pre>
     *
     * @param schema the JSON schema expression
     * @param document the JSON document to validate
     * @return boolean expression indicating validity
     */
    public static BooleanExpression jsonSchemaValid(Expression<?> schema, Expression<?> document) {
        return Expressions.booleanTemplate("json_schema_valid({0}, {1})", schema, document);
    }

    /**
     * Validates a JSON document against a JSON schema (with string literals).
     *
     * @param schemaJson the JSON schema as string
     * @param documentJson the JSON document as string
     * @return boolean expression indicating validity
     */
    public static BooleanExpression jsonSchemaValid(String schemaJson, String documentJson) {
        return Expressions.booleanTemplate(
            "json_schema_valid({0}, {1})",
            Expressions.constant(schemaJson),
            Expressions.constant(documentJson)
        );
    }

    /**
     * Validates a JSON document (expression) against a schema (string literal).
     *
     * @param schemaJson the JSON schema as string
     * @param document the JSON document expression
     * @return boolean expression indicating validity
     */
    public static BooleanExpression jsonSchemaValid(String schemaJson, Expression<?> document) {
        return Expressions.booleanTemplate(
            "json_schema_valid({0}, {1})",
            Expressions.constant(schemaJson),
            document
        );
    }

    // ========================================
    // JSON_SCHEMA_VALIDATION_REPORT - Validation report
    // ========================================

    /**
     * Validates a JSON document against a schema and returns a detailed validation report.
     *
     * <p>SQL: {@code JSON_SCHEMA_VALIDATION_REPORT(schema, document)}</p>
     *
     * <p>MySQL 8.0.17+</p>
     *
     * <p>Returns a JSON object containing:
     * <ul>
     *   <li>{@code valid} - boolean indicating if document is valid</li>
     *   <li>{@code reason} - error message if invalid (only present when valid=false)</li>
     *   <li>{@code schema-location} - JSON pointer to schema location (if applicable)</li>
     *   <li>{@code document-location} - JSON pointer to document location (if applicable)</li>
     *   <li>{@code schema-failed-keyword} - the schema keyword that failed (if applicable)</li>
     * </ul>
     *
     * <p>Example output for valid document:
     * <pre>
     * {"valid": true}
     * </pre>
     *
     * <p>Example output for invalid document:
     * <pre>
     * {
     *   "valid": false,
     *   "reason": "The JSON document location '#/age' failed requirement 'minimum' at JSON Schema location '#/properties/age'",
     *   "schema-location": "#/properties/age",
     *   "document-location": "#/age",
     *   "schema-failed-keyword": "minimum"
     * }
     * </pre>
     *
     * @param schema the JSON schema expression
     * @param document the JSON document to validate
     * @return validation report as string expression (JSON object)
     */
    public static StringExpression jsonSchemaValidationReport(
        Expression<?> schema,
        Expression<?> document
    ) {
        return Expressions.stringTemplate(
            "json_schema_validation_report({0}, {1})",
            schema,
            document
        );
    }

    /**
     * Returns validation report for string literals.
     *
     * @param schemaJson the JSON schema as string
     * @param documentJson the JSON document as string
     * @return validation report as string expression
     */
    public static StringExpression jsonSchemaValidationReport(String schemaJson, String documentJson) {
        return Expressions.stringTemplate(
            "json_schema_validation_report({0}, {1})",
            Expressions.constant(schemaJson),
            Expressions.constant(documentJson)
        );
    }

    /**
     * Returns validation report with schema as string and document as expression.
     *
     * @param schemaJson the JSON schema as string
     * @param document the JSON document expression
     * @return validation report as string expression
     */
    public static StringExpression jsonSchemaValidationReport(
        String schemaJson,
        Expression<?> document
    ) {
        return Expressions.stringTemplate(
            "json_schema_validation_report({0}, {1})",
            Expressions.constant(schemaJson),
            document
        );
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Validates a document against a schema and returns true if valid.
     * Alias for {@link #jsonSchemaValid(Expression, Expression)}.
     *
     * @param schema the JSON schema
     * @param document the JSON document
     * @return boolean expression
     */
    public static BooleanExpression validate(Expression<?> schema, Expression<?> document) {
        return jsonSchemaValid(schema, document);
    }

    /**
     * Validates a document and checks if the validation report indicates validity.
     *
     * <p>This extracts the "valid" field from the validation report.
     *
     * @param schema the JSON schema
     * @param document the JSON document
     * @return boolean expression extracted from report
     */
    public static BooleanExpression isValidFromReport(Expression<?> schema, Expression<?> document) {
        return Expressions.booleanTemplate(
            "json_extract(json_schema_validation_report({0}, {1}), '$.valid') = true",
            schema,
            document
        );
    }

    /**
     * Gets the validation error reason from the report.
     *
     * <p>Returns NULL if the document is valid.
     *
     * @param schema the JSON schema
     * @param document the JSON document
     * @return string expression with error reason (or NULL if valid)
     */
    public static StringExpression getValidationReason(Expression<?> schema, Expression<?> document) {
        return Expressions.stringTemplate(
            "json_unquote(json_extract(json_schema_validation_report({0}, {1}), '$.reason'))",
            schema,
            document
        );
    }

    /**
     * Gets the schema location that failed validation.
     *
     * @param schema the JSON schema
     * @param document the JSON document
     * @return string expression with schema location (or NULL if valid)
     */
    public static StringExpression getFailedSchemaLocation(
        Expression<?> schema,
        Expression<?> document
    ) {
        return Expressions.stringTemplate(
            "json_unquote(json_extract(json_schema_validation_report({0}, {1}), '$[\"schema-location\"]'))",
            schema,
            document
        );
    }

    /**
     * Gets the document location that failed validation.
     *
     * @param schema the JSON schema
     * @param document the JSON document
     * @return string expression with document location (or NULL if valid)
     */
    public static StringExpression getFailedDocumentLocation(
        Expression<?> schema,
        Expression<?> document
    ) {
        return Expressions.stringTemplate(
            "json_unquote(json_extract(json_schema_validation_report({0}, {1}), '$[\"document-location\"]'))",
            schema,
            document
        );
    }
}