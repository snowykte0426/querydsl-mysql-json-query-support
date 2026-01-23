package io.github.snowykte0426.querydsl.mysql.json.core.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a JSON path expression following MySQL JSON path syntax.
 *
 * <p>
 * MySQL JSON paths follow this syntax:
 * <ul>
 * <li>{@code $} - Root element</li>
 * <li>{@code $.key} - Object member by key</li>
 * <li>{@code $[n]} - Array element by index</li>
 * <li>{@code $.key.subkey} - Nested object access</li>
 * <li>{@code $[*]} - All array elements (wildcard)</li>
 * <li>{@code $**.key} - Recursive descent</li>
 * </ul>
 *
 * <p>
 * Examples:
 *
 * <pre>{@code
 * JsonPath.of("$")                    // Root
 * JsonPath.of("$.user.name")          // Nested object
 * JsonPath.of("$.users[0].email")     // Array with index
 * JsonPath.of("$.settings.*")         // Wildcard
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonPath implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Pattern for validating MySQL JSON path syntax.
     *
     * <p>
     * Matches the following patterns:
     * <ul>
     * <li>{@code $} - Root element (required start)</li>
     * <li>{@code \.memberName} - Object member access (e.g., $.user.name)</li>
     * <li>{@code \[n\]} - Array index access where n is a number (e.g., $[0],
     * $[10])</li>
     * <li>{@code \[\*\]} - Array wildcard (all elements)</li>
     * <li>{@code \.\*} - Object wildcard (all members)</li>
     * <li>{@code \*\*\.memberName} - Recursive descent (e.g., $**.price)</li>
     * </ul>
     *
     * <p>
     * Examples of valid paths:
     *
     * <pre>
     * $                    ✓ Root
     * $.user.name          ✓ Nested object access
     * $.users[0].email     ✓ Array with index
     * $.settings.*         ✓ Wildcard members
     * $**.price            ✓ Recursive descent
     * </pre>
     *
     * <p>
     * <strong>Note:</strong> This is simplified client-side validation. Full
     * validation happens at MySQL level. The regex breaks down as:
     * <ul>
     * <li>{@code ^\\$} - Must start with '$' (root)</li>
     * <li>{@code (?:...)*} - Non-capturing group, repeated zero or more times</li>
     * <li>{@code \\.[a-zA-Z_][a-zA-Z0-9_]*} - Dot followed by identifier (member
     * access)</li>
     * <li>{@code \\[\\d+\\]} - Array index in brackets</li>
     * <li>{@code \\[\\*\\]} - Array wildcard in brackets</li>
     * <li>{@code \\.\\*} - Object wildcard (all members)</li>
     * <li>{@code \\*\\*\\.[a-zA-Z_][a-zA-Z0-9_]*} - Recursive descent to
     * member</li>
     * <li>{@code $} - End of string</li>
     * </ul>
     */
    private static final Pattern PATH_PATTERN = Pattern
            .compile("^\\$(?:\\.[a-zA-Z_][a-zA-Z0-9_]*|\\[\\d+\\]|\\[\\*\\]|\\.\\*|\\*\\*\\.[a-zA-Z_][a-zA-Z0-9_]*)*$");

    /**
     * Root path constant.
     */
    public static final JsonPath ROOT = new JsonPath("$");

    private final @NotNull String path;

    /**
     * Private constructor - use factory methods.
     *
     * @param path
     *            the JSON path string
     */
    private JsonPath(String path) {
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    /**
     * Creates a JsonPath from a path string.
     *
     * @param path
     *            the JSON path (e.g., "$.user.name")
     * @return JsonPath instance
     * @throws IllegalArgumentException
     *             if path is invalid
     */
    public static @NotNull JsonPath of(String path) {
        Objects.requireNonNull(path, "path must not be null");
        if (!isValidPath(path)) {
            throw new IllegalArgumentException("Invalid JSON path: " + path);
        }
        return new JsonPath(path);
    }

    /**
     * Creates a JsonPath without validation. Use only when path is already
     * validated or comes from trusted source.
     *
     * @param path
     *            the JSON path
     * @return JsonPath instance
     */
    public static @NotNull JsonPath ofUnchecked(String path) {
        return new JsonPath(path);
    }

    /**
     * Creates a path to an object member.
     *
     * @param key
     *            the object key
     * @return JsonPath to the member
     */
    public static @NotNull JsonPath member(String key) {
        return new JsonPath("$." + key);
    }

    /**
     * Creates a path to an array element.
     *
     * @param index
     *            the array index
     * @return JsonPath to the element
     */
    public static @NotNull JsonPath arrayElement(int index) {
        return new JsonPath("$[" + index + "]");
    }

    /**
     * Appends a wildcard to this path.
     *
     * @return new JsonPath with wildcard
     */
    public @NotNull JsonPath wildcard() {
        return new JsonPath(this.path + "[*]");
    }

    /**
     * Appends recursive descent to this path.
     *
     * @param key
     *            the member key to search recursively
     * @return new JsonPath with recursive descent
     */
    public @NotNull JsonPath recursiveDescent(String key) {
        return new JsonPath(this.path + "**." + key);
    }

    /**
     * Returns the path string.
     *
     * @return the JSON path as string
     */
    public @NotNull String getPath() {
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
     * @param path
     *            the path to validate
     * @return true if valid
     */
    public static boolean isValidPath(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        // Basic validation - MySQL will do full validation
        return path.startsWith("$");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        @NotNull
        JsonPath jsonPath = (JsonPath) o;
        return path.equals(jsonPath.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public @NotNull String toString() {
        return path;
    }
}
