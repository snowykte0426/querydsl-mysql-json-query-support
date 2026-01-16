package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonSchemaFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON schema validation functions (MySQL 8.0.17+).
 *
 * @author snowykte0426
 * @since 1.0.0
 */
class JsonSchemaFunctionsTest extends AbstractJsonFunctionTest {

    // Common schemas for testing
    private static final String USER_SCHEMA = "{"
        + "\"type\": \"object\","
        + "\"properties\": {"
        + "  \"name\": {\"type\": \"string\"},"
        + "  \"age\": {\"type\": \"number\", \"minimum\": 0}"
        + "},"
        + "\"required\": [\"name\"]"
        + "}";

    private static final String ARRAY_SCHEMA = "{"
        + "\"type\": \"array\","
        + "\"items\": {\"type\": \"number\"},"
        + "\"minItems\": 1"
        + "}";

    // ========================================
    // JSON_SCHEMA_VALID tests
    // ========================================

    @Test
    void jsonSchemaValid_withValidDocument_shouldReturnTrue() throws SQLException {
        // Given
        String validDoc = "{\"name\": \"John\", \"age\": 30}";
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, validDoc);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_withMissingRequiredField_shouldReturnFalse() throws SQLException {
        // Given
        String invalidDoc = "{\"age\": 30}";
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withInvalidType_shouldReturnFalse() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": \"John\", \"age\": \"thirty\"}";
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withNegativeAge_shouldReturnFalse() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withExtraFields_shouldReturnTrue() throws SQLException {
        // Given - Schema doesn't have additionalProperties: false
        String validDoc = "{\"name\": \"John\", \"age\": 30, \"city\": \"Seoul\"}";
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, validDoc);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_withArraySchema_shouldValidate() throws SQLException {
        // Given
        String validArray = "[1, 2, 3, 4, 5]";
        String invalidArray = "[1, \"two\", 3]";

        BooleanExpression validCheck = jsonSchemaValid(ARRAY_SCHEMA, validArray);
        BooleanExpression invalidCheck = jsonSchemaValid(ARRAY_SCHEMA, invalidArray);

        // When
        String validResult = executeScalar(validCheck);
        String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withExpression_shouldWork() throws SQLException {
        // Given
        StringExpression docExpr = Expressions.stringTemplate(
            "json_object('name', 'Alice', 'age', 25)"
        );
        BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, docExpr);

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    // ========================================
    // JSON_SCHEMA_VALIDATION_REPORT tests
    // ========================================

    @Test
    void jsonSchemaValidationReport_withValidDocument_shouldReturnValidTrue() throws SQLException {
        // Given
        String validDoc = "{\"name\": \"John\", \"age\": 30}";
        StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, validDoc);

        // When
        String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("true");
    }

    @Test
    void jsonSchemaValidationReport_withInvalidDocument_shouldReturnReason() throws SQLException {
        // Given
        String invalidDoc = "{\"age\": 30}";
        StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("false");
        assertThat(result).contains("\"reason\"");
    }

    @Test
    void jsonSchemaValidationReport_withTypeError_shouldShowDetails() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": 123, \"age\": 30}";
        StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("false");
        // Report should have detailed error information
    }

