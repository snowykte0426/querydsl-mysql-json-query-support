package io.github.snowykte0426.querydsl.mysql.json.core.validation;

/**
 * Runtime exception thrown when JSON validation fails.
 * <p>
 * This exception is thrown by {@link JsonValidator} when:
 * <ul>
 * <li>Invalid JSON syntax is detected</li>
 * <li>Object serialization to JSON fails</li>
 * <li>JSON parsing encounters an error</li>
 * </ul>
 *
 * @since 0.1.0-Beta.1
 */
public class JsonValidationException extends RuntimeException {

    private final String invalidInput;

    /**
     * Constructs a new JSON validation exception with the specified message.
     *
     * @param message
     *            the detail message explaining why validation failed
     */
    public JsonValidationException(String message) {
        super(message);
        this.invalidInput = null;
    }

    /**
     * Constructs a new JSON validation exception with the specified message and
     * cause.
     *
     * @param message
     *            the detail message explaining why validation failed
     * @param cause
     *            the underlying cause of the validation failure
     */
    public JsonValidationException(String message, Throwable cause) {
        super(message, cause);
        this.invalidInput = null;
    }

    /**
     * Constructs a new JSON validation exception with the specified message and
     * invalid input.
     *
     * @param message
     *            the detail message explaining why validation failed
     * @param invalidInput
     *            the input value that failed validation
     */
    public JsonValidationException(String message, String invalidInput) {
        super(message + " (input: " + truncate(invalidInput) + ")");
        this.invalidInput = invalidInput;
    }

    /**
     * Constructs a new JSON validation exception with the specified message,
     * invalid input, and cause.
     *
     * @param message
     *            the detail message explaining why validation failed
     * @param invalidInput
     *            the input value that failed validation
     * @param cause
     *            the underlying cause of the validation failure
     */
    public JsonValidationException(String message, String invalidInput, Throwable cause) {
        super(message + " (input: " + truncate(invalidInput) + ")", cause);
        this.invalidInput = invalidInput;
    }

    /**
     * Returns the invalid input that caused the validation failure, if available.
     *
     * @return the invalid input, or {@code null} if not available
     */
    public String getInvalidInput() {
        return invalidInput;
    }

    /**
     * Truncates a string to a maximum length for error messages.
     *
     * @param input
     *            the input to truncate
     * @return the truncated string
     */
    private static String truncate(String input) {
        if (input == null) {
            return "null";
        }
        if (input.length() <= 100) {
            return input;
        }
        return input.substring(0, 100) + "... (truncated)";
    }
}
