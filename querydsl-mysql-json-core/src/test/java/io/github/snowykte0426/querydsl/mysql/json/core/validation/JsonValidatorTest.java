package io.github.snowykte0426.querydsl.mysql.json.core.validation;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for {@link JsonValidator}.
 */
@DisplayName("JsonValidator Tests")
class JsonValidatorTest {

    // ============ toJsonString() Tests ============

    @Test
    @DisplayName("toJsonString should convert null to 'null'")
    void toJsonString_withNull_shouldReturnNull() {
        @NotNull String result = JsonValidator.toJsonString(null);
        assertThat(result).isEqualTo("null");
    }

    @Test
    @DisplayName("toJsonString should convert simple map to JSON")
    void toJsonString_withSimpleMap_shouldReturnJson() {
        @NotNull Map<String, Object> map = Map.of("key", "value");
        String result = JsonValidator.toJsonString(map);
        assertThat(result).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("toJsonString should convert list to JSON array")
    void toJsonString_withList_shouldReturnJsonArray() {
        @NotNull List<String> list = List.of("a", "b", "c");
        String result = JsonValidator.toJsonString(list);
        assertThat(result).isEqualTo("[\"a\",\"b\",\"c\"]");
    }

    @Test
    @DisplayName("toJsonString should convert nested objects to JSON")
    void toJsonString_withNestedObjects_shouldReturnJson() {
        @NotNull Map<String, Object> nested = Map.of("user",
                Map.of("name", "John", "age", 30, "roles", List.of("admin", "user")));
        String result = JsonValidator.toJsonString(nested);
        assertThat(result).contains("\"user\"");
        assertThat(result).contains("\"name\":\"John\"");
        assertThat(result).contains("\"age\":30");
        assertThat(result).contains("\"roles\":[\"admin\",\"user\"]");
    }

    @Test
    @DisplayName("toJsonString should escape special characters")
    void toJsonString_withSpecialCharacters_shouldEscape() {
        @NotNull Map<String, String> map = Map.of("text", "line1\nline2\ttab");
        String result = JsonValidator.toJsonString(map);
        assertThat(result).contains("\\n");
        assertThat(result).contains("\\t");
    }

    @Test
    @DisplayName("toJsonString should convert string to quoted JSON string")
    void toJsonString_withString_shouldReturnQuotedString() {
        String result = JsonValidator.toJsonString("hello");
        assertThat(result).isEqualTo("\"hello\"");
    }

    @Test
    @DisplayName("toJsonString should convert number to JSON number")
    void toJsonString_withNumber_shouldReturnNumber() {
        String result = JsonValidator.toJsonString(42);
        assertThat(result).isEqualTo("42");
    }

    @Test
    @DisplayName("toJsonString should convert boolean to JSON boolean")
    void toJsonString_withBoolean_shouldReturnBoolean() {
        String result = JsonValidator.toJsonString(true);
        assertThat(result).isEqualTo("true");
    }

    // ============ validateJson() Tests ============

    @Test
    @DisplayName("validateJson should accept valid JSON object")
    void validateJson_withValidObject_shouldReturnInput() {
        @NotNull String json = "{\"key\":\"value\"}";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should accept valid JSON array")
    void validateJson_withValidArray_shouldReturnInput() {
        @NotNull String json = "[1,2,3]";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should accept valid JSON string")
    void validateJson_withValidString_shouldReturnInput() {
        @NotNull String json = "\"hello\"";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should accept valid JSON number")
    void validateJson_withValidNumber_shouldReturnInput() {
        @NotNull String json = "42";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should accept valid JSON boolean")
    void validateJson_withValidBoolean_shouldReturnInput() {
        @NotNull String json = "true";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should accept valid JSON null")
    void validateJson_withValidNull_shouldReturnInput() {
        @NotNull String json = "null";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should preserve formatting")
    void validateJson_withFormatting_shouldPreserveFormatting() {
        @NotNull String json = "{\n  \"key\": \"value\"\n}";
        @NotNull String result = JsonValidator.validateJson(json);
        assertThat(result).isEqualTo(json);
    }

    @Test
    @DisplayName("validateJson should reject invalid JSON")
    void validateJson_withInvalidJson_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.validateJson("{invalid}")).isInstanceOf(JsonValidationException.class)
                .hasMessageContaining("Invalid JSON syntax");
    }

    @Test
    @DisplayName("validateJson should reject unclosed braces")
    void validateJson_withUnclosedBraces_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.validateJson("{\"key\":\"value\""))
                .isInstanceOf(JsonValidationException.class).hasMessageContaining("Invalid JSON syntax");
    }

    @Test
    @DisplayName("validateJson should reject trailing comma")
    void validateJson_withTrailingComma_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.validateJson("{\"key\":\"value\",}"))
                .isInstanceOf(JsonValidationException.class).hasMessageContaining("Invalid JSON syntax");
    }

    @Test
    @DisplayName("validateJson should reject null input")
    void validateJson_withNull_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.validateJson(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON string cannot be null");
    }

    // ============ validateAndQuote() Tests ============

    @Test
    @DisplayName("validateAndQuote should quote simple string")
    void validateAndQuote_withSimpleString_shouldQuote() {
        String result = JsonValidator.validateAndQuote("hello");
        assertThat(result).isEqualTo("\"hello\"");
    }

    @Test
    @DisplayName("validateAndQuote should escape newline")
    void validateAndQuote_withNewline_shouldEscape() {
        String result = JsonValidator.validateAndQuote("line1\nline2");
        assertThat(result).isEqualTo("\"line1\\nline2\"");
    }

    @Test
    @DisplayName("validateAndQuote should escape tab")
    void validateAndQuote_withTab_shouldEscape() {
        String result = JsonValidator.validateAndQuote("col1\tcol2");
        assertThat(result).isEqualTo("\"col1\\tcol2\"");
    }

    @Test
    @DisplayName("validateAndQuote should escape quotes")
    void validateAndQuote_withQuotes_shouldEscape() {
        String result = JsonValidator.validateAndQuote("say \"hello\"");
        assertThat(result).isEqualTo("\"say \\\"hello\\\"\"");
    }

    @Test
    @DisplayName("validateAndQuote should escape backslash")
    void validateAndQuote_withBackslash_shouldEscape() {
        String result = JsonValidator.validateAndQuote("path\\to\\file");
        assertThat(result).isEqualTo("\"path\\\\to\\\\file\"");
    }

    @Test
    @DisplayName("validateAndQuote should handle empty string")
    void validateAndQuote_withEmptyString_shouldReturnEmptyQuoted() {
        String result = JsonValidator.validateAndQuote("");
        assertThat(result).isEqualTo("\"\"");
    }

    @Test
    @DisplayName("validateAndQuote should reject null")
    void validateAndQuote_withNull_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.validateAndQuote(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String value cannot be null");
    }

    @Test
    @DisplayName("validateAndQuote should escape unicode control characters")
    void validateAndQuote_withControlCharacters_shouldEscape() {
        String result = JsonValidator.validateAndQuote("text\u0001\u0002");
        assertThat(result).contains("\\u0001").contains("\\u0002");
    }

    // ============ isValidJson() Tests ============

    @ParameterizedTest
    @ValueSource(strings = {"{\"key\":\"value\"}", "[1,2,3]", "\"hello\"", "42", "true", "false", "null", "[]", "{}",
            "{\"nested\":{\"key\":\"value\"}}"})
    @DisplayName("isValidJson should return true for valid JSON")
    void isValidJson_withValidJson_shouldReturnTrue(String json) {
        assertThat(JsonValidator.isValidJson(json)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{invalid}", "{\"key\":\"value\",}", "{\"key\":}", "[1,2,]", "undefined", "{key:value}",
            "{'key':'value'}", "{\"key\":\"value\""})
    @DisplayName("isValidJson should return false for invalid JSON")
    void isValidJson_withInvalidJson_shouldReturnFalse(String json) {
        assertThat(JsonValidator.isValidJson(json)).isFalse();
    }

    @Test
    @DisplayName("isValidJson should return false for null")
    void isValidJson_withNull_shouldReturnFalse() {
        assertThat(JsonValidator.isValidJson(null)).isFalse();
    }

    @Test
    @DisplayName("isValidJson should return false for empty string")
    void isValidJson_withEmptyString_shouldReturnFalse() {
        assertThat(JsonValidator.isValidJson("")).isFalse();
    }

    // ============ escapeJsonString() Tests ============

    @Test
    @DisplayName("escapeJsonString should escape backslash")
    void escapeJsonString_withBackslash_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("path\\to\\file");
        assertThat(result).isEqualTo("path\\\\to\\\\file");
    }

    @Test
    @DisplayName("escapeJsonString should escape quotes")
    void escapeJsonString_withQuotes_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("say \"hello\"");
        assertThat(result).isEqualTo("say \\\"hello\\\"");
    }

