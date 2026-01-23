package io.github.snowykte0426.querydsl.mysql.json.core.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonValueExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for MySQL JSON modification functions.
 *
 * <p>
 * This class provides static factory methods for modifying JSON documents:
 * <ul>
 * <li>{@link #jsonSet} - Inserts or updates values</li>
 * <li>{@link #jsonInsert} - Inserts values without replacing</li>
 * <li>{@link #jsonReplace} - Replaces existing values</li>
 * <li>{@link #jsonRemove} - Removes values at paths</li>
 * <li>{@link #jsonArrayAppend} - Appends to arrays</li>
 * <li>{@link #jsonArrayInsert} - Inserts into arrays</li>
 * <li>{@link #jsonMergePatch} - Merges documents (RFC 7386)</li>
 * <li>{@link #jsonMergePreserve} - Merges preserving duplicates</li>
 * <li>{@link #jsonUnquote} - Unquotes JSON strings</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonModifyFunctions {

    private JsonModifyFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_SET - Insert or update values
    // ========================================

    /**
     * Inserts or updates data in a JSON document at the specified path.
     *
     * <p>
     * SQL: {@code JSON_SET(json_doc, path, val)}
     * </p>
     *
     * <p>
     * If the path exists, the value is replaced. If not, it's inserted.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @param value
     *            the value to set
     * @return modified JSON expression
     */
    public static @NotNull JsonValueExpression jsonSet(Expression<?> jsonDoc, String path, Object value) {
        return JsonValueExpression.set(jsonDoc, path, value);
    }

    /**
     * Inserts or updates multiple values in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_SET(json_doc, path1, val1, path2, val2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param pathsAndValues
     *            alternating paths and values
     * @return modified JSON expression
     * @throws IllegalArgumentException
     *             if odd number of path-value pairs
     */
    public static @NotNull StringExpression jsonSet(Expression<?> jsonDoc, Object @NotNull... pathsAndValues) {
        if (pathsAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide path-value pairs (even number of arguments)");
        }

        Object @NotNull [] args = new Object[pathsAndValues.length + 1];
        args[0] = jsonDoc;

        for (int i = 0; i < pathsAndValues.length; i += 2) {
            args[i + 1] = Expressions.constant(pathsAndValues[i]);
            args[i + 2] = pathsAndValues[i + 1] instanceof Expression
                    ? pathsAndValues[i + 1]
                    : Expressions.constant(pathsAndValues[i + 1]);
        }

        @NotNull
        StringBuilder template = new StringBuilder("json_set({0}");
        for (int i = 0; i < pathsAndValues.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");

        return Expressions.stringTemplate(template.toString(), args);
    }

    // ========================================
    // JSON_INSERT - Insert without replacing
    // ========================================

    /**
     * Inserts data into a JSON document without replacing existing values.
     *
     * <p>
     * SQL: {@code JSON_INSERT(json_doc, path, val)}
     * </p>
     *
     * <p>
     * Only inserts if the path doesn't exist.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @param value
     *            the value to insert
     * @return modified JSON expression
     */
    public static @NotNull JsonValueExpression jsonInsert(Expression<?> jsonDoc, String path, Object value) {
        return JsonValueExpression.insert(jsonDoc, path, value);
    }

    /**
     * Inserts multiple values into a JSON document.
     *
     * <p>
     * SQL: {@code JSON_INSERT(json_doc, path1, val1, path2, val2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param pathsAndValues
     *            alternating paths and values
     * @return modified JSON expression
     */
    public static @NotNull StringExpression jsonInsert(Expression<?> jsonDoc, Object @NotNull... pathsAndValues) {
        if (pathsAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide path-value pairs (even number of arguments)");
        }

        Object @NotNull [] args = new Object[pathsAndValues.length + 1];
        args[0] = jsonDoc;

        for (int i = 0; i < pathsAndValues.length; i += 2) {
            args[i + 1] = Expressions.constant(pathsAndValues[i]);
            args[i + 2] = pathsAndValues[i + 1] instanceof Expression
                    ? pathsAndValues[i + 1]
                    : Expressions.constant(pathsAndValues[i + 1]);
        }

        @NotNull
        StringBuilder template = new StringBuilder("json_insert({0}");
        for (int i = 0; i < pathsAndValues.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");

        return Expressions.stringTemplate(template.toString(), args);
    }

    // ========================================
    // JSON_REPLACE - Replace existing values
    // ========================================

    /**
     * Replaces existing values in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_REPLACE(json_doc, path, val)}
     * </p>
     *
     * <p>
     * Only replaces if the path exists.
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path
     * @param value
     *            the new value
     * @return modified JSON expression
     */
    public static @NotNull JsonValueExpression jsonReplace(Expression<?> jsonDoc, String path, Object value) {
        return JsonValueExpression.replace(jsonDoc, path, value);
    }

    /**
     * Replaces multiple values in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_REPLACE(json_doc, path1, val1, path2, val2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param pathsAndValues
     *            alternating paths and values
     * @return modified JSON expression
     */
    public static @NotNull StringExpression jsonReplace(Expression<?> jsonDoc, Object @NotNull... pathsAndValues) {
        if (pathsAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide path-value pairs (even number of arguments)");
        }

        Object @NotNull [] args = new Object[pathsAndValues.length + 1];
        args[0] = jsonDoc;

        for (int i = 0; i < pathsAndValues.length; i += 2) {
            args[i + 1] = Expressions.constant(pathsAndValues[i]);
            args[i + 2] = pathsAndValues[i + 1] instanceof Expression
                    ? pathsAndValues[i + 1]
                    : Expressions.constant(pathsAndValues[i + 1]);
        }

        @NotNull
        StringBuilder template = new StringBuilder("json_replace({0}");
        for (int i = 0; i < pathsAndValues.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");

        return Expressions.stringTemplate(template.toString(), args);
    }

    // ========================================
    // JSON_REMOVE - Remove data from JSON
    // ========================================

    /**
     * Removes data from a JSON document at the specified path.
     *
     * <p>
     * SQL: {@code JSON_REMOVE(json_doc, path)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the JSON path to remove
     * @return modified JSON expression
     */
    public static @NotNull StringExpression jsonRemove(Expression<?> jsonDoc, @NotNull String path) {
        return Expressions.stringTemplate("json_remove({0}, {1})", jsonDoc, Expressions.constant(path));
    }

    /**
     * Removes data from multiple paths in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_REMOVE(json_doc, path1, path2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param paths
     *            the paths to remove
     * @return modified JSON expression
     */
    public static @NotNull JsonValueExpression jsonRemove(Expression<?> jsonDoc, String... paths) {
        return JsonValueExpression.remove(jsonDoc, paths);
    }

    // ========================================
    // JSON_ARRAY_APPEND - Append to arrays
    // ========================================

    /**
     * Appends values to the end of arrays in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_ARRAY_APPEND(json_doc, path, val)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the path to the array
     * @param value
     *            the value to append
     * @return modified JSON expression
     */
    public static @NotNull JsonArrayExpression jsonArrayAppend(Expression<?> jsonDoc,
            @NotNull String path,
            Object value) {
        @NotNull
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return JsonArrayExpression.wrap(Expressions
                .stringTemplate("json_array_append({0}, {1}, {2})", jsonDoc, Expressions.constant(path), valueExpr));
    }

    /**
     * Appends multiple values to arrays in a JSON document.
     *
     * <p>
     * SQL: {@code JSON_ARRAY_APPEND(json_doc, path1, val1, path2, val2, ...)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param pathsAndValues
     *            alternating paths and values
     * @return modified JSON expression
     */
    public static @NotNull JsonArrayExpression jsonArrayAppend(Expression<?> jsonDoc,
            Object @NotNull... pathsAndValues) {
        if (pathsAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide path-value pairs (even number of arguments)");
        }

        Object @NotNull [] args = new Object[pathsAndValues.length + 1];
        args[0] = jsonDoc;

        for (int i = 0; i < pathsAndValues.length; i += 2) {
            args[i + 1] = Expressions.constant(pathsAndValues[i]);
            args[i + 2] = pathsAndValues[i + 1] instanceof Expression
                    ? pathsAndValues[i + 1]
                    : Expressions.constant(pathsAndValues[i + 1]);
        }

        @NotNull
        StringBuilder template = new StringBuilder("json_array_append({0}");
        for (int i = 0; i < pathsAndValues.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");

        return JsonArrayExpression.wrap(Expressions.stringTemplate(template.toString(), args));
    }

    // ========================================
    // JSON_ARRAY_INSERT - Insert into arrays
    // ========================================

    /**
     * Inserts a value into a JSON array at a specific position.
     *
     * <p>
     * SQL: {@code JSON_ARRAY_INSERT(json_doc, path, val)}
     * </p>
     *
     * @param jsonDoc
     *            the JSON document expression
     * @param path
     *            the path with array index (e.g., "$[0]")
     * @param value
     *            the value to insert
     * @return modified JSON expression
     */
    public static @NotNull JsonArrayExpression jsonArrayInsert(Expression<?> jsonDoc,
            @NotNull String path,
            Object value) {
        @NotNull
        Expression<?> valueExpr = value instanceof Expression ? (Expression<?>) value : Expressions.constant(value);

        return JsonArrayExpression.wrap(Expressions
                .stringTemplate("json_array_insert({0}, {1}, {2})", jsonDoc, Expressions.constant(path), valueExpr));
    }

    // ========================================
    // JSON_MERGE_PATCH - Merge with RFC 7386
    // ========================================

    /**
     * Merges two or more JSON documents using RFC 7386 merge patch semantics.
     *
     * <p>
     * SQL: {@code JSON_MERGE_PATCH(json_doc1, json_doc2, ...)}
     * </p>
     *
     * <p>
     * Duplicate keys: later values replace earlier ones.
     * </p>
     *
     * @param jsonDocs
     *            the JSON documents to merge
     * @return merged JSON expression
     */
    public static @NotNull JsonObjectExpression jsonMergePatch(Expression<?>... jsonDocs) {
        return JsonObjectExpression.wrap(Expressions.stringTemplate("json_merge_patch({0})", (Object[]) jsonDocs));
    }

    /**
     * Merges JSON document expressions and string literals.
     *
     * @param first
     *            the first JSON document
     * @param others
     *            additional documents (expressions or JSON strings)
     * @return merged JSON expression
     */
    public static @NotNull StringExpression jsonMergePatch(Expression<?> first, Object @NotNull... others) {
        Object @NotNull [] args = new Object[others.length + 1];
        args[0] = first;

        for (int i = 0; i < others.length; i++) {
            args[i + 1] = others[i] instanceof Expression ? others[i] : Expressions.constant(others[i]);
        }

        @NotNull
        StringBuilder template = new StringBuilder("json_merge_patch({0}");
        for (int i = 0; i < others.length; i++) {
            template.append(", {").append(i + 1).append("}");
        }
        template.append(")");

        return Expressions.stringTemplate(template.toString(), args);
    }

    // ========================================
    // JSON_MERGE_PRESERVE - Merge preserving duplicates
    // ========================================

    /**
     * Merges two or more JSON documents, preserving duplicate keys as arrays.
     *
     * <p>
     * SQL: {@code JSON_MERGE_PRESERVE(json_doc1, json_doc2, ...)}
     * </p>
     *
     * @param jsonDocs
     *            the JSON documents to merge
     * @return merged JSON expression
     */
    public static @NotNull StringExpression jsonMergePreserve(Expression<?> @NotNull... jsonDocs) {
        if (jsonDocs.length == 0) {
            throw new IllegalArgumentException("json_merge_preserve requires at least one argument");
        }

        // Build template with proper number of placeholders
        @NotNull
        StringBuilder template = new StringBuilder("json_merge_preserve(");
        for (int i = 0; i < jsonDocs.length; i++) {
            if (i > 0)
                template.append(", ");
            template.append("{").append(i).append("}");
        }
        template.append(")");

        return Expressions.stringTemplate(template.toString(), (Object[]) jsonDocs);
    }

    // ========================================
    // JSON_UNQUOTE - Unquote JSON string
    // ========================================

    /**
     * Unquotes a JSON string value.
     *
     * <p>
     * SQL: {@code JSON_UNQUOTE(json_val)}
     * </p>
     *
     * <p>
     * Removes quotes and unescapes special characters.
     * </p>
     *
     * @param jsonValue
     *            the JSON value expression
     * @return unquoted string expression
     */
    public static @NotNull StringExpression jsonUnquote(Expression<?> jsonValue) {
        return Expressions.stringTemplate("json_unquote({0})", jsonValue);
    }

    /**
     * Unquotes a JSON string literal.
     *
     * @param jsonString
     *            the JSON string literal
     * @return unquoted string expression
     */
    public static @NotNull StringExpression jsonUnquote(@NotNull String jsonString) {
        return Expressions.stringTemplate("json_unquote({0})", Expressions.constant(jsonString));
    }
}
