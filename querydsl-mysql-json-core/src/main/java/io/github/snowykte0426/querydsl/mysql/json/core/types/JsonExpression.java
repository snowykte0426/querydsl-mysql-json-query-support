package io.github.snowykte0426.querydsl.mysql.json.core.types;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;

/**
 * Base expression class for MySQL JSON operations.
 *
 * <p>
 * This class serves as the foundation for all JSON-related expressions in
 * QueryDSL, providing common JSON operations and type-safe method chaining
 * capabilities.
 * </p>
 *
 * <p>
 * JSON expressions can represent:
 * <ul>
 * <li>JSON column references</li>
 * <li>JSON function results</li>
 * <li>JSON literals and constants</li>
 * </ul>
 *
 * @param <T>
 *            the Java type this expression represents
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public abstract class JsonExpression<T> extends SimpleExpression<T> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a JsonExpression with the specified type.
     *
     * @param type
     *            the Java type
     */
    protected JsonExpression(Class<? extends T> type) {
        super(Expressions.constant(null));
    }

    /**
     * Constructs a JsonExpression wrapping another expression.
     *
     * @param mixin
     *            the underlying expression
     */
    protected JsonExpression(Expression<T> mixin) {
        super(mixin);
    }

    /**
     * Extracts data from this JSON document at the specified path.
     *
     * <p>
     * SQL: {@code JSON_EXTRACT(json_doc, path)}
     * </p>
     *
     * @param path
     *            JSON path expression (e.g., "$.key.subkey")
     * @return expression representing the extracted JSON value
     */
    public JsonExpression<String> jsonExtract(String path) {
        return JsonExpression.jsonExtract(this, path);
    }

    /**
     * Extracts data from this JSON document at the specified paths.
     *
     * <p>
     * SQL: {@code JSON_EXTRACT(json_doc, path1, path2, ...)}
     * </p>
     *
     * @param paths
     *            JSON path expressions
     * @return expression representing the extracted JSON value
     */
    public JsonExpression<String> jsonExtract(String... paths) {
        return JsonExpression.jsonExtract(this, paths);
    }

    /**
     * Extracts and unquotes a value from this JSON document.
     *
     * <p>
     * SQL: {@code json_doc ->> path}
     * </p>
     *
     * @param path
     *            JSON path expression
     * @return string expression with unquoted value
     */
    public StringExpression jsonUnquoteExtract(String path) {
        return JsonExpression.jsonUnquoteExtract(this, path);
    }

    /**
     * Returns the maximum depth of this JSON document.
     *
     * <p>
     * SQL: {@code JSON_DEPTH(json_doc)}
     * </p>
     *
     * @return integer expression representing depth
     */
    public SimpleExpression<Integer> jsonDepth() {
        return JsonExpression.jsonDepth(this);
    }

    /**
     * Returns the number of elements in this JSON document.
     *
     * <p>
     * SQL: {@code JSON_LENGTH(json_doc)}
     * </p>
     *
     * @return integer expression representing length
     */
    public SimpleExpression<Integer> jsonLength() {
        return JsonExpression.jsonLength(this);
    }

    /**
     * Returns the number of elements at the specified path in this JSON document.
     *
     * <p>
     * SQL: {@code JSON_LENGTH(json_doc, path)}
     * </p>
     *
     * @param path
     *            JSON path expression
     * @return integer expression representing length
     */
    public SimpleExpression<Integer> jsonLength(String path) {
        return JsonExpression.jsonLength(this, path);
    }

    /**
     * Returns the type of this JSON value.
     *
     * <p>
     * SQL: {@code JSON_TYPE(json_val)}
     * </p>
     *
     * @return string expression representing JSON type
     */
    public StringExpression jsonType() {
        return JsonExpression.jsonType(this);
    }

    /**
     * Checks if this value is valid JSON.
     *
     * <p>
     * SQL: {@code JSON_VALID(val)}
     * </p>
     *
     * @return boolean expression
     */
    public SimpleExpression<Boolean> jsonValid() {
        return JsonExpression.jsonValid(this);
    }

    /**
     * Formats this JSON document in a readable format.
     *
     * <p>
     * SQL: {@code JSON_PRETTY(json_doc)}
     * </p>
     *
     * @return string expression with formatted JSON
     */
    public StringExpression jsonPretty() {
        return JsonExpression.jsonPretty(this);
    }

    // ========================================
    // Static factory methods
    // ========================================

    /**
     * Creates a JSON expression from a constant value.
     *
     * @param value
     *            the constant value
     * @param <T>
     *            value type
     * @return JSON expression
     */
    public static <T> JsonExpression<T> constant(T value) {
        return new JsonConstant<>(value);
    }

    /**
     * Extracts data from a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return extracted JSON expression
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String path) {
        StringExpression expr = Expressions
                .stringTemplate("json_extract({0}, {1})", jsonDoc, Expressions.constant(path));
        return new JsonExpressionImpl<>(expr);
    }

    /**
     * Extracts data from a JSON document at multiple paths.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param paths
     *            the JSON paths
     * @return extracted JSON expression
     */
    public static JsonExpression<String> jsonExtract(Expression<?> jsonDoc, String... paths) {
        Object[] args = new Object[paths.length + 1];
        args[0] = jsonDoc;
        for (int i = 0; i < paths.length; i++) {
            args[i + 1] = Expressions.constant(paths[i]);
        }
        StringBuilder template = new StringBuilder("json_extract({0}");
        for (int i = 0; i < paths.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");
        StringExpression expr = Expressions.stringTemplate(template.toString(), args);
        return new JsonExpressionImpl<>(expr);
    }

    /**
     * Extracts and unquotes a value from a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return unquoted string expression
     */
    public static StringExpression jsonUnquoteExtract(Expression<?> jsonDoc, String path) {
        return Expressions.stringTemplate("{0} ->> {1}", jsonDoc, Expressions.constant(path));
    }

    /**
     * Returns the maximum depth of a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return depth expression
     */
    public static SimpleExpression<Integer> jsonDepth(Expression<?> jsonDoc) {
        return Expressions.numberTemplate(Integer.class, "json_depth({0})", jsonDoc);
    }

    /**
     * Returns the number of elements in a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return length expression
     */
    public static SimpleExpression<Integer> jsonLength(Expression<?> jsonDoc) {
        return Expressions.numberTemplate(Integer.class, "json_length({0})", jsonDoc);
    }

    /**
     * Returns the number of elements at a path in a JSON document.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return length expression
     */
    public static SimpleExpression<Integer> jsonLength(Expression<?> jsonDoc, String path) {
        return Expressions.numberTemplate(Integer.class, "json_length({0}, {1})", jsonDoc, Expressions.constant(path));
    }

    /**
     * Returns the type of a JSON value.
     *
     * @param jsonValue
     *            the JSON value expression
     * @return type expression
     */
    public static StringExpression jsonType(Expression<?> jsonValue) {
        return Expressions.stringTemplate("json_type({0})", jsonValue);
    }

    /**
     * Checks if a value is valid JSON.
     *
     * @param value
     *            the value expression
     * @return validation expression
     */
    public static SimpleExpression<Boolean> jsonValid(Expression<?> value) {
        return Expressions.booleanTemplate("json_valid({0})", value);
    }

    /**
     * Formats a JSON document in readable format.
     *
     * @param jsonDoc
     *            the JSON document expression
     * @return formatted JSON expression
     */
    public static StringExpression jsonPretty(Expression<?> jsonDoc) {
        return Expressions.stringTemplate("json_pretty({0})", jsonDoc);
    }

    /**
     * Internal implementation class for wrapped expressions.
     */
    private static class JsonExpressionImpl<T> extends JsonExpression<T> {
        JsonExpressionImpl(Expression<T> mixin) {
            super(mixin);
        }

        @Override
        @Nullable
        public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
            return mixin.accept(v, context);
        }
    }

    /**
     * Internal class for JSON constant values.
     */
    private static class JsonConstant<T> extends JsonExpression<T> {
        JsonConstant(T value) {
            super(Expressions.constant(value));
        }

        @Override
        @Nullable
        public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
            return mixin.accept(v, context);
        }
    }
}
