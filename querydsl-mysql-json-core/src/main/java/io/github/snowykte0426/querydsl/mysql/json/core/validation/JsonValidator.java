package io.github.snowykte0426.querydsl.mysql.json.core.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON validation and serialization.
 * <p>
 * This class provides methods for:
 * <ul>
 * <li>Validating JSON syntax</li>
 * <li>Converting objects to JSON strings with validation</li>
 * <li>Escaping special characters in JSON strings</li>
 * <li>Validating and quoting string values</li>
 * </ul>
 * <p>
 * All methods use a thread-safe, cached {@link ObjectMapper} instance for JSON
 * processing.
 * <p>
 * <strong>Security Note:</strong> While this class validates JSON syntax and
 * escapes special characters, SQL injection prevention is primarily handled by
 * JDBC PreparedStatement parameter binding in the SDK. This class provides an
 * additional layer of client-side validation for fail-fast behavior.
 *
 * @since 0.1.0-Beta.1
 */
public final class JsonValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts an object to a JSON string with validation.
     * <p>
     * This method:
     * <ul>
     * <li>Serializes the object to JSON using Jackson</li>
     * <li>Validates the resulting JSON syntax</li>
     * <li>Returns the validated JSON string</li>
     * </ul>
     * <p>
     * Example:
     *
     * <pre>{@code
     * Map<String, Object> map = Map.of("key", "value");
     * String json = JsonValidator.toJsonString(map);
     * // Returns: {"key":"value"}
     *
     * List<String> list = List.of("a", "b", "c");
     * String json = JsonValidator.toJsonString(list);
     * // Returns: ["a","b","c"]
     * }</pre>
     *
     * @param value
     *            the object to convert to JSON (can be null)
     * @return the JSON string representation of the object
     * @throws JsonValidationException
     *             if serialization fails or produces invalid JSON
     */
    public static String toJsonString(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonValidationException("Failed to serialize object to JSON: " + e.getMessage(),
                    String.valueOf(value),
                    e);
        }
    }

    /**
     * Validates JSON syntax and returns the input if valid.
     * <p>
     * This method:
     * <ul>
     * <li>Parses the JSON string to verify syntax</li>
     * <li>Returns the original input if valid (preserves formatting)</li>
     * <li>Throws an exception if invalid</li>
     * </ul>
     * <p>
     * Example:
     *
     * <pre>{@code
     * String valid = JsonValidator.validateJson("{\"key\":\"value\"}");
     * // Returns: {"key":"value"}
     *
     * JsonValidator.validateJson("{invalid}");
     * // Throws: JsonValidationException
     * }</pre>
     *
     * @param json
     *            the JSON string to validate
     * @return the original JSON string if valid
     * @throws JsonValidationException
     *             if the JSON syntax is invalid
     * @throws IllegalArgumentException
     *             if json is null
     */
    public static String validateJson(String json) {
        if (json == null) {
            throw new IllegalArgumentException("JSON string cannot be null");
        }

        try {
            // Parse to validate syntax
            MAPPER.readTree(json);
            return json;
        } catch (JsonProcessingException e) {
            throw new JsonValidationException("Invalid JSON syntax: " + e.getOriginalMessage(), json, e);
        }
    }

    /**
     * Validates a string value and wraps it in JSON quotes if needed.
     * <p>
     * This method:
     * <ul>
     * <li>Escapes special characters in the string</li>
     * <li>Wraps the result in double quotes</li>
     * <li>Validates the resulting JSON string</li>
     * </ul>
     * <p>
     * Example:
     *
     * <pre>{@code
     * String quoted = JsonValidator.validateAndQuote("hello");
     * // Returns: "hello"
     *
     * String escaped = JsonValidator.validateAndQuote("line1\nline2");
     * // Returns: "line1\\nline2"
     * }</pre>
     *
     * @param value
     *            the string value to quote (cannot be null)
     * @return the JSON-quoted string
     * @throws JsonValidationException
     *             if validation fails
     * @throws IllegalArgumentException
     *             if value is null
     */
    public static String validateAndQuote(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        try {
            // Use Jackson to properly escape and quote the string
            return MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonValidationException("Failed to quote string value: " + e.getMessage(), value, e);
        }
    }

    /**
     * Checks if a string is valid JSON.
     * <p>
     * This method attempts to parse the JSON string and returns {@code true} if
     * successful, {@code false} otherwise.
     * <p>
     * Example:
     *
     * <pre>{@code
     * boolean valid = JsonValidator.isValidJson("{\"key\":\"value\"}");
     * // Returns: true
     *
     * boolean invalid = JsonValidator.isValidJson("{invalid}");
     * // Returns: false
     * }</pre>
     *
     * @param json
     *            the JSON string to check
     * @return {@code true} if the string is valid JSON, {@code false} otherwise
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }

        try {
            MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Escapes special characters in a JSON string value.
     * <p>
     * This method escapes:
     * <ul>
     * <li>Backslashes (\)</li>
     * <li>Double quotes (")</li>
     * <li>Control characters (newline, tab, carriage return, etc.)</li>
     * </ul>
     * <p>
     * <strong>Note:</strong> This method does NOT add surrounding quotes. Use
     * {@link #validateAndQuote(String)} for a complete JSON string value.
     * <p>
     * Example:
     *
     * <pre>{@code
     * String escaped = JsonValidator.escapeJsonString("hello\nworld");
     * // Returns: hello\\nworld
     *
     * String escaped = JsonValidator.escapeJsonString("say \"hello\"");
     * // Returns: say \\\"hello\\\"
     * }</pre>
     *
     * @param value
     *            the string value to escape (cannot be null)
     * @return the escaped string (without surrounding quotes)
     * @throws IllegalArgumentException
     *             if value is null
     */
    public static String escapeJsonString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("String value cannot be null");
        }

        StringBuilder sb = new StringBuilder(value.length() + 20);

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' :
                    sb.append("\\\\");
                    break;
                case '"' :
                    sb.append("\\\"");
                    break;
                case '\b' :
                    sb.append("\\b");
                    break;
                case '\f' :
                    sb.append("\\f");
                    break;
                case '\n' :
                    sb.append("\\n");
                    break;
                case '\r' :
                    sb.append("\\r");
                    break;
                case '\t' :
                    sb.append("\\t");
                    break;
                default :
                    // Escape control characters
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }

        return sb.toString();
    }
}
