package io.github.snowykte0426.querydsl.mysql.json.core.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import org.jetbrains.annotations.NotNull;

/**
 * Factory class for MySQL JSON aggregate functions.
 *
 * <p>
 * This class provides static factory methods for JSON aggregation operations:
 * <ul>
 * <li>{@link #jsonArrayAgg} - Aggregates values into a JSON array</li>
 * <li>{@link #jsonObjectAgg} - Aggregates key-value pairs into a JSON
 * object</li>
 * </ul>
 *
 * <p>
 * These functions are used in GROUP BY queries to aggregate multiple rows into
 * a single JSON value.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public final class JsonAggregateFunctions {

    private JsonAggregateFunctions() {
        // Utility class - prevent instantiation
    }

    // ========================================
    // JSON_ARRAYAGG - Aggregate into array
    // ========================================

    /**
     * Aggregates values from multiple rows into a JSON array.
     *
     * <p>
     * SQL: {@code JSON_ARRAYAGG(value)}
     * </p>
     *
     * <p>
     * This is an aggregate function that returns a JSON array containing all
     * non-NULL values from the specified column in the group. NULL values are
     * ignored.
     *
     * <p>
     * Example:
     *
     * <pre>
     * -- Table: products
     * -- +----+-------+-------+
     * -- | id | name  | price |
     * -- +----+-------+-------+
     * -- |  1 | Item1 |  10.0 |
     * -- |  2 | Item2 |  20.0 |
     * -- |  3 | Item3 |  30.0 |
     * -- +----+-------+-------+
     *
     * SELECT JSON_ARRAYAGG(name) FROM products;
     * -&gt; ["Item1", "Item2", "Item3"]
     *
     * SELECT JSON_ARRAYAGG(price) FROM products;
     * -&gt; [10.0, 20.0, 30.0]
     * </pre>
     *
     * <p>
     * Usage in QueryDSL:
     *
     * <pre>
     * // SELECT category, JSON_ARRAYAGG(name) FROM products GROUP BY category
     * queryFactory.select(product.category, jsonArrayAgg(product.name)).from(product).groupBy(product.category)
     *         .fetch();
     * </pre>
     *
     * @param value
     *            the expression to aggregate
     * @return JSON array expression containing aggregated values
     */
    public static @NotNull JsonArrayExpression jsonArrayAgg(Expression<?> value) {
        return JsonArrayExpression.wrap(Expressions.stringTemplate("json_arrayagg({0})", value));
    }

    /**
     * Placeholder for DISTINCT aggregation.
     *
     * <p>
     * <strong>Note:</strong> MySQL doesn't support DISTINCT in JSON_ARRAYAGG
     * directly. To achieve DISTINCT behavior, use a subquery with DISTINCT:
     *
     * <pre>{@code
     * // MySQL query pattern:
     * SELECT JSON_ARRAYAGG(DISTINCT_VALUE)
     * FROM (
     *   SELECT DISTINCT value AS DISTINCT_VALUE FROM table
     * ) subquery
     *
     * // QueryDSL equivalent:
     * QTable table = QTable.table;
     * JPAQuery<String> distinctSubquery = queryFactory
     *     .selectDistinct(table.value)
     *     .from(table);
     *
     * JsonArrayExpression result = jsonArrayAgg(distinctSubquery);
     * }</pre>
     *
     * <p>
     * This method currently behaves identically to
     * {@link #jsonArrayAgg(Expression)} and is provided for API completeness. Users
     * must implement DISTINCT logic using subqueries as shown above.
     *
     * @param value
     *            the expression to aggregate
     * @return JSON array expression (behaves identically to jsonArrayAgg)
     */
    public static @NotNull JsonArrayExpression jsonArrayAggDistinct(Expression<?> value) {
        // MySQL doesn't support DISTINCT in JSON_ARRAYAGG directly
        // This is a placeholder - users should use DISTINCT in the subquery
        return jsonArrayAgg(value);
    }

    // ========================================
    // JSON_OBJECTAGG - Aggregate into object
    // ========================================

    /**
     * Aggregates key-value pairs from multiple rows into a JSON object.
     *
     * <p>
     * SQL: {@code JSON_OBJECTAGG(key, value)}
     * </p>
     *
     * <p>
     * This is an aggregate function that returns a JSON object containing key-value
     * pairs from the specified columns in the group. Both key and value must be
     * non-NULL; rows with NULL keys or values are ignored.
     *
     * <p>
     * Example:
     *
     * <pre>
     * -- Table: settings
     * -- +----+----------+-------+
     * -- | id | key      | value |
     * -- +----+----------+-------+
     * -- |  1 | theme    | dark  |
     * -- |  2 | language | en    |
     * -- |  3 | timezone | UTC   |
     * -- +----+----------+-------+
     *
     * SELECT JSON_OBJECTAGG(key, value) FROM settings;
     * -&gt; {"theme": "dark", "language": "en", "timezone": "UTC"}
     * </pre>
     *
     * <p>
     * Usage in QueryDSL:
     *
     * <pre>
     * // SELECT user_id, JSON_OBJECTAGG(setting_key, setting_value)
     * // FROM user_settings GROUP BY user_id
     * queryFactory.select(userSetting.userId, jsonObjectAgg(userSetting.key, userSetting.value)).from(userSetting)
     *         .groupBy(userSetting.userId).fetch();
     * </pre>
     *
     * @param key
     *            the expression for object keys
     * @param value
     *            the expression for object values
     * @return JSON object expression containing aggregated key-value pairs
     */
    public static @NotNull JsonObjectExpression jsonObjectAgg(Expression<?> key, Expression<?> value) {
        return JsonObjectExpression.wrap(Expressions.stringTemplate("json_objectagg({0}, {1})", key, value));
    }

    /**
     * Aggregates key-value pairs with string literal key.
     *
     * @param key
     *            the key as string literal
     * @param value
     *            the value expression
     * @return JSON object expression
     */
    public static @NotNull JsonObjectExpression jsonObjectAgg(@NotNull String key, Expression<?> value) {
        return JsonObjectExpression
                .wrap(Expressions.stringTemplate("json_objectagg({0}, {1})", Expressions.constant(key), value));
    }

    // ========================================
    // Convenience methods
    // ========================================

    /**
     * Aggregates values into a JSON array. Alias for
     * {@link #jsonArrayAgg(Expression)}.
     *
     * @param value
     *            the expression to aggregate
     * @return JSON array expression
     */
    public static @NotNull JsonArrayExpression arrayAgg(Expression<?> value) {
        return jsonArrayAgg(value);
    }

    /**
     * Aggregates key-value pairs into a JSON object. Alias for
     * {@link #jsonObjectAgg(Expression, Expression)}.
     *
     * @param key
     *            the key expression
     * @param value
     *            the value expression
     * @return JSON object expression
     */
    public static @NotNull JsonObjectExpression objectAgg(Expression<?> key, Expression<?> value) {
        return jsonObjectAgg(key, value);
    }

    /**
     * Creates a nested JSON structure by aggregating objects into an array.
     *
     * <p>
     * This is useful for creating nested JSON structures like:
     *
     * <pre>
     * [
     *   {"id": 1, "name": "Item1"},
     *   {"id": 2, "name": "Item2"}
     * ]
     * </pre>
     *
     * <p>
     * Usage:
     *
     * <pre>
     * // SELECT JSON_ARRAYAGG(JSON_OBJECT('id', id, 'name', name))
     * jsonArrayAgg(JsonCreationFunctions.jsonObject().add("id", product.id).add("name", product.name).build())
     * </pre>
     *
     * @param objectExpression
     *            the JSON object expression to aggregate
     * @return JSON array of objects
     */
    public static @NotNull JsonArrayExpression aggregateObjects(Expression<?> objectExpression) {
        return jsonArrayAgg(objectExpression);
    }

    /**
     * Groups values by a key into a JSON object containing arrays.
     *
     * <p>
     * Example output:
     *
     * <pre>
     * {
     *   "category1": ["item1", "item2"],
     *   "category2": ["item3", "item4"]
     * }
     * </pre>
     *
     * <p>
     * Note: This requires a subquery to first aggregate arrays by key, then
     * aggregate those into an object. This method provides the inner aggregation.
     *
     * @param value
     *            the value to aggregate into array
     * @return JSON array expression for use in outer JSON_OBJECTAGG
     */
    public static @NotNull JsonArrayExpression groupIntoArray(Expression<?> value) {
        return jsonArrayAgg(value);
    }
}
