package com.github.snowykte0426.querydsl.mysql.json.core.expressions;

import com.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Expression class for JSON object operations.
 *
 * <p>This class provides specialized operations for JSON objects,
 * including object creation, key extraction, and object-specific functions.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 * // Create a JSON object
 * JsonObjectExpression obj = JsonObjectExpression.builder()
 *     .put("name", "John")
 *     .put("age", 30)
 *     .build();
 *
 * // Get object keys
 * JsonArrayExpression keys = obj.keys();
 *
 * // Aggregate into JSON object
 * JsonObjectExpression result = JsonObjectExpression.aggregate(
 *     user.id, user.name
 * );
 * }</pre>
 *
 * @author snowykte0426
 * @since 1.0.0
 */
public class JsonObjectExpression extends JsonExpression<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a JsonObjectExpression.
     *
     * @param mixin the underlying expression
     */
    protected JsonObjectExpression(Expression<String> mixin) {
        super(mixin);
    }

    /**
     * Creates a JSON object from key-value pairs.
     *
     * <p>SQL: {@code JSON_OBJECT(key1, val1, key2, val2, ...)}</p>
     *
     * @param keyValuePairs alternating keys and values
     * @return JsonObjectExpression
     * @throws IllegalArgumentException if odd number of arguments
     */
    public static JsonObjectExpression create(Object... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException(
                "JSON_OBJECT requires an even number of arguments (key-value pairs)"
            );
        }

        Expression<?>[] args = new Expression<?>[keyValuePairs.length];
        for (int i = 0; i < keyValuePairs.length; i++) {
            args[i] = keyValuePairs[i] instanceof Expression
                ? (Expression<?>) keyValuePairs[i]
                : Expressions.constant(keyValuePairs[i]);
        }

        return new JsonObjectExpression(
            Expressions.stringTemplate("json_object({0})", (Object[]) args)
        );
    }

    /**
     * Creates an empty JSON object.
     *
     * <p>SQL: {@code JSON_OBJECT()}</p>
     *
     * @return empty JsonObjectExpression
     */
    public static JsonObjectExpression empty() {
        return new JsonObjectExpression(
            Expressions.stringTemplate("json_object()")
        );
    }

    /**
     * Returns the keys from this JSON object.
     *
     * <p>SQL: {@code JSON_KEYS(json_doc)}</p>
     *
     * @return JsonArrayExpression containing the keys
     */
    public JsonArrayExpression keys() {
        return JsonArrayExpression.wrap(
            Expressions.stringTemplate("json_keys({0})", this)
        );
    }

    /**
     * Returns the keys from a specific path in this JSON object.
     *
     * <p>SQL: {@code JSON_KEYS(json_doc, path)}</p>
     *
     * @param path the JSON path
     * @return JsonArrayExpression containing the keys
     */
    public JsonArrayExpression keys(String path) {
        return JsonArrayExpression.wrap(
            Expressions.stringTemplate(
                "json_keys({0}, {1})",
                this,
                Expressions.constant(path)
            )
        );
    }

    /**
     * Checks if this object contains a specific value.
     *
     * <p>SQL: {@code JSON_CONTAINS(json_doc, val)}</p>
     *
     * @param value the value to search for
     * @return boolean expression
     */
    public SimpleExpression<Boolean> contains(Object value) {
        Expression<?> valueExpr = value instanceof Expression
            ? (Expression<?>) value
            : Expressions.constant(value);

        return Expressions.booleanTemplate(
            "json_contains({0}, {1})",
            this,
            valueExpr
        );
    }

    /**
     * Checks if this object contains a specific value at a path.
     *
     * <p>SQL: {@code JSON_CONTAINS(json_doc, val, path)}</p>
     *
     * @param value the value to search for
     * @param path the JSON path
     * @return boolean expression
     */
    public SimpleExpression<Boolean> contains(Object value, String path) {
        Expression<?> valueExpr = value instanceof Expression
            ? (Expression<?>) value
            : Expressions.constant(value);

        return Expressions.booleanTemplate(
            "json_contains({0}, {1}, {2})",
            this,
            valueExpr,
            Expressions.constant(path)
        );
    }

    /**
     * Merges this JSON object with another using RFC 7386 merge patch semantics.
     *
     * <p>SQL: {@code JSON_MERGE_PATCH(json_doc1, json_doc2, ...)}</p>
     *
     * @param others the JSON documents to merge
     * @return merged JsonObjectExpression
     */
    public JsonObjectExpression mergePatch(Expression<?>... others) {
        Expression<?>[] args = new Expression<?>[others.length + 1];
        args[0] = this;
        System.arraycopy(others, 0, args, 1, others.length);

        return new JsonObjectExpression(
            Expressions.stringTemplate("json_merge_patch({0})", (Object[]) args)
        );
    }

    /**
     * Merges this JSON object with others, preserving duplicate keys.
     *
     * <p>SQL: {@code JSON_MERGE_PRESERVE(json_doc1, json_doc2, ...)}</p>
     *
     * @param others the JSON documents to merge
     * @return merged JsonObjectExpression
     */
    public JsonObjectExpression mergePreserve(Expression<?>... others) {
        Expression<?>[] args = new Expression<?>[others.length + 1];
        args[0] = this;
        System.arraycopy(others, 0, args, 1, others.length);

        return new JsonObjectExpression(
            Expressions.stringTemplate("json_merge_preserve({0})", (Object[]) args)
        );
    }

    /**
     * Aggregates key-value pairs into a JSON object.
     *
     * <p>SQL: {@code JSON_OBJECTAGG(key, value)}</p>
     *
     * @param key the key expression
     * @param value the value expression
     * @return JsonObjectExpression
     */
    public static JsonObjectExpression aggregate(Expression<?> key, Expression<?> value) {
        return new JsonObjectExpression(
            Expressions.stringTemplate("json_objectagg({0}, {1})", key, value)
        );
    }

    /**
     * Wraps an existing expression as a JsonObjectExpression.
     *
     * @param expression the expression to wrap
     * @return JsonObjectExpression
     */
    public static JsonObjectExpression wrap(Expression<String> expression) {
        return new JsonObjectExpression(expression);
    }

    /**
     * Returns a builder for constructing JSON objects fluently.
     *
     * @return new JsonObjectBuilder
     */
    public static JsonObjectBuilder builder() {
        return new JsonObjectBuilder();
    }

    /**
     * Builder for fluent JSON object construction.
     */
    public static class JsonObjectBuilder {
        private final List<Object> keyValuePairs = new ArrayList<>();

        /**
         * Adds a key-value pair to the object.
         *
         * @param key the key
         * @param value the value
         * @return this builder
         */
        public JsonObjectBuilder put(String key, Object value) {
            keyValuePairs.add(key);
            keyValuePairs.add(value);
            return this;
        }

        /**
         * Builds the JsonObjectExpression.
         *
         * @return JsonObjectExpression
         */
        public JsonObjectExpression build() {
            return JsonObjectExpression.create(keyValuePairs.toArray());
        }
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return ((Expression<String>) mixin).accept(v, context);
    }
}