    @Test
    @DisplayName("escapeJsonString should escape newline")
    void escapeJsonString_withNewline_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("line1\nline2");
        assertThat(result).isEqualTo("line1\\nline2");
    }

    @Test
    @DisplayName("escapeJsonString should escape tab")
    void escapeJsonString_withTab_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("col1\tcol2");
        assertThat(result).isEqualTo("col1\\tcol2");
    }

    @Test
    @DisplayName("escapeJsonString should escape carriage return")
    void escapeJsonString_withCarriageReturn_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("line1\rline2");
        assertThat(result).isEqualTo("line1\\rline2");
    }

    @Test
    @DisplayName("escapeJsonString should escape backspace")
    void escapeJsonString_withBackspace_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("text\bbackspace");
        assertThat(result).isEqualTo("text\\bbackspace");
    }

    @Test
    @DisplayName("escapeJsonString should escape form feed")
    void escapeJsonString_withFormFeed_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("text\fformfeed");
        assertThat(result).isEqualTo("text\\fformfeed");
    }

    @Test
    @DisplayName("escapeJsonString should escape control characters")
    void escapeJsonString_withControlCharacters_shouldEscape() {
        @NotNull String result = JsonValidator.escapeJsonString("text\u0001\u0002\u001f");
        assertThat(result).contains("\\u0001").contains("\\u0002").contains("\\u001f");
    }

    @Test
    @DisplayName("escapeJsonString should not add quotes")
    void escapeJsonString_shouldNotAddQuotes() {
        @NotNull String result = JsonValidator.escapeJsonString("hello");
        assertThat(result).isEqualTo("hello");
        assertThat(result).doesNotStartWith("\"");
        assertThat(result).doesNotEndWith("\"");
    }

    @Test
    @DisplayName("escapeJsonString should reject null")
    void escapeJsonString_withNull_shouldThrowException() {
        assertThatThrownBy(() -> JsonValidator.escapeJsonString(null)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String value cannot be null");
    }

    @Test
    @DisplayName("escapeJsonString should handle empty string")
    void escapeJsonString_withEmptyString_shouldReturnEmpty() {
        @NotNull String result = JsonValidator.escapeJsonString("");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("escapeJsonString should handle string with no special characters")
    void escapeJsonString_withNoSpecialChars_shouldReturnOriginal() {
        @NotNull String input = "hello world 123";
        @NotNull String result = JsonValidator.escapeJsonString(input);
        assertThat(result).isEqualTo(input);
    }

    // ============ SQL Injection Prevention Tests ============

    @Test
    @DisplayName("toJsonString should safely handle potential SQL injection attempts")
    void toJsonString_withSqlInjection_shouldEscape() {
        @NotNull String malicious = "'; DROP TABLE users; --";
        String result = JsonValidator.toJsonString(malicious);
        assertThat(result).isEqualTo("\"'; DROP TABLE users; --\"");

        // Test with quotes to verify escaping
        @NotNull String withQuotes = "test\"quote";
        String resultWithQuotes = JsonValidator.toJsonString(withQuotes);
        assertThat(resultWithQuotes).contains("\\\""); // Quotes should be escaped
    }

    @Test
    @DisplayName("validateAndQuote should safely handle potential SQL injection attempts")
    void validateAndQuote_withSqlInjection_shouldEscape() {
        @NotNull String malicious = "'; DROP TABLE users; --";
        String result = JsonValidator.validateAndQuote(malicious);
        assertThat(result).isEqualTo("\"'; DROP TABLE users; --\"");
    }

    @Test
    @DisplayName("escapeJsonString should safely handle potential SQL injection attempts")
    void escapeJsonString_withSqlInjection_shouldEscape() {
        @NotNull String malicious = "'; DROP TABLE users; --";
        @NotNull String result = JsonValidator.escapeJsonString(malicious);
        assertThat(result).isEqualTo("'; DROP TABLE users; --");
        // Note: escapeJsonString doesn't add quotes, just escapes special chars
    }

    // ============ JsonValidationException Tests ============

    @Test
    @DisplayName("JsonValidationException should include invalid input in message")
    void jsonValidationException_shouldIncludeInput() {
        @NotNull String invalidJson = "{invalid}";
        try {
            JsonValidator.validateJson(invalidJson);
        } catch (JsonValidationException e) {
            assertThat(e.getMessage()).contains("Invalid JSON syntax");
            assertThat(e.getMessage()).contains("{invalid}");
            assertThat(e.getInvalidInput()).isEqualTo(invalidJson);
        }
    }

    @Test
    @DisplayName("JsonValidationException should truncate long input")
    void jsonValidationException_shouldTruncateLongInput() {
        @NotNull String longInvalidJson = "{" + "a".repeat(200) + "}";
        try {
            JsonValidator.validateJson(longInvalidJson);
        } catch (JsonValidationException e) {
            assertThat(e.getMessage()).contains("truncated");
            assertThat(e.getInvalidInput()).isEqualTo(longInvalidJson);
        }
    }

    // ============ Edge Cases ============

    @Test
    @DisplayName("toJsonString should handle complex nested structures")
    void toJsonString_withComplexNesting_shouldSucceed() {
        @NotNull Map<String, Object> complex = Map.of("users",
                List.of(Map.of("id", 1, "name", "Alice", "active", true),
                        Map.of("id", 2, "name", "Bob", "active", false)),
                "metadata",
                Map.of("version", "1.0", "timestamp", 1234567890));

        String result = JsonValidator.toJsonString(complex);
        assertThat(JsonValidator.isValidJson(result)).isTrue();
    }

    @Test
    @DisplayName("validateJson should handle deeply nested JSON")
    void validateJson_withDeeplyNested_shouldSucceed() {
        @NotNull String deeplyNested = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":\"value\"}}}}}";
        @NotNull String result = JsonValidator.validateJson(deeplyNested);
        assertThat(result).isEqualTo(deeplyNested);
    }

    @Test
    @DisplayName("validateAndQuote should handle all common escape sequences")
    void validateAndQuote_withAllEscapeSequences_shouldEscape() {
        @NotNull String allEscapes = "\"\\\b\f\n\r\t";
        String result = JsonValidator.validateAndQuote(allEscapes);
        assertThat(result).contains("\\\"");
        assertThat(result).contains("\\\\");
        assertThat(result).contains("\\b");
        assertThat(result).contains("\\f");
        assertThat(result).contains("\\n");
        assertThat(result).contains("\\r");
        assertThat(result).contains("\\t");
    }
}
