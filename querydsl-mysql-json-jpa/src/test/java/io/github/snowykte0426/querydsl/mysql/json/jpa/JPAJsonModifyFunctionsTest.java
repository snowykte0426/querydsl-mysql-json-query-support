package io.github.snowykte0426.querydsl.mysql.json.jpa;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON modification functions in JPA environment.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>JSON_SET - Insert or update values</li>
 * <li>JSON_INSERT - Insert values without replacing</li>
 * <li>JSON_REPLACE - Replace existing values</li>
 * <li>JSON_REMOVE - Remove values at paths</li>
 * <li>JSON_ARRAY_APPEND - Append to arrays</li>
 * <li>JSON_ARRAY_INSERT - Insert into arrays</li>
 * <li>JSON_MERGE_PATCH - Merge with RFC 7386</li>
 * <li>JSON_MERGE_PRESERVE - Merge preserving duplicates</li>
 * <li>JSON_UNQUOTE - Unquote JSON strings</li>
 * </ul>
 */
@DisplayName("JPA JSON Modify Functions")
class JPAJsonModifyFunctionsTest extends AbstractJPAJsonFunctionTest {

    @Nested
    @DisplayName("JSON_SET")
    class JsonSetTests {

        @Test
        @DisplayName("should set value at existing path")
        void setValueAtExistingPath() {
            @NotNull
            String sql = "SELECT JSON_SET('{\"name\": \"John\"}', '$.name', 'Jane')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"Jane\"");
        }

