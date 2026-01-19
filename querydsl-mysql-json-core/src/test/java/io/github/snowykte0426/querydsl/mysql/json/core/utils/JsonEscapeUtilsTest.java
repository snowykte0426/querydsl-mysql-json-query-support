package io.github.snowykte0426.querydsl.mysql.json.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonEscapeUtils}.
 */
class JsonEscapeUtilsTest {

    // ========================================
    // String escaping tests
    // ========================================

    @Test
    void escapeString_basicString_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("student:read");
        assertEquals("\"student:read\"", result);
    }

    @Test
    void escapeString_withDoubleQuotes_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("He said \"Hi\"");
        assertEquals("\"He said \\\"Hi\\\"\"", result);
    }

    @Test
    void escapeString_withBackslashes_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("path\\to\\file");
        assertEquals("\"path\\\\to\\\\file\"", result);
    }

    @Test
    void escapeString_withNewline_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("line1\nline2");
        assertEquals("\"line1\\nline2\"", result);
    }

    @Test
    void escapeString_withCarriageReturn_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("line1\rline2");
        assertEquals("\"line1\\rline2\"", result);
    }

    @Test
    void escapeString_withTab_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("col1\tcol2");
        assertEquals("\"col1\\tcol2\"", result);
    }

    @Test
    void escapeString_withMultipleSpecialChars_shouldEscape() {
        String result = JsonEscapeUtils.escapeString("Line 1\n\tQuoted: \"value\"\n\\path");
        assertEquals("\"Line 1\\n\\tQuoted: \\\"value\\\"\\n\\\\path\"", result);
    }

    @Test
    void escapeString_emptyString_shouldReturnEmptyQuoted() {
        String result = JsonEscapeUtils.escapeString("");
        assertEquals("\"\"", result);
    }

    @Test
    void escapeString_null_shouldReturnNull() {
        String result = JsonEscapeUtils.escapeString(null);
        assertEquals("null", result);
    }

    @Test
    void escapeString_unicodeChars_shouldNotEscape() {
        // Unicode characters should pass through unchanged
        String result = JsonEscapeUtils.escapeString("Hello 世界");
        assertEquals("\"Hello 世界\"", result);
    }

    // ========================================
    // Number escaping tests
    // ========================================

    @Test
    void escapeNumber_integer_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(42);
        assertEquals("42", result);
    }

    @Test
    void escapeNumber_negativeInteger_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(-100);
        assertEquals("-100", result);
    }

    @Test
    void escapeNumber_zero_shouldReturnZero() {
        String result = JsonEscapeUtils.escapeNumber(0);
        assertEquals("0", result);
    }

    @Test
    void escapeNumber_decimal_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(3.14);
        assertEquals("3.14", result);
    }

    @Test
    void escapeNumber_negativeDecimal_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(-99.99);
        assertEquals("-99.99", result);
    }

    @Test
    void escapeNumber_long_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(9999999999L);
        assertEquals("9999999999", result);
    }

    @Test
    void escapeNumber_float_shouldConvertToString() {
        String result = JsonEscapeUtils.escapeNumber(2.5f);
        assertEquals("2.5", result);
    }

    @Test
    void escapeNumber_null_shouldReturnNull() {
        String result = JsonEscapeUtils.escapeNumber(null);
        assertEquals("null", result);
    }

    // ========================================
    // Boolean escaping tests
    // ========================================

    @Test
    void escapeBoolean_true_shouldReturnTrue() {
        String result = JsonEscapeUtils.escapeBoolean(true);
        assertEquals("true", result);
    }

    @Test
    void escapeBoolean_false_shouldReturnFalse() {
        String result = JsonEscapeUtils.escapeBoolean(false);
        assertEquals("false", result);
    }

    // ========================================
    // Integration/realistic scenarios
    // ========================================

    @Test
    void escapeString_realisticScenario_apiKey() {
        // User's actual use case
        String result = JsonEscapeUtils.escapeString("student:read");
        assertEquals("\"student:read\"", result);
    }

    @Test
    void escapeString_realisticScenario_role() {
        String result = JsonEscapeUtils.escapeString("admin");
        assertEquals("\"admin\"", result);
    }

    @Test
    void escapeString_realisticScenario_email() {
        String result = JsonEscapeUtils.escapeString("user@example.com");
        assertEquals("\"user@example.com\"", result);
    }

    @Test
    void escapeString_realisticScenario_jsonPath() {
        String result = JsonEscapeUtils.escapeString("$.user.name");
        assertEquals("\"$.user.name\"", result);
    }

    @Test
    void escapeNumber_realisticScenario_id() {
        String result = JsonEscapeUtils.escapeNumber(12345);
        assertEquals("12345", result);
    }

    @Test
    void escapeBoolean_realisticScenario_active() {
        String result = JsonEscapeUtils.escapeBoolean(true);
        assertEquals("true", result);
    }
}
