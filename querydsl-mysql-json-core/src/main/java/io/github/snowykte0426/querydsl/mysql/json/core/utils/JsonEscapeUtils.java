package io.github.snowykte0426.querydsl.mysql.json.core.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for escaping plain Java values to JSON literals.
 *
 * <p>
 * This class helps users who want to use plain strings, numbers, or booleans
 * with JSON functions without manually escaping them.
 * </p>
 *
 * @since 0.1.0-Beta.4
 */
public final class JsonEscapeUtils {

    private JsonEscapeUtils() {
        // Utility class
    }

    /**
     * Escapes a plain string to a JSON string literal.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * escapeString("student:read")  // Returns: "\"student:read\""
     * escapeString("He said \"Hi\"")  // Returns: "\"He said \\\"Hi\\\"\""
     * }</pre>
     *
     * @param plainString
     *            the plain string (e.g., "student:read")
     * @return JSON escaped string (e.g., "\"student:read\"")
     */
    public static @NotNull String escapeString(@Nullable String plainString) {
        if (plainString == null) {
            return "null";
        }

        @NotNull String escaped = plainString.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");

        return "\"" + escaped + "\"";
    }

    /**
     * Converts a number to JSON number literal.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * escapeNumber(42)  // Returns: "42"
     * escapeNumber(3.14)  // Returns: "3.14"
     * escapeNumber(null)  // Returns: "null"
     * }</pre>
     *
     * @param number
     *            the number
     * @return JSON number literal
     */
    public static String escapeNumber(@Nullable Number number) {
        return number == null ? "null" : number.toString();
    }

    /**
     * Converts a boolean to JSON boolean literal.
     *
     * <p>
     * Example:
     *
     * <pre>{@code
     * escapeBoolean(true)  // Returns: "true"
     * escapeBoolean(false)  // Returns: "false"
     * }</pre>
     *
     * @param bool
     *            the boolean value
     * @return JSON boolean literal ("true" or "false")
     */
    public static @NotNull String escapeBoolean(boolean bool) {
        return String.valueOf(bool);
    }
}