        @Test
        @DisplayName("should set value at new path")
        void setValueAtNewPath() {
            @NotNull
            String sql = "SELECT JSON_SET('{\"name\": \"John\"}', '$.age', 30)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"name\"", "\"age\"", "30");
        }

        @Test
        @DisplayName("should set multiple values")
        void setMultipleValues() {
            @NotNull
            String sql = "SELECT JSON_SET('{}', '$.a', 1, '$.b', 2)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"", "\"b\"", "1", "2");
        }

        @Test
        @DisplayName("should set nested value")
        void setNestedValue() {
            @NotNull
            String sql = "SELECT JSON_SET('{\"user\": {\"name\": \"John\"}}', '$.user.age', 30)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"age\"", "30");
        }
    }

    @Nested
    @DisplayName("JSON_INSERT")
    class JsonInsertTests {

        @Test
        @DisplayName("should insert at new path")
        void insertAtNewPath() {
            @NotNull
            String sql = "SELECT JSON_INSERT('{\"name\": \"John\"}', '$.age', 30)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"age\"", "30");
        }

        @Test
        @DisplayName("should not replace existing value")
        void notReplaceExistingValue() {
            @NotNull
            String sql = "SELECT JSON_INSERT('{\"name\": \"John\"}', '$.name', 'Jane')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"John\"");
            assertThat(result).doesNotContain("\"Jane\"");
        }

        @Test
        @DisplayName("should insert multiple values")
        void insertMultipleValues() {
            @NotNull
            String sql = "SELECT JSON_INSERT('{}', '$.a', 1, '$.b', 2)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"", "\"b\"");
        }
    }

    @Nested
    @DisplayName("JSON_REPLACE")
    class JsonReplaceTests {

        @Test
        @DisplayName("should replace existing value")
        void replaceExistingValue() {
            @NotNull
            String sql = "SELECT JSON_REPLACE('{\"name\": \"John\"}', '$.name', 'Jane')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"Jane\"");
        }

        @Test
        @DisplayName("should not insert at new path")
        void notInsertAtNewPath() {
            @NotNull
            String sql = "SELECT JSON_REPLACE('{\"name\": \"John\"}', '$.age', 30)";
            String result = executeNativeQuery(sql);

            assertThat(result).doesNotContain("\"age\"");
        }

        @Test
        @DisplayName("should replace multiple values")
        void replaceMultipleValues() {
            @NotNull
            String sql = "SELECT JSON_REPLACE('{\"a\": 1, \"b\": 2}', '$.a', 10, '$.b', 20)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("10", "20");
        }
    }

    @Nested
    @DisplayName("JSON_REMOVE")
    class JsonRemoveTests {

        @Test
        @DisplayName("should remove value at path")
        void removeValueAtPath() {
            @NotNull
            String sql = "SELECT JSON_REMOVE('{\"name\": \"John\", \"age\": 30}', '$.age')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"name\"");
            assertThat(result).doesNotContain("\"age\"");
        }

        @Test
        @DisplayName("should remove multiple paths")
        void removeMultiplePaths() {
            @NotNull
            String sql = "SELECT JSON_REMOVE('{\"a\": 1, \"b\": 2, \"c\": 3}', '$.a', '$.c')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"b\"");
            assertThat(result).doesNotContain("\"a\"", "\"c\"");
        }

        @Test
        @DisplayName("should remove array element")
        void removeArrayElement() {
            @NotNull
            String sql = "SELECT JSON_REMOVE('[1, 2, 3]', '$[1]')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 3]");
        }
    }

    @Nested
    @DisplayName("JSON_ARRAY_APPEND")
    class JsonArrayAppendTests {

        @Test
        @DisplayName("should append to array")
        void appendToArray() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_APPEND('[1, 2]', '$', 3)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }

        @Test
        @DisplayName("should append to nested array")
        void appendToNestedArray() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_APPEND('{\"items\": [1, 2]}', '$.items', 3)";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("[1, 2, 3]");
        }

        @Test
        @DisplayName("should append multiple values")
        void appendMultipleValues() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_APPEND('[1]', '$', 2, '$', 3)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }
    }

    @Nested
    @DisplayName("JSON_ARRAY_INSERT")
    class JsonArrayInsertTests {

        @Test
        @DisplayName("should insert at beginning")
        void insertAtBeginning() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_INSERT('[2, 3]', '$[0]', 1)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }

        @Test
        @DisplayName("should insert at middle")
        void insertAtMiddle() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_INSERT('[1, 3]', '$[1]', 2)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }

        @Test
        @DisplayName("should insert at end")
        void insertAtEnd() {
            @NotNull
            String sql = "SELECT JSON_ARRAY_INSERT('[1, 2]', '$[2]', 3)";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }
    }

    @Nested
    @DisplayName("JSON_MERGE_PATCH")
    class JsonMergePatchTests {

        @Test
        @DisplayName("should merge objects replacing duplicates")
        void mergeObjectsReplacingDuplicates() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PATCH('{\"a\": 1, \"b\": 2}', '{\"b\": 3, \"c\": 4}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"", "\"b\"", "\"c\"");
            assertThat(result).contains("1", "3", "4");
            assertThat(result).doesNotContain("2");
        }

        @Test
        @DisplayName("should remove null values")
        void removeNullValues() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PATCH('{\"a\": 1, \"b\": 2}', '{\"b\": null}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"");
            assertThat(result).doesNotContain("\"b\"");
        }

        @Test
        @DisplayName("should merge multiple objects")
        void mergeMultipleObjects() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PATCH('{\"a\": 1}', '{\"b\": 2}', '{\"c\": 3}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"", "\"b\"", "\"c\"");
        }
    }

    @Nested
    @DisplayName("JSON_MERGE_PRESERVE")
    class JsonMergePreserveTests {

        @Test
        @DisplayName("should merge objects preserving duplicates as arrays")
        void mergeObjectsPreservingDuplicates() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PRESERVE('{\"a\": 1}', '{\"a\": 2}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("[1, 2]");
        }

        @Test
        @DisplayName("should merge arrays")
        void mergeArrays() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PRESERVE('[1, 2]', '[3, 4]')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3, 4]");
        }

        @Test
        @DisplayName("should merge multiple documents")
        void mergeMultipleDocuments() {
            @NotNull
            String sql = "SELECT JSON_MERGE_PRESERVE('[1]', '[2]', '[3]')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[1, 2, 3]");
        }
    }

    @Nested
    @DisplayName("JSON_UNQUOTE")
    class JsonUnquoteTests {

        @Test
        @DisplayName("should unquote string")
        void unquoteString() {
            @NotNull
            String sql = "SELECT JSON_UNQUOTE('\"hello\"')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("should unescape special characters")
        void unescapeSpecialCharacters() {
            @NotNull
            String sql = "SELECT JSON_UNQUOTE('\"hello\\\\nworld\"')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("hello");
        }

        @Test
        @DisplayName("should return null for null")
        void returnNullForNull() {
            @NotNull
            String sql = "SELECT JSON_UNQUOTE(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }
    }
}
