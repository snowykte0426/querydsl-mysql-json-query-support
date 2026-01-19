package io.github.snowykte0426.querydsl.mysql.json.core.types;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;

/**
 * Expression wrapper for JSON path values.
 *
 * <p>This class wraps a {@link JsonPath} to be used as a QueryDSL expression
 * in JSON function calls. The path is rendered as a string constant in the
 * generated SQL.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * JsonPath path = JsonPath.of("$.user.name");
 * JsonPathExpression expr = JsonPathExpression.of(path);
 *
 * // Used in JSON_EXTRACT
 * queryFactory.select(
 *     JsonFunctions.jsonExtract(user.metadata, expr)
 * );
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public class JsonPathExpression extends SimpleExpression<String> {

    private static final long serialVersionUID = 1L;

    private final JsonPath path;

    /**
     * Constructs a JsonPathExpression from a JsonPath.
     *
     * @param path the JSON path
     */
    private JsonPathExpression(JsonPath path) {
        super(Expressions.constant(path.getPath()));
        this.path = Objects.requireNonNull(path, "path must not be null");
    }

    /**
     * Creates a JsonPathExpression from a JsonPath.
     *
     * @param path the JSON path
     * @return JsonPathExpression instance
     */
    public static JsonPathExpression of(JsonPath path) {
        return new JsonPathExpression(path);
    }

    /**
     * Creates a JsonPathExpression from a path string.
     *
     * @param path the JSON path string (e.g., "$.user.name")
     * @return JsonPathExpression instance
     */
    public static JsonPathExpression of(String path) {
        return new JsonPathExpression(JsonPath.of(path));
    }

    /**
     * Returns the underlying JsonPath.
     *
     * @return the JSON path
     */
    public JsonPath getPath() {
        return path;
    }

    /**
     * Returns the path string.
     *
     * @return the path as string
     */
    public String getPathString() {
        return path.getPath();
    }

    /**
     * Appends an object member to this path.
     *
     * @param key the member key
     * @return new JsonPathExpression with appended member
     */
    public JsonPathExpression member(String key) {
        return new JsonPathExpression(path.member(key));
    }

    /**
     * Appends an array element access to this path.
     *
     * @param index the array index
     * @return new JsonPathExpression with appended array access
     */
    public JsonPathExpression arrayElement(int index) {
        return new JsonPathExpression(path.arrayElement(index));
    }

    /**
     * Appends a wildcard to this path.
     *
     * @return new JsonPathExpression with wildcard
     */
    public JsonPathExpression wildcard() {
        return new JsonPathExpression(path.wildcard());
    }

    /**
     * Appends recursive descent to this path.
     *
     * @param key the member key to search recursively
     * @return new JsonPathExpression with recursive descent
     */
    public JsonPathExpression recursiveDescent(String key) {
        return new JsonPathExpression(path.recursiveDescent(key));
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        // Render as string constant
        return v.visit(ConstantImpl.create(path.getPath()), context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JsonPathExpression that = (JsonPathExpression) o;
        return path.equals(that.path);
    }

}

