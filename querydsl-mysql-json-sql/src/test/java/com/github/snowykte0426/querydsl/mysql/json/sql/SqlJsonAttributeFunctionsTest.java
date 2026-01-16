package com.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON attribute functions in SQL module.
 */
class SqlJsonAttributeFunctionsTest extends AbstractSqlJsonFunctionTest {

    @Test
    void jsonDepth_shouldReturnDepth() throws SQLException {
        Integer depth = executeScalarInt("SELECT JSON_DEPTH('{\"a\":{\"b\":1}}')");
        assertThat(depth).isEqualTo(3);
    }

    @Test
    void jsonDepth_ofArray_shouldReturnDepth() throws SQLException {
        Integer depth = executeScalarInt("SELECT JSON_DEPTH('[1,[2,[3]]]')");
        assertThat(depth).isEqualTo(4);
    }

    @Test
    void jsonLength_shouldReturnLength() throws SQLException {
        Integer length = executeScalarInt("SELECT JSON_LENGTH('[1,2,3,4]')");
        assertThat(length).isEqualTo(4);
    }

    @Test
    void jsonLength_ofObject_shouldReturnKeyCount() throws SQLException {
        Integer length = executeScalarInt("SELECT JSON_LENGTH('{\"a\":1,\"b\":2,\"c\":3}')");
        assertThat(length).isEqualTo(3);
    }

    @Test
    void jsonLength_withPath_shouldReturnLengthAtPath() throws SQLException {
        Integer length = executeScalarInt(
            "SELECT JSON_LENGTH('{\"a\":[1,2,3]}', '$.a')"
        );
        assertThat(length).isEqualTo(3);
    }

    @Test
    void jsonType_shouldReturnType() throws SQLException {
        String type = executeNativeQuery("SELECT JSON_TYPE('[1,2,3]')");
        assertThat(type).isEqualTo("ARRAY");
    }

    @Test
    void jsonType_ofObject_shouldReturnObject() throws SQLException {
        String type = executeNativeQuery("SELECT JSON_TYPE('{\"a\":1}')");
        assertThat(type).isEqualTo("OBJECT");
    }

    @Test
    void jsonType_ofString_shouldReturnString() throws SQLException {
        String type = executeNativeQuery("SELECT JSON_TYPE(JSON_QUOTE('hello'))");
        assertThat(type).isEqualTo("STRING");
    }

    @Test
    void jsonType_ofNumber_shouldReturnNumber() throws SQLException {
        String type = executeNativeQuery("SELECT JSON_TYPE(JSON_EXTRACT('[123]', '$[0]'))");
        assertThat(type).isIn("INTEGER", "DOUBLE");
    }

    @Test
    void jsonValid_withValidJson_shouldReturnTrue() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_VALID('{\"a\":1}')");
        assertThat(result).isEqualTo("1");
    }

    @Test
    void jsonValid_withInvalidJson_shouldReturnFalse() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_VALID('{invalid}')");
        assertThat(result).isEqualTo("0");
    }

    @Test
    void jsonValid_withNull_shouldReturnFalse() throws SQLException {
        String result = executeNativeQuery("SELECT JSON_VALID(NULL)");
        assertThat(result).isNull();
    }
}
