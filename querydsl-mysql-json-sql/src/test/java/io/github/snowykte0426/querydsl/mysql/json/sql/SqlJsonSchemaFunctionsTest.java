package io.github.snowykte0426.querydsl.mysql.json.sql;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON schema validation functions in SQL module (MySQL 8.0.17+).
 */
class SqlJsonSchemaFunctionsTest extends AbstractSqlJsonFunctionTest {

    private static final String SIMPLE_SCHEMA = """
            {
                "type": "object",
                "properties": {
                    "name": {"type": "string"},
                    "age": {"type": "number"}
                },
                "required": ["name"]
            }
            """;

    @Test
    void jsonSchemaValid_withValidDocument_shouldReturnTrue() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_SCHEMA_VALID('"
                + SIMPLE_SCHEMA.replace("\n", "").replace("\"", "\\\"") + "', '{\"name\":\"John\",\"age\":30}')");
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonSchemaValid_withInvalidDocument_shouldReturnFalse() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_SCHEMA_VALID('"
                + SIMPLE_SCHEMA.replace("\n", "").replace("\"", "\\\"") + "', '{\"age\":30}')");
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonSchemaValidationReport_shouldReturnReport() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_SCHEMA_VALIDATION_REPORT('"
                + SIMPLE_SCHEMA.replace("\n", "").replace("\"", "\\\"") + "', '{\"name\":\"John\"}')");
        assertThat(result).contains("valid");
    }

    @Test
    void jsonSchemaValid_withTypeValidation_shouldWork() throws SQLException {
        @NotNull String schema = "{\"type\":\"array\"}";

        String validResult = executeNativeQuery("SELECT JSON_SCHEMA_VALID('" + schema + "', '[1,2,3]')");
        assertThat(validResult).isEqualTo("1");

        String invalidResult = executeNativeQuery("SELECT JSON_SCHEMA_VALID('" + schema + "', '{\"a\":1}')");
        assertThat(invalidResult).isEqualTo("0");
    }
}
