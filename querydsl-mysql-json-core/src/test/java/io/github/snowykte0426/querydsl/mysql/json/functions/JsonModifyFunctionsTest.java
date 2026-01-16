package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonModifyFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for JSON modification functions.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
class JsonModifyFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_SET tests
    // ========================================

    @Test
    void jsonSet_withSinglePath_shouldSetValue() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30}";
        JsonValueExpression result = jsonSet(Expressions.constant(doc), "$.age", 31);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"");
        assertThat(modified).contains("31");
        assertThat(modified).doesNotContain("30");
    }

    @Test
    void jsonSet_withNewPath_shouldInsertValue() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\"}";
        JsonValueExpression result = jsonSet(Expressions.constant(doc), "$.age", 25);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"name\"", "\"John\"");
        assertThat(modified).contains("\"age\"", "25");
    }

    @Test
    void jsonSet_withMultiplePaths_shouldSetAllValues() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30}";
        StringExpression result = jsonSet(Expressions.constant(doc), "$.age", 31, "$.city", "Seoul");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "31");
        assertThat(modified).contains("\"city\"", "\"Seoul\"");
    }

    @Test
    void jsonSet_withOddArguments_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonSet(Expressions.constant("{}"), "$.key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("path-value pairs");
    }

    // ========================================
    // JSON_INSERT tests
    // ========================================

    @Test
    void jsonInsert_withNewPath_shouldInsertValue() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\"}";
        JsonValueExpression result = jsonInsert(Expressions.constant(doc), "$.age", 25);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "25");
    }

    @Test
    void jsonInsert_withExistingPath_shouldNotReplace() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30}";
        JsonValueExpression result = jsonInsert(Expressions.constant(doc), "$.age", 25);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "30");
        assertThat(modified).doesNotContain("25");
    }

    @Test
    void jsonInsert_withMultiplePaths_shouldInsertAll() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\"}";
        StringExpression result = jsonInsert(Expressions.constant(doc), "$.age", 25, "$.city", "Seoul");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "25");
        assertThat(modified).contains("\"city\"", "\"Seoul\"");
    }

    // ========================================
    // JSON_REPLACE tests
    // ========================================

    @Test
    void jsonReplace_withExistingPath_shouldReplaceValue() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30}";
        JsonValueExpression result = jsonReplace(Expressions.constant(doc), "$.age", 31);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "31");
        assertThat(modified).doesNotContain("30");
    }

    @Test
    void jsonReplace_withNonExistingPath_shouldNotInsert() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\"}";
        JsonValueExpression result = jsonReplace(Expressions.constant(doc), "$.age", 25);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).doesNotContain("\"age\"");
        assertThat(modified).doesNotContain("25");
    }

    @Test
    void jsonReplace_withMultiplePaths_shouldReplaceAll() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30, \"city\": \"Seoul\"}";
        StringExpression result = jsonReplace(Expressions.constant(doc), "$.age", 31, "$.city", "Busan");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"age\"", "31");
        assertThat(modified).contains("\"city\"", "\"Busan\"");
    }

    // ========================================
    // JSON_REMOVE tests
    // ========================================

    @Test
    void jsonRemove_withSinglePath_shouldRemoveValue() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30, \"city\": \"Seoul\"}";
        StringExpression result = jsonRemove(Expressions.constant(doc), "$.age");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"name\"", "\"John\"");
        assertThat(modified).doesNotContain("\"age\"");
        assertThat(modified).contains("\"city\"", "\"Seoul\"");
    }

    @Test
    void jsonRemove_withMultiplePaths_shouldRemoveAll() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\", \"age\": 30, \"city\": \"Seoul\"}";
        JsonValueExpression result = jsonRemove(Expressions.constant(doc), "$.age", "$.city");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"name\"", "\"John\"");
        assertThat(modified).doesNotContain("\"age\"");
        assertThat(modified).doesNotContain("\"city\"");
    }

    @Test
    void jsonRemove_fromArray_shouldRemoveElement() throws SQLException {
        // Given
        String doc = "[\"a\", \"b\", \"c\", \"d\"]";
        StringExpression result = jsonRemove(Expressions.constant(doc), "$[1]");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"a\"", "\"c\"", "\"d\"");
        assertThat(modified).doesNotContain("\"b\"");
    }

    // ========================================
    // JSON_ARRAY_APPEND tests
    // ========================================

    @Test
    void jsonArrayAppend_withRootPath_shouldAppendToArray() throws SQLException {
        // Given
        String doc = "[1, 2, 3]";
        JsonArrayExpression result = jsonArrayAppend(Expressions.constant(doc), "$", 4);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).isIn("[1, 2, 3, 4]", "[1,2,3,4]");
    }

    @Test
    void jsonArrayAppend_withNestedArray_shouldAppendToNestedArray() throws SQLException {
        // Given
        String doc = "{\"tags\": [\"java\", \"kotlin\"]}";
        JsonArrayExpression result = jsonArrayAppend(Expressions.constant(doc), "$.tags", "scala");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"java\"", "\"kotlin\"", "\"scala\"");
    }

    @Test
    void jsonArrayAppend_withMultiplePaths_shouldAppendToAll() throws SQLException {
        // Given
        String doc = "{\"tags1\": [\"a\"], \"tags2\": [\"x\"]}";
        JsonArrayExpression result = jsonArrayAppend(Expressions.constant(doc), "$.tags1", "b", "$.tags2", "y");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).contains("\"a\"", "\"b\"");
        assertThat(modified).contains("\"x\"", "\"y\"");
    }

    // ========================================
    // JSON_ARRAY_INSERT tests
    // ========================================

    @Test
    void jsonArrayInsert_withIndex_shouldInsertAtPosition() throws SQLException {
        // Given
        String doc = "[\"a\", \"c\", \"d\"]";
        JsonArrayExpression result = jsonArrayInsert(Expressions.constant(doc), "$[1]", "b");

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).isIn("[\"a\", \"b\", \"c\", \"d\"]", "[\"a\",\"b\",\"c\",\"d\"]");
    }

    @Test
    void jsonArrayInsert_atBeginning_shouldInsertFirst() throws SQLException {
        // Given
        String doc = "[2, 3, 4]";
        JsonArrayExpression result = jsonArrayInsert(Expressions.constant(doc), "$[0]", 1);

        // When
        String modified = executeScalar(result);

        // Then
        assertThat(modified).isIn("[1, 2, 3, 4]", "[1,2,3,4]");
    }

    // ========================================
    // JSON_MERGE_PATCH tests
    // ========================================

    @Test
    void jsonMergePatch_withTwoObjects_shouldMerge() throws SQLException {
        // Given
        String doc1 = "{\"a\": 1, \"b\": 2}";
        String doc2 = "{\"b\": 3, \"c\": 4}";
        StringExpression result = jsonMergePatch(Expressions.constant(doc1), doc2);

        // When
        String merged = executeScalar(result);

        // Then
        assertThat(merged).contains("\"a\"", "1");
        assertThat(merged).contains("\"b\"", "3"); // Later value wins
        assertThat(merged).contains("\"c\"", "4");
    }

    @Test
    void jsonMergePatch_withNullValue_shouldRemoveKey() throws SQLException {
        // Given
        String doc1 = "{\"a\": 1, \"b\": 2}";
        String doc2 = "{\"b\": null}";
        StringExpression result = jsonMergePatch(Expressions.constant(doc1), doc2);

        // When
        String merged = executeScalar(result);

        // Then
        assertThat(merged).contains("\"a\"", "1");
        assertThat(merged).doesNotContain("\"b\""); // b is removed by null
    }

    @Test
    void jsonMergePatch_withMultipleDocuments_shouldMergeAll() throws SQLException {
        // Given
        String doc1 = "{\"a\": 1}";
        String doc2 = "{\"b\": 2}";
        String doc3 = "{\"c\": 3}";
        StringExpression result = jsonMergePatch(Expressions.constant(doc1), doc2, doc3);

        // When
        String merged = executeScalar(result);

        // Then
        assertThat(merged).contains("\"a\"", "1");
        assertThat(merged).contains("\"b\"", "2");
        assertThat(merged).contains("\"c\"", "3");
    }

    // ========================================
    // JSON_MERGE_PRESERVE tests
    // ========================================

    @Test
    void jsonMergePreserve_withDuplicateKeys_shouldPreserveAsArray() throws SQLException {
        // Given
        String doc1 = "{\"a\": 1}";
        String doc2 = "{\"a\": 2}";
        StringExpression result = jsonMergePreserve(Expressions.constant(doc1), Expressions.constant(doc2));

        // When
        String merged = executeScalar(result);

        // Then
        assertThat(merged).contains("\"a\"");
        assertThat(merged).contains("[1, 2]");
    }

    @Test
    void jsonMergePreserve_withArrays_shouldConcatenate() throws SQLException {
        // Given
        String doc1 = "[1, 2]";
        String doc2 = "[3, 4]";
        StringExpression result = jsonMergePreserve(Expressions.constant(doc1), Expressions.constant(doc2));

        // When
        String merged = executeScalar(result);

        // Then
        assertThat(merged).isIn("[1, 2, 3, 4]", "[1,2,3,4]");
    }

    // ========================================
    // JSON_UNQUOTE tests
    // ========================================

    @Test
    void jsonUnquote_withQuotedString_shouldUnquote() throws SQLException {
        // Given
        String quotedString = "\"hello\"";
        StringExpression result = jsonUnquote(quotedString);

        // When
        String unquoted = executeScalar(result);

        // Then
        assertThat(unquoted).isEqualTo("hello");
    }

    @Test
    void jsonUnquote_withEscapedCharacters_shouldUnescape() throws SQLException {
        // Given
        String quotedString = "\"hello\\nworld\"";
        StringExpression result = jsonUnquote(quotedString);

        // When
        String unquoted = executeScalar(result);

        // Then
        assertThat(unquoted).contains("hello");
        assertThat(unquoted).contains("world");
    }

    @Test
    void jsonUnquote_withExpression_shouldUnquote() throws SQLException {
        // Given
        String doc = "{\"name\": \"John\"}";
        StringExpression extracted = Expressions.stringTemplate("json_extract({0}, '$.name')", Expressions.constant(doc));
        StringExpression result = jsonUnquote(extracted);

        // When
        String unquoted = executeScalar(result);

        // Then
        assertThat(unquoted).isEqualTo("John");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonSet_inDatabase_shouldUpdateJsonColumn() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('John', 'john@test.com', '{\"age\": 30}')");

        // When
        executeUpdate("UPDATE users SET metadata = JSON_SET(metadata, '$.age', 31) WHERE name = 'John'");
        String metadata = executeScalar("SELECT metadata FROM users WHERE name = 'John'");

        // Then
        assertThat(metadata).contains("\"age\"", "31");
    }

    @Test
    void jsonArrayAppend_inDatabase_shouldAppendToArray() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, tags) VALUES " +
            "('Product', 100.00, '[\"tag1\", \"tag2\"]')");

        // When
        executeUpdate("UPDATE products SET tags = JSON_ARRAY_APPEND(tags, '$', 'tag3') WHERE name = 'Product'");
        String tags = executeScalar("SELECT tags FROM products WHERE name = 'Product'");

        // Then
        assertThat(tags).contains("\"tag1\"", "\"tag2\"", "\"tag3\"");
    }

    @Test
    void jsonMergePatch_inDatabase_shouldMergeObjects() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, settings) VALUES " +
            "('Alice', 'alice@test.com', '{\"theme\": \"light\", \"lang\": \"en\"}')");

        // When
        executeUpdate("UPDATE users SET settings = JSON_MERGE_PATCH(settings, '{\"theme\": \"dark\", \"notifications\": true}') " +
            "WHERE name = 'Alice'");
        String settings = executeScalar("SELECT settings FROM users WHERE name = 'Alice'");

        // Then
        assertThat(settings).contains("\"theme\"", "\"dark\"");
        assertThat(settings).contains("\"lang\"", "\"en\"");
        assertThat(settings).contains("\"notifications\"", "true");
    }
}