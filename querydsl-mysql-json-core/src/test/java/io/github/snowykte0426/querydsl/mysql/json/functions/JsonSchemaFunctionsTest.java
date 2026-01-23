package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonSchemaFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON schema validation functions (MySQL 8.0.17+).
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonSchemaFunctionsTest extends AbstractJsonFunctionTest {

    // Common schemas for testing
    private static final String USER_SCHEMA = "{" + "\"type\": \"object\"," + "\"properties\": {"
            + "  \"name\": {\"type\": \"string\"}," + "  \"age\": {\"type\": \"number\", \"minimum\": 0}" + "},"
            + "\"required\": [\"name\"]" + "}";

    private static final String ARRAY_SCHEMA = "{" + "\"type\": \"array\"," + "\"items\": {\"type\": \"number\"},"
            + "\"minItems\": 1" + "}";

    // ========================================
    // JSON_SCHEMA_VALID tests
    // ========================================

    @Test
    void jsonSchemaValid_withValidDocument_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30}";
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, validDoc);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_withMissingRequiredField_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"age\": 30}";
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withInvalidType_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": \"John\", \"age\": \"thirty\"}";
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withNegativeAge_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withExtraFields_shouldReturnTrue() throws SQLException {
        // Given - Schema doesn't have additionalProperties: false
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30, \"city\": \"Seoul\"}";
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, validDoc);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_withArraySchema_shouldValidate() throws SQLException {
        // Given
        @NotNull String validArray = "[1, 2, 3, 4, 5]";
        @NotNull String invalidArray = "[1, \"two\", 3]";

        @NotNull BooleanExpression validCheck = jsonSchemaValid(ARRAY_SCHEMA, validArray);
        @NotNull BooleanExpression invalidCheck = jsonSchemaValid(ARRAY_SCHEMA, invalidArray);

        // When
        @Nullable String validResult = executeScalar(validCheck);
        @Nullable String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }

    @Test
    void jsonSchemaValid_withExpression_shouldWork() throws SQLException {
        // Given
        @NotNull StringExpression docExpr = Expressions.stringTemplate("json_object('name', 'Alice', 'age', 25)");
        @NotNull BooleanExpression valid = jsonSchemaValid(USER_SCHEMA, docExpr);

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    // ========================================
    // JSON_SCHEMA_VALIDATION_REPORT tests
    // ========================================

    @Test
    void jsonSchemaValidationReport_withValidDocument_shouldReturnValidTrue() throws SQLException {
        // Given
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30}";
        @NotNull StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, validDoc);

        // When
        @Nullable String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("true");
    }

    @Test
    void jsonSchemaValidationReport_withInvalidDocument_shouldReturnReason() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"age\": 30}";
        @NotNull StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("false");
        assertThat(result).contains("\"reason\"");
    }

    @Test
    void jsonSchemaValidationReport_withTypeError_shouldShowDetails() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": 123, \"age\": 30}";
        @NotNull StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(report);

        // Then
        assertThat(result).contains("\"valid\"");
        assertThat(result).contains("false");
        // Report should have detailed error information
    }

    @Test
    void jsonSchemaValidationReport_withMinimumViolation_shouldShowError() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": \"John\", \"age\": -10}";
        @NotNull StringExpression report = jsonSchemaValidationReport(USER_SCHEMA, invalidDoc);

        // When
        @Nullable String result = executeScalar(report);

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
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30}";
        @NotNull BooleanExpression valid = validate(Expressions.constant(USER_SCHEMA), Expressions.constant(validDoc));

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isValidFromReport_withValidDocument_shouldReturnTrue() throws SQLException {
        // Given
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30}";
        @NotNull BooleanExpression valid = isValidFromReport(Expressions.constant(USER_SCHEMA), Expressions.constant(validDoc));

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("1");
    }

    @Test
    void isValidFromReport_withInvalidDocument_shouldReturnFalse() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"age\": 30}";
        @NotNull BooleanExpression valid = isValidFromReport(Expressions.constant(USER_SCHEMA),
                Expressions.constant(invalidDoc));

        // When
        @Nullable String result = executeScalar(valid);

        // Then
        assertThat(result).isEqualTo("0");
    }

    @Test
    void getValidationReason_withInvalidDocument_shouldReturnReason() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"age\": 30}";
        @NotNull StringExpression reason = getValidationReason(Expressions.constant(USER_SCHEMA),
                Expressions.constant(invalidDoc));

        // When
        @Nullable String result = executeScalar(reason);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        // Reason should mention the missing required field
    }

    @Test
    void getValidationReason_withValidDocument_shouldReturnNull() throws SQLException {
        // Given
        @NotNull String validDoc = "{\"name\": \"John\", \"age\": 30}";
        @NotNull StringExpression reason = getValidationReason(Expressions.constant(USER_SCHEMA),
                Expressions.constant(validDoc));

        // When
        @Nullable String result = executeScalar(reason);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void getFailedSchemaLocation_withInvalidDocument_shouldReturnLocation() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        @NotNull StringExpression location = getFailedSchemaLocation(Expressions.constant(USER_SCHEMA),
                Expressions.constant(invalidDoc));

        // When
        @Nullable String result = executeScalar(location);

        // Then
        if (result != null) {
            // Schema location should point to the age property
            assertThat(result).contains("age");
        }
    }

    @Test
    void getFailedDocumentLocation_withInvalidDocument_shouldReturnLocation() throws SQLException {
        // Given
        @NotNull String invalidDoc = "{\"name\": \"John\", \"age\": -5}";
        @NotNull StringExpression location = getFailedDocumentLocation(Expressions.constant(USER_SCHEMA),
                Expressions.constant(invalidDoc));

        // When
        @Nullable String result = executeScalar(location);

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
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('John', 'john@test.com', '{\"name\": \"John\", \"age\": 30}')");

        // When
        @Nullable String valid = executeScalar(
                "SELECT JSON_SCHEMA_VALID('" + USER_SCHEMA + "', metadata) FROM users WHERE name = 'John'");

        // Then
        assertThat(valid).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_inWhereClause_shouldFilter() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, settings) VALUES "
                + "('ValidUser', 'valid@test.com', '{\"name\": \"Valid\", \"age\": 25}'), "
                + "('InvalidUser', 'invalid@test.com', '{\"age\": 30}')");

        // When
        @Nullable String count = executeScalar(
                "SELECT COUNT(*) FROM users WHERE JSON_SCHEMA_VALID('" + USER_SCHEMA + "', settings)");

        // Then
        assertThat(count).isEqualTo("1");
    }

    @Test
    void jsonSchemaValidationReport_inDatabase_shouldProvideDetails() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES "
                + "('InvalidProduct', 100.00, '{\"color\": 123}')");

        @NotNull String colorSchema = "{\"type\": \"object\", \"properties\": {\"color\": {\"type\": \"string\"}}}";

        // When
        @Nullable String report = executeScalar("SELECT JSON_SCHEMA_VALIDATION_REPORT('" + colorSchema + "', attributes) "
                + "FROM products WHERE name = 'InvalidProduct'");

        // Then
        assertThat(report).contains("\"valid\"");
        assertThat(report).contains("false");
    }

    @Test
    void complexSchema_withNestedObjects_shouldValidate() throws SQLException {
        // Given
        @NotNull String complexSchema = "{" + "\"type\": \"object\"," + "\"properties\": {" + "  \"user\": {"
                + "    \"type\": \"object\"," + "    \"properties\": {" + "      \"name\": {\"type\": \"string\"},"
                + "      \"email\": {\"type\": \"string\", \"format\": \"email\"}" + "    },"
                + "    \"required\": [\"name\", \"email\"]" + "  }" + "}" + "}";

        @NotNull String validDoc = "{\"user\": {\"name\": \"John\", \"email\": \"john@example.com\"}}";
        @NotNull String invalidDoc = "{\"user\": {\"name\": \"John\"}}";

        @NotNull BooleanExpression validCheck = jsonSchemaValid(complexSchema, validDoc);
        @NotNull BooleanExpression invalidCheck = jsonSchemaValid(complexSchema, invalidDoc);

        // When
        @Nullable String validResult = executeScalar(validCheck);
        @Nullable String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }

    @Test
    void schemaWithEnums_shouldValidate() throws SQLException {
        // Given
        @NotNull String enumSchema = "{" + "\"type\": \"object\"," + "\"properties\": {"
                + "  \"status\": {\"type\": \"string\", \"enum\": [\"active\", \"inactive\", \"pending\"]}" + "}" + "}";

        @NotNull String validDoc = "{\"status\": \"active\"}";
        @NotNull String invalidDoc = "{\"status\": \"deleted\"}";

        @NotNull BooleanExpression validCheck = jsonSchemaValid(enumSchema, validDoc);
        @NotNull BooleanExpression invalidCheck = jsonSchemaValid(enumSchema, invalidDoc);

        // When
        @Nullable String validResult = executeScalar(validCheck);
        @Nullable String invalidResult = executeScalar(invalidCheck);

        // Then
        assertThat(validResult).isEqualTo("1");
        assertThat(invalidResult).isEqualTo("0");
    }
}
