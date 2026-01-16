package io.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON modification functions in SQL module.
 */
class SqlJsonModifyFunctionsTest extends AbstractSqlJsonFunctionTest {

    private Long testUserId;

    @BeforeEach
    void setupTestData() throws SQLException {
        testUserId = createUser("John", "john@example.com", "{\"role\":\"user\",\"age\":25}");
    }

    @Test
    void jsonSet_shouldUpdateValue() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_SET('{\"a\":1}', '$.b', 2)"
        );
        assertThat(result).contains("\"a\"", "\"b\"", "1", "2");
    }

    @Test
    void jsonInsert_shouldInsertNewValue() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_INSERT('{\"a\":1}', '$.b', 2)"
        );
        assertThat(result).contains("\"b\"", "2");
    }

    @Test
    void jsonReplace_shouldReplaceExistingValue() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_REPLACE('{\"a\":1}', '$.a', 2)"
        );
        assertThat(result).contains("\"a\"", "2");
    }

    @Test
    void jsonRemove_shouldRemoveValue() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_REMOVE('{\"a\":1,\"b\":2}', '$.b')"
        );
        assertThat(result).contains("\"a\"");
        assertThat(result).doesNotContain("\"b\"");
    }

    @Test
    void jsonArrayAppend_shouldAppendToArray() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_ARRAY_APPEND('[1,2]', '$', 3)"
        );
        assertThat(result).isIn("[1, 2, 3]", "[1,2,3]");
    }

    @Test
    void jsonArrayInsert_shouldInsertIntoArray() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_ARRAY_INSERT('[1,3]', '$[1]', 2)"
        );
        assertThat(result).isIn("[1, 2, 3]", "[1,2,3]");
    }

    @Test
    void jsonMergePatch_shouldMergeDocuments() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_MERGE_PATCH('{\"a\":1}', '{\"b\":2}')"
        );
        assertThat(result).contains("\"a\"", "\"b\"");
    }

    @Test
    void jsonMergePreserve_shouldPreserveDuplicates() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_MERGE_PRESERVE('[1,2]', '[3,4]')"
        );
        assertThat(result).contains("1", "2", "3", "4");
    }

    @Test
    void jsonUnquote_shouldRemoveQuotes() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_UNQUOTE('\"hello\"')"
        );
        assertThat(result).isEqualTo("hello");
    }

    @Test
    void updateWithJsonSet_shouldModifyDatabase() throws SQLException {
        connection.createStatement().execute(
            "UPDATE users SET metadata = JSON_SET(metadata, '$.role', 'admin') WHERE id = " + testUserId
        );

        String role = executeNativeQuery(
            "SELECT JSON_EXTRACT(metadata, '$.role') FROM users WHERE id = " + testUserId
        );
        assertThat(role).isEqualTo("\"admin\"");
    }
}
