package io.github.snowykte0426.querydsl.mysql.json.jpa;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON schema validation functions in JPA environment. These
 * functions are available in MySQL 8.0.17+.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>JSON_SCHEMA_VALID - Validate JSON against schema</li>
 * <li>JSON_SCHEMA_VALIDATION_REPORT - Get validation report</li>
 * </ul>
 */
@DisplayName("JPA JSON Schema Functions (MySQL 8.0.17+)")
class JPAJsonSchemaFunctionsTest extends AbstractJPAJsonFunctionTest {

    private static final String SIMPLE_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "age": {"type": "integer", "minimum": 0}
                },
                "required": ["name"]
            }
            """;

    private static final String ARRAY_SCHEMA = """
            {
                "type": "array",
                "items": {"type": "integer"},
                "minItems": 1
            }
            """;

    @Nested
    @DisplayName("JSON_SCHEMA_VALID")
    class JsonSchemaValidTests {

        @Test
        @DisplayName("should return 1 for valid document")
        void return1ForValidDocument() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID("
                    + "'{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}}}', "
                    + "'{\"name\": \"John\"}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 0 for invalid document")
        void return0ForInvalidDocument() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID("
                    + "'{\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}}, \"required\": [\"name\"]}', "
                    + "'{\"age\": 30}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should validate type constraints")
        void validateTypeConstraints() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"integer\"}', " + "'123')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should fail type constraints")
        void failTypeConstraints() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"integer\"}', " + "'\"not a number\"')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should validate array items")
        void validateArrayItems() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"array\", \"items\": {\"type\": \"integer\"}}', "
                    + "'[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should fail invalid array items")
        void failInvalidArrayItems() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"array\", \"items\": {\"type\": \"integer\"}}', "
                    + "'[1, \"two\", 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should validate minimum constraint")
        void validateMinimumConstraint() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"integer\", \"minimum\": 0}', " + "'10')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should fail minimum constraint")
        void failMinimumConstraint() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + "'{\"type\": \"integer\", \"minimum\": 0}', " + "'-5')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("JSON_SCHEMA_VALIDATION_REPORT")
    class JsonSchemaValidationReportTests {

        @Test
        @DisplayName("should return valid:true for valid document")
        void returnValidTrueForValidDocument() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALIDATION_REPORT(" + "'{\"type\": \"object\"}', " + "'{}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"valid\"");
            assertThat(result).contains("true");
        }

        @Test
        @DisplayName("should return valid:false for invalid document")
        void returnValidFalseForInvalidDocument() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALIDATION_REPORT("
                    + "'{\"type\": \"object\", \"required\": [\"name\"]}', " + "'{}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"valid\"");
            assertThat(result).contains("false");
        }

        @Test
        @DisplayName("should include reason for invalid document")
        void includeReasonForInvalidDocument() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALIDATION_REPORT(" + "'{\"type\": \"integer\"}', " + "'\"string\"')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"reason\"");
        }

        @Test
        @DisplayName("should include schema-location for failure")
        void includeSchemaLocationForFailure() {
            @NotNull String sql = "SELECT JSON_SCHEMA_VALIDATION_REPORT("
                    + "'{\"type\": \"object\", \"properties\": {\"age\": {\"type\": \"integer\"}}}', "
                    + "'{\"age\": \"not a number\"}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("valid");
        }

        @Test
        @DisplayName("should work with complex schema")
        void workWithComplexSchema() {
            @NotNull String schema = "'{\"type\": \"object\", \"properties\": {\"users\": {\"type\": \"array\", \"items\": {\"type\": \"object\", \"properties\": {\"name\": {\"type\": \"string\"}}}}}}'";
            @NotNull String validDoc = "'{\"users\": [{\"name\": \"John\"}, {\"name\": \"Jane\"}]}'";

            @NotNull String sql = "SELECT JSON_SCHEMA_VALIDATION_REPORT(" + schema + ", " + validDoc + ")";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"valid\"");
            assertThat(result).contains("true");
        }
    }

    @Nested
    @DisplayName("Schema Validation with User Data")
    class SchemaValidationWithUserDataTests {

        @Test
        @DisplayName("should validate user metadata against schema")
        void validateUserMetadataAgainstSchema() {
            // Create a user with valid metadata
            createUser("John", "john@example.com", "{\"role\": \"admin\", \"level\": 5}");

            @NotNull String schema = "'{\"type\": \"object\", \"properties\": {\"role\": {\"type\": \"string\"}, \"level\": {\"type\": \"integer\"}}}'";
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + schema
                    + ", metadata) FROM users WHERE email = 'john@example.com'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should detect invalid user metadata")
        void detectInvalidUserMetadata() {
            // Create a user with metadata that has wrong type
            createUser("Jane", "jane@example.com", "{\"role\": 123}");

            @NotNull String schema = "'{\"type\": \"object\", \"properties\": {\"role\": {\"type\": \"string\"}}}'";
            @NotNull String sql = "SELECT JSON_SCHEMA_VALID(" + schema
                    + ", metadata) FROM users WHERE email = 'jane@example.com'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }
    }
}
