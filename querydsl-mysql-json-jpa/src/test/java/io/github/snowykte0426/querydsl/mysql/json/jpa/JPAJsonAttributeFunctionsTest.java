package io.github.snowykte0426.querydsl.mysql.json.jpa;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON attribute functions in JPA environment.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>JSON_DEPTH - Maximum depth of JSON document</li>
 * <li>JSON_LENGTH - Number of elements</li>
 * <li>JSON_TYPE - Type of JSON value</li>
 * <li>JSON_VALID - Validate JSON</li>
 * </ul>
 */
@DisplayName("JPA JSON Attribute Functions")
class JPAJsonAttributeFunctionsTest extends AbstractJPAJsonFunctionTest {

    @Nested
    @DisplayName("JSON_DEPTH")
    class JsonDepthTests {

        @Test
        @DisplayName("should return 1 for empty object")
        void depth1ForEmptyObject() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('{}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 1 for empty array")
        void depth1ForEmptyArray() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('[]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 1 for scalar")
        void depth1ForScalar() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('\"hello\"')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 2 for simple array")
        void depth2ForSimpleArray() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return 2 for simple object")
        void depth2ForSimpleObject() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return 3 for nested structure")
        void depth3ForNestedStructure() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('{\"a\": [1, 2]}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return 4 for deeply nested structure")
        void depth4ForDeeplyNested() {
            @NotNull
            String sql = "SELECT JSON_DEPTH('{\"a\": {\"b\": [1]}}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("JSON_LENGTH")
    class JsonLengthTests {

        @Test
        @DisplayName("should return 0 for empty array")
        void length0ForEmptyArray() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('[]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return 0 for empty object")
        void length0ForEmptyObject() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('{}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return 1 for scalar")
        void length1ForScalar() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('\"hello\"')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return array length")
        void returnArrayLength() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('[1, 2, 3, 4, 5]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("should return object key count")
        void returnObjectKeyCount() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('{\"a\": 1, \"b\": 2, \"c\": 3}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return length at path")
        void returnLengthAtPath() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('{\"items\": [1, 2, 3]}', '$.items')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("JSON_TYPE")
    class JsonTypeTests {

        @Test
        @DisplayName("should return OBJECT for objects")
        void returnObjectForObjects() {
            @NotNull
            String sql = "SELECT JSON_TYPE('{\"a\": 1}')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("OBJECT");
        }

        @Test
        @DisplayName("should return ARRAY for arrays")
        void returnArrayForArrays() {
            @NotNull
            String sql = "SELECT JSON_TYPE('[1, 2, 3]')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("ARRAY");
        }

        @Test
        @DisplayName("should return STRING for strings")
        void returnStringForStrings() {
            @NotNull
            String sql = "SELECT JSON_TYPE('\"hello\"')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("STRING");
        }

        @Test
        @DisplayName("should return INTEGER for integers")
        void returnIntegerForIntegers() {
            @NotNull
            String sql = "SELECT JSON_TYPE('123')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("INTEGER");
        }

        @Test
        @DisplayName("should return DOUBLE for decimals")
        void returnDoubleForDecimals() {
            @NotNull
            String sql = "SELECT JSON_TYPE('3.14')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("DOUBLE");
        }

        @Test
        @DisplayName("should return BOOLEAN for true")
        void returnBooleanForTrue() {
            @NotNull
            String sql = "SELECT JSON_TYPE('true')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("BOOLEAN");
        }

        @Test
        @DisplayName("should return BOOLEAN for false")
        void returnBooleanForFalse() {
            @NotNull
            String sql = "SELECT JSON_TYPE('false')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("BOOLEAN");
        }

        @Test
        @DisplayName("should return NULL for null")
        void returnNullForNull() {
            @NotNull
            String sql = "SELECT JSON_TYPE('null')";
            @Nullable
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("NULL");
        }
    }

    @Nested
    @DisplayName("JSON_VALID")
    class JsonValidTests {

        @Test
        @DisplayName("should return 1 for valid JSON object")
        void return1ForValidObject() {
            @NotNull
            String sql = "SELECT JSON_VALID('{\"a\": 1}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 1 for valid JSON array")
        void return1ForValidArray() {
            @NotNull
            String sql = "SELECT JSON_VALID('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 1 for valid JSON string")
        void return1ForValidString() {
            @NotNull
            String sql = "SELECT JSON_VALID('\"hello\"')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return 0 for invalid JSON")
        void return0ForInvalidJson() {
            @NotNull
            String sql = "SELECT JSON_VALID('invalid')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return 0 for incomplete JSON")
        void return0ForIncompleteJson() {
            // Use a clearly invalid JSON string (missing closing bracket)
            // Using array to avoid colon which Hibernate might interpret as parameter
            @NotNull
            String sql = "SELECT JSON_VALID('[1, 2')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return null for null input")
        void returnNullForNullInput() {
            @NotNull
            String sql = "SELECT JSON_VALID(NULL)";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Convenience Methods")
    class ConvenienceMethodTests {

        @Test
        @DisplayName("isEmpty should return true for empty array")
        void isEmptyForEmptyArray() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('[]') = 0";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("isEmpty should return true for empty object")
        void isEmptyForEmptyObject() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('{}') = 0";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("isNotEmpty should return true for non-empty")
        void isNotEmptyForNonEmpty() {
            @NotNull
            String sql = "SELECT JSON_LENGTH('[1, 2, 3]') > 0";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("isArray should return true for arrays")
        void isArrayForArrays() {
            @NotNull
            String sql = "SELECT JSON_TYPE('[1, 2, 3]') = 'ARRAY'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("isObject should return true for objects")
        void isObjectForObjects() {
            @NotNull
            String sql = "SELECT JSON_TYPE('{\"a\": 1}') = 'OBJECT'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("isScalar should return true for scalars")
        void isScalarForScalars() {
            @NotNull
            String sql = "SELECT JSON_TYPE('123') NOT IN ('ARRAY', 'OBJECT')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }
}