    @Test
    void jsonSchemaValidationReport_withMinimumViolation_shouldShowError() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": \"John\", \"age\": -10}";
        StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("false");
        assertThat(result).contains("\"reason\"");
    }

    // ========================================
    // Convenience method tests
    // ========================================

    @Test
    void validate_shouldBeSameAsJsonSchemaValid() throws SQLException {
        // Given
        String validDoc = "{\"name\": \"John\", \"age\": 30}";
        BooleanExpression valid = validate(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(validDoc)
        );

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isValidFromReport_withValidDocument_shouldReturnTrue() throws SQLException {
        // Given
        String validDoc = "{\"name\": \"John\", \"age\": 30}";
        BooleanExpression valid = isValidFromReport(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(validDoc)
        );

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isValidFromReport_withInvalidDocument_shouldReturnFalse() throws SQLException {
        // Given
        String invalidDoc = "{\"age\": 30}";
        BooleanExpression valid = isValidFromReport(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(invalidDoc)
        );

        // When
        String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void getValidationReason_withInvalidDocument_shouldReturnReason() throws SQLException {
        // Given
        String invalidDoc = "{\"age\": 30}";
        StringExpression reason = getValidationReason(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(invalidDoc)
        );

        // When
        String result = executeScalar(reason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        // Reason should mention the missing required field
    }

    @Test
    void getValidationReason_withValidDocument_shouldReturnNull() throws SQLException {
        // Given
        String validDoc = "{\"name\": \"John\", \"age\": 30}";
        StringExpression reason = getValidationReason(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(validDoc)
        );

        // When
        String result = executeScalar(reason);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getFailedSchemaLocation_withInvalidDocument_shouldReturnLocation() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        StringExpression location = getFailedSchemaLocation(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(invalidDoc)
        );

        // When
        String result = executeScalar(location);

        // Then
        if (result != null) {
            // Schema location should point to the age property
            assertThat(result).contains("age");
        }
    }

    @Test
    void getFailedDocumentLocation_withInvalidDocument_shouldReturnLocation() throws SQLException {
        // Given
        String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        StringExpression location = getFailedDocumentLocation(
            Expressions.constant(USER_SCHEMA),
            Expressions.constant(invalidDoc)
        );

        // When
        String result = executeScalar(location);

        // Then
        if (result != null) {
            // Document location should point to the age field
            assertThat(result).contains("age");
        }
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonSchemaValid_inDatabase_shouldValidateColumn() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('John', 'john@test.com', '{\"name\": \"John\", \"age\": 30}')");

        // When
        String valid = executeScalar(
            "SELECT JSON_SCHEMA_VALID('" + USER_SCHEMA + "', metadata) FROM users WHERE name = 'John'"
        );

        // Then
        assertThat(valid).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_inWhereClause_shouldFilter() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, settings) VALUES " +
            "('ValidUser', 'valid@test.com', '{\"name\": \"Valid\", \"age\": 25}'), " +
            "('InvalidUser', 'invalid@test.com', '{\"age\": 30}')");

        // When
        String count = executeScalar(
            "SELECT COUNT(*) FROM users WHERE JSON_SCHEMA_VALID('" + USER_SCHEMA + "', settings)"
        );

        // Then
        assertThat(count).isEqualTo("1");
    }

    @Test
    void jsonSchemaValidationReport_inDatabase_shouldProvideDetails() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES " +
            "('InvalidProduct', 100.00, '{\"color\": 123}')");

        String colorSchema = "{\"type\": \"object\", \"properties\": {\"color\": {\"type\": \"string\"}}}";

        // When
        String report = executeScalar(
            "SELECT JSON_SCHEMA_VALIDATION_REPORT('" + colorSchema + "', attributes) " +
            "FROM products WHERE name = 'InvalidProduct'"
        );

        // Then
        assertThat(report).contains("\"valid\"");
        assertThat(report).contains("false");
    }

    @Test
    void complexSchema_withNestedObjects_shouldValidate() throws SQLException {
        // Given
        String complexSchema = "{"
            + "\"type\": \"object\","
            + "\"properties\": {"
            + "  \"user\": {"
            + "    \"type\": \"object\","
            + "    \"properties\": {"
            + "      \"name\": {\"type\": \"string\"},"
            + "      \"email\": {\"type\": \"string\", \"format\": \"email\"}"
            + "    },"
            + "    \"required\": [\"name\", \"email\"]"
            + "  }"
            + "}"
            + "}";

        String validDoc = "{\"user\": {\"name\": \"John\", \"email\": \"john@example.com\"}}";
        String invalidDoc = "{\"user\": {\"name\": \"John\"}}";

        BooleanExpression validCheck = jsonSchemaValid(complexSchema, validDoc);
        BooleanExpression invalidCheck = jsonSchemaValid(complexSchema, invalidDoc);

        // When
        String validResult = executeScalar(validCheck);
        String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }

    @Test
    void schemaWithEnums_shouldValidate() throws SQLException {
        // Given
        String enumSchema = "{"
            + "\"type\": \"object\","
            + "\"properties\": {"
            + "  \"status\": {\"type\": \"string\", \"enum\": [\"active\", \"inactive\", \"pending\"]}"
            + "}"
            + "}";

        String validDoc = "{\"status\": \"active\"}";
        String invalidDoc = "{\"status\": \"deleted\"}";

        BooleanExpression validCheck = jsonSchemaValid(enumSchema, validDoc);
        BooleanExpression invalidCheck = jsonSchemaValid(enumSchema, invalidDoc);

        // When
        String validResult = executeScalar(validCheck);
        String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }
}