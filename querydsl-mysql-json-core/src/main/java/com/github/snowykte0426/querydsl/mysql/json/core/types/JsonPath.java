package com.github.snowykte0426.querydsl.mysql.json.core.types;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a JSON path expression following MySQL JSON path syntax.
 *
 * <p>MySQL JSON paths follow this syntax:
 * <ul>
 *   <li>{@code $} - Root element</li>
 *   <li>{@code $.key} - Object member by key</li>
 *   <li>{@code $[n]} - Array element by index</li>
 *   <li>{@code $.key.subkey} - Nested object access</li>
 *   <li>{@code $[*]} - All array elements (wildcard)</li>
 *   <li>{@code $**.key} - Recursive descent</li>
 * </ul>
 *
 * <p>Examples:
 * <pre>{@code
 * JsonPath.of("$")                    // Root
 * JsonPath.of("$.user.name")          // Nested object
 * JsonPath.of("$.users[0].email")     // Array with index
 * JsonPath.of("$.settings.*")         // Wildcard
 * }</pre>
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public final class JsonPath implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Pattern for validating MySQL JSON path syntax.
     * Simplified validation - full validation happens at MySQL level.
     */
    private static final Pattern PATH_PATTERN = Pattern.compile(
        "^\\$(?:\\.[a-zA-Z_][a-zA-Z0-9_]*|\\[\\d+\\]|\\[\\*\\]|\\.\\*|\\*\\*\\.[a-zA-Z_][a-zA-Z0-9_]*)*$"
    );

    /**
     * Root path constant.
     */
    public static final JsonPath ROOT = new JsonPath("$");

    private final String path;

    /**
     * Private constructor - use factory methods.
     *
     * @param path the JSON path string
     */
    private JsonPath(String path) {
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    /**
     * Creates a JsonPath from a path string.
     *
     * @param path the JSON path (e.g., "$.user.name")
     * @return JsonPath instance
     * @throws IllegalArgumentException if path is invalid
     */
    public static JsonPath of(String path) {
        Objects.requireNonNull(path, "path must not be null");
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid JSON path: " + path);
        }
        return new JsonPath(path);
    }

    /**
     * Creates a JsonPath without validation.
     * Use only when path is already validated or comes from trusted source.
     *
     * @param path the JSON path
     * @return JsonPath instance
     */
    public static JsonPath ofUnchecked(String path) {
        return new JsonPath(path);
    }

    /**
     * Creates a path to an object member.
     *
     * @param key the object key
     * @return JsonPath to the member
     */
    public static JsonPath member(String key) {
        return new JsonPath("$." + key);
    }

    /**
     * Creates a path to an array element.
     *
     * @param index the array index
     * @return JsonPath to the element
     */
    public static JsonPath arrayElement(int index) {
        return new JsonPath("$[" + index + "]");
    }

    /**
     * Appends a wildcard to this path.
     *
     * @return new JsonPath with wildcard
     */
    public JsonPath wildcard() {
        return new JsonPath(this.path + "[*]");
    }

    /**
     * Appends recursive descent to this path.
     *
     * @param key the member key to search recursively
     * @return new JsonPath with recursive descent
     */
    public JsonPath recursiveDescent(String key) {
        return new JsonPath(this.path + "**." + key);
    }

    /**
     * Returns the path string.
     *
     * @return the JSON path as string
     */
    public String getPath() {
        return path;
    }

    /**
     * Checks if this path is the root path.
     *
     * @return true if root path
     */
    public boolean isRoot() {
        return "$".equals(path);
    }

    /**
     * Validates a JSON path string (basic validation).
     *
     * @param path the path to validate
     * @return true if valid
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Basic validation - MySQL will do full validation
        return path.startsWith("$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonPath jsonPath = (JsonPath) o;
        return path.equals(jsonPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return path;
    }
}
