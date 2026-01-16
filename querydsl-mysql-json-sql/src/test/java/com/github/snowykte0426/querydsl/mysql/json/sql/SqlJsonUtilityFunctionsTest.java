package com.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON utility functions in SQL module.
 */
class SqlJsonUtilityFunctionsTest extends AbstractSqlJsonFunctionTest {

    @Test
    void jsonPretty_shouldFormatJson() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_PRETTY('{\"a\":1,\"b\":2}')");
        assertThat(result).contains("\"a\"", "\"b\"");
        assertThat(result).contains("\n"); // Should have newlines
    }

    @Test
    void jsonPretty_withArray_shouldFormatArray() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_PRETTY('[1,2,3]')");
        assertThat(result).contains("[", "]");
        assertThat(result).containsAnyOf("\n", " "); // Formatted
    }

    @Test
    void jsonStorageSize_shouldReturnSize() throws SQLException {
        Integer size = executeScalarInt("SELECT JSON_STORAGE_SIZE('{\"a\":1}')");
        assertThat(size).isGreaterThan(0);
    }

    @Test
    void jsonStorageSize_ofArray_shouldReturnSize() throws SQLException {
        Integer size = executeScalarInt("SELECT JSON_STORAGE_SIZE('[1,2,3,4,5]')");
        assertThat(size).isGreaterThan(0);
    }

    @Test
    void jsonStorageFree_shouldReturnFreeSpace() throws SQLException {
        Long userId = createUser("Test", "test@example.com", "{\"a\":1,\"b\":2,\"c\":3}");
        
        // Update to smaller JSON
        connection.createStatement().execute(
            "UPDATE users SET metadata = '{\"a\":1}' WHERE id = " + userId
        );
        
        // Check storage free (may be 0 if MySQL optimizes)
        Integer freed = executeScalarInt(
            "SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE id = " + userId
        );
        assertThat(freed).isNotNull();
    }
}
