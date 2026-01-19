package io.github.snowykte0426.querydsl.mysql.json.core.expressions;

import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.Nullable;

/**
 * Expression class for JSON scalar value operations.
 *
 * <p>
 * This class represents JSON scalar values (strings, numbers, booleans, null)
 * and provides operations specific to scalar JSON values.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * // Extract a scalar value
 * JsonValueExpression value = JsonValueExpression.extract(user.metadata, "$.age");
 *
 * // Quote a string as JSON
 * JsonValueExpression quoted = JsonValueExpression.quote("Hello \"World\"");
 *
 * // Unquote a JSON value
 * StringExpression unquoted = value.unquote();
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public class JsonValueExpression extends JsonExpression<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a JsonValueExpression.
     *
     * @param mixin
     *            the underlying expression
     */
    protected JsonValueExpression(Expression<String> mixin) {
        super(mixin);
    }

    /**
     * Extracts a scalar value from a JSON document.
     *
     * <p>
     * SQL: {@code JSON_VALUE(json_doc, path)}
     * </p>
     * <p>
     * Available in MySQL 8.0.21+
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @return JsonValueExpression with the extracted value
     */
    public static JsonValueExpression extract(Expression<?> jsonDoc, String path) {
        return new JsonValueExpression(
                Expressions.stringTemplate("json_value({0}, {1})", jsonDoc, Expressions.constant(path)));
    }

    /**
     * Quotes a string as a JSON value.
     *
     * <p>
     * SQL: {@code JSON_QUOTE(string)}
     * </p>
     *
     * @param value
     *            the string to quote
     * @return JsonValueExpression with quoted value
     */
    public static JsonValueExpression quote(String value) {
        return new JsonValueExpression(Expressions.stringTemplate("json_quote({0})", Expressions.constant(value)));
    }

    /**
     * Quotes a string expression as a JSON value.
     *
     * <p>
     * SQL: {@code JSON_QUOTE(string)}
     * </p>
     *
     * @param expression
     *            the string expression to quote
     * @return JsonValueExpression with quoted value
     */
    public static JsonValueExpression quote(Expression<String> expression) {
        return new JsonValueExpression(Expressions.stringTemplate("json_quote({0})", expression));
    }

    /**
     * Unquotes this JSON value.
     *
     * <p>
     * SQL: {@code JSON_UNQUOTE(json_val)}
     * </p>
     *
     * @return unquoted string expression
     */
    public StringExpression unquote() {
        return Expressions.stringTemplate("json_unquote({0})", this);
    }

    /**
     * Validates that this value is valid JSON.
     *
     * <p>
     * SQL: {@code JSON_VALID(val)}
     * </p>
     *
     * @return boolean expression
     */
    @Override
    public SimpleExpression<Boolean> jsonValid() {
        return Expressions.booleanTemplate("json_valid({0})", this);
    }

    /**
     * Returns the type of this JSON value.
     *
     * <p>
     * SQL: {@code JSON_TYPE(json_val)}
     * </p>
     * <p>
     * Possible return values: NULL, INTEGER, DOUBLE, STRING, BOOLEAN, ARRAY, OBJECT
     * </p>
     *
     * @return string expression with the JSON type
     */
    @Override
    public StringExpression jsonType() {
        return Expressions.stringTemplate("json_type({0})", this);
    }

    /**
     * Searches for a value within a JSON document.
     *
     * <p>
     * SQL: {@code JSON_SEARCH(json_doc, one_or_all, search_str)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document to search
     * @param oneOrAll
     *            "one" to return first match, "all" to return all matches
     * @param searchString
     *            the string to search for (supports % and _ wildcards)
     * @return JsonValueExpression with the path(s) where value was found
     */
    public static JsonValueExpression search(Expression<?> jsonDoc, String oneOrAll, String searchString) {
        return new JsonValueExpression(Expressions.stringTemplate("json_search({0}, {1}, {2})",
                jsonDoc,
                Expressions.constant(oneOrAll),
                Expressions.constant(searchString)));
    }

    /**
     * Searches for a value within a JSON document with custom escape character.
     *
     * <p>
     * SQL:
     * {@code JSON_SEARCH(json_doc, one_or_all, search_str, escape_char, path...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document to search
     * @param oneOrAll
     *            "one" to return first match, "all" to return all matches
     * @param searchString
     *            the string to search for
     * @param escapeChar
     *            the escape character for wildcards
     * @param paths
     *            optional paths to search within
     * @return JsonValueExpression with the path(s) where value was found
     */
    public static JsonValueExpression search(Expression<?> jsonDoc,
            String oneOrAll,
            String searchString,
            String escapeChar,
            String... paths) {
        Expression<?>[] args = new Expression<?>[4 + paths.length];
        args[0] = jsonDoc;
        args[1] = Expressions.constant(oneOrAll);
        args[2] = Expressions.constant(searchString);
        args[3] = Expressions.constant(escapeChar);

        for (int i = 0; i < paths.length; i++) {
            args[4 + i] = Expressions.constant(paths[i]);
        }

        return new JsonValueExpression(Expressions.stringTemplate("json_search({0})", (Object[]) args));
    }

    /**
     * Sets or updates a value in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_SET(json_doc, path, val)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document
     * @param path
     *            the path where to set the value
     * @param value
     *            the value to set
     * @return JsonValueExpression with updated document
     */
    public static JsonValueExpression set(Expression<?> jsonDoc, String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return new JsonValueExpression(
                Expressions.stringTemplate("json_set({0}, {1}, {2})", jsonDoc, Expressions.constant(path), valueExpr));
    }

    /**
     * Inserts a value into a JSON document.
     *
     * <p>
     * SQL: {@code JSON_INSERT(json_doc, path, val)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document
     * @param path
     *            the path where to insert
     * @param value
     *            the value to insert
     * @return JsonValueExpression with updated document
     */
    public static JsonValueExpression insert(Expression<?> jsonDoc, String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return new JsonValueExpression(Expressions
                .stringTemplate("json_insert({0}, {1}, {2})", jsonDoc, Expressions.constant(path), valueExpr));
    }

    /**
     * Replaces a value in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_REPLACE(json_doc, path, val)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document
     * @param path
     *            the path where to replace
     * @param value
     *            the new value
     * @return JsonValueExpression with updated document
     */
    public static JsonValueExpression replace(Expression<?> jsonDoc, String path, Object value) {
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return new JsonValueExpression(Expressions
                .stringTemplate("json_replace({0}, {1}, {2})", jsonDoc, Expressions.constant(path), valueExpr));
    }

    /**
     * Removes data from a JSON document.
     *
     * <p>
     * SQL: {@code JSON_REMOVE(json_doc, path)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document
     * @param paths
     *            the paths to remove
     * @return JsonValueExpression with updated document
     */
    public static JsonValueExpression remove(Expression<?> jsonDoc, String... paths) {
        Expression<?>[] args = new Expression<?>[paths.length + 1];
        args[0] = jsonDoc;

        for (int i = 0; i < paths.length; i++) {
            args[i + 1] = Expressions.constant(paths[i]);
        }

        // Build template with proper number of placeholders
        StringBuilder template = new StringBuilder("json_remove(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0)
                template.append(", ");
            template.append("{").append(i).append("}");
        }
        template.append(")");

        return new JsonValueExpression(Expressions.stringTemplate(template.toString(), (Object[]) args));
    }

    /**
     * Wraps an existing expression as a JsonValueExpression.
     *
     * @param expression
     *            the expression to wrap
     * @return JsonValueExpression
     */
    public static JsonValueExpression wrap(Expression<String> expression) {
        return new JsonValueExpression(expression);
    }

    @Override
    @Nullable
    public <R, C> R accept(Visitor<R, C> v, @Nullable C context) {
        return mixin.accept(v, context);
    }
}
