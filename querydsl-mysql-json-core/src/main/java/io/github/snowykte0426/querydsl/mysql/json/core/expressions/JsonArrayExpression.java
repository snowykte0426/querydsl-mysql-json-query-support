package io.github.snowykte0426.querydsl.mysql.json.core.expressions;

import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.jetbrains.annotations.Nullable;

/**
 * Expression class for JSON array operations.
 *
 * <p>
 * This class provides specialized operations for JSON arrays, including array
 * manipulation, element access, and array-specific functions.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Create a JSON array
 * JsonArrayExpression arr = JsonArrayExpression.create("value1", "value2");
 *
 * // Append to array
 * JsonArrayExpression updated = arr.append("$.path", "newValue");
 *
 * // Check array membership
 * BooleanExpression isMember = arr.memberOf("searchValue");
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public class JsonArrayExpression extends JsonExpression<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a JsonArrayExpression.
     *
     * @param mixin
     *            the underlying expression
     */
    protected JsonArrayExpression(Expression<String> mixin) {
        super(mixin);
    }

    /**
     * Creates a JSON array from values.
     *
     * <p>
     * SQL: {@code JSON_ARRAY(val1, val2, ...)}
     * </p>
     *
     * @param values
     *            the array values
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression create(Object... values) {
        if (values.length == 0) {
            return empty();
        }

        Expression<?>[] args = new Expression<?>[values.length];
        for (int i = 0; i < values.length; i++) {
            args[i] = values[i] instanceof Expression ? (Expression<?>) values[i] : Expressions.constant(values[i]);
        }

        // Build template with proper number of placeholders
        StringBuilder template = new StringBuilder("json_array(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                template.append(", ");
            template.append("{").append(i).append("}");
        }
        template.append(")");

        return new JsonArrayExpression(Expressions.stringTemplate(template.toString(), (Object[]) args));
    }

    /**
     * Creates an empty JSON array.
     *
     * <p>
     * SQL: {@code JSON_ARRAY()}
     * </p>
     *
     * @return empty JsonArrayExpression
     */
    public static JsonArrayExpression empty() {
        return new JsonArrayExpression(Expressions.stringTemplate("json_array()"));
    }

    /**
     * Appends values to the end of JSON arrays in this document.
     *
     * <p>
     * SQL: {@code JSON_ARRAY_APPEND(json_doc, path, val)}
     * </p>
     *
     * @param path
     *            the JSON path where to append
     * @param value
     *            the value to append
     * @return updated JsonArrayExpression
     */
    public JsonArrayExpression append(String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return new JsonArrayExpression(Expressions
                .stringTemplate("json_array_append({0}, {1}, {2})", this, Expressions.constant(path), valueExpr));
    }

    /**
     * Inserts a value into a JSON array at a specific position.
     *
     * <p>
     * SQL: {@code JSON_ARRAY_INSERT(json_doc, path, val)}
     * </p>
     *
     * @param path
     *            the JSON path where to insert (e.g., "$[0]")
     * @param value
     *            the value to insert
     * @return updated JsonArrayExpression
     */
    public JsonArrayExpression insert(String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return new JsonArrayExpression(Expressions
                .stringTemplate("json_array_insert({0}, {1}, {2})", this, Expressions.constant(path), valueExpr));
    }

    /**
     * Tests whether a value is a member of this JSON array.
     *
     * <p>
     * SQL: {@code value MEMBER OF(json_array)}
     * </p>
     *
     * @param value
     *            the value to test
     * @return boolean expression
     */
    public SimpleExpression<Boolean> memberOf(Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return Expressions.booleanTemplate("{0} member of({1})", valueExpr, this);
    }

    /**
     * Checks if this array overlaps with another JSON document.
     *
     * <p>
     * SQL: {@code JSON_OVERLAPS(json_doc1, json_doc2)}
     * </p>
     *
     * @param other
     *            the other JSON document
     * @return boolean expression
     */
    public SimpleExpression<Boolean> overlaps(Expression<?> other) {
        return Expressions.booleanTemplate("json_overlaps({0}, {1})", this, other);
    }

    /**
     * Checks if this array overlaps with a JSON value.
     *
     * @param jsonValue
     *            the JSON value as string
     * @return boolean expression
     */
    public SimpleExpression<Boolean> overlaps(String jsonValue) {
        return overlaps(Expressions.constant(jsonValue));
    }

    /**
     * Aggregates values into a JSON array.
     *
     * <p>
     * SQL: {@code JSON_ARRAYAGG(col_or_expr)}
     * </p>
     *
     * @param expression
     *            the expression to aggregate
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression aggregate(Expression<?> expression) {
        return new JsonArrayExpression(Expressions.stringTemplate("json_arrayagg({0})", expression));
    }

    /**
     * Wraps an existing expression as a JsonArrayExpression.
     *
     * @param expression
     *            the expression to wrap
     * @return JsonArrayExpression
     */
    public static JsonArrayExpression wrap(Expression<String> expression) {
        return new JsonArrayExpression(expression);
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return ((Expression<String>) mixin).accept(v, context);
    }
}
