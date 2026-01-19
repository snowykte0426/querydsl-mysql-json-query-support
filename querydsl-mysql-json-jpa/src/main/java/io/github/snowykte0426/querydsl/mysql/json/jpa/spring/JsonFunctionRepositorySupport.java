package io.github.snowykte0426.querydsl.mysql.json.jpa.spring;

import io.github.snowykte0426.querydsl.mysql.json.jpa.JPAJsonFunctions;
import io.github.snowykte0426.querydsl.mysql.json.jpa.expressions.JPAJsonExpression;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonArrayExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonObjectExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonExpression;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

/**
 * Spring Data JPA repository support class with enhanced JSON function capabilities.
 *
 * <p>This class extends {@link QuerydslRepositorySupport} to provide convenient
 * access to MySQL JSON functions within Spring Data JPA repositories.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Repository
 * public class UserRepositoryImpl extends JsonFunctionRepositorySupport implements UserRepositoryCustom {
 *
 *     public UserRepositoryImpl() {
 *         super(User.class);
 *     }
 *
 *     @Override
 *     public List<User> findByRole(String role) {
 *         QUser user = QUser.user;
 *         JPAJsonExpression metadata = jsonExpression(user.metadata);
 *
 *         return from(user)
 *             .where(metadata.extract("$.role").eq("\"" + role + "\""))
 *             .fetch();
 *     }
 *
 *     @Override
 *     public List<User> findUsersWithPermission(String permission) {
 *         QUser user = QUser.user;
 *
 *         return from(user)
 *             .where(JPAJsonFunctions.memberOf(permission, user.permissions))
 *             .fetch();
 *     }
 * }
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Direct access to all 35 MySQL JSON functions via {@link JPAJsonFunctions}</li>
 *   <li>Fluent API through {@link JPAJsonExpression} wrappers</li>
 *   <li>Integration with Spring Data JPA repositories</li>
 *   <li>Support for custom query implementations</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.2
 * @see QuerydslRepositorySupport
 * @see JPAJsonFunctions
 * @see JPAJsonExpression
 */
public abstract class JsonFunctionRepositorySupport extends QuerydslRepositorySupport {

    private JPAQueryFactory queryFactory;

    /**
     * Creates a new {@link JsonFunctionRepositorySupport} for the given domain type.
     *
     * @param domainClass the domain class (entity type)
     */
    public JsonFunctionRepositorySupport(Class<?> domainClass) {
        super(domainClass);
    }

    @Override
    public void setEntityManager(EntityManager entityManager) {
        super.setEntityManager(entityManager);
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    /**
     * Gets the JPAQueryFactory for building queries.
     *
     * @return the query factory
     */
    protected JPAQueryFactory getQueryFactory() {
        return queryFactory;
    }

    /**
     * Creates a JPAJsonExpression wrapper for the given expression.
     *
     * <p>This is a convenience method for creating fluent JSON expressions.
     *
     * <p>Example:
     * <pre>{@code
     * QUser user = QUser.user;
     * JPAJsonExpression metadata = jsonExpression(user.metadata);
     *
     * return from(user)
     *     .where(metadata.contains("\"admin\"", "$.roles"))
     *     .fetch();
     * }</pre>
     *
     * @param expression the JSON column expression
     * @return JPAJsonExpression wrapper for fluent API
     */
    protected JPAJsonExpression jsonExpression(Expression<?> expression) {
        return JPAJsonExpression.of(expression);
    }

    /**
     * Creates a JPAJsonExpression wrapper for a StringPath.
     *
     * @param path the string path (Q-class property)
     * @return JPAJsonExpression wrapper
     */
    protected JPAJsonExpression jsonExpression(StringPath path) {
        return JPAJsonExpression.of(path);
    }

    /**
     * Creates a path builder for JSON operations on an entity.
     *
     * <p>Example:
     * <pre>{@code
     * PathBuilder<User> userPath = jsonPath(User.class, "user");
     * StringPath metadata = userPath.getString("metadata");
     * }</pre>
     *
     * @param <T> the entity type
     * @param entityClass the entity class
     * @param variable the variable name
     * @return PathBuilder for the entity
     */
    protected <T> PathBuilder<T> jsonPath(Class<T> entityClass, String variable) {
        return new PathBuilder<>(entityClass, variable);
    }

    /**
     * Provides direct access to JPAJsonFunctions for static method usage.
     *
     * <p>This is useful when you need to use functions that don't fit
     * the fluent API pattern, such as aggregate functions.
     *
     * <p>Example:
     * <pre>{@code
     * // Using aggregate function
     * return from(user)
     *     .select(user.department, JPAJsonFunctions.jsonArrayAgg(user.name))
     *     .groupBy(user.department)
     *     .fetch();
     * }</pre>
     *
     * @return JPAJsonFunctions class for static access
     */
    protected Class<JPAJsonFunctions> jsonFunctions() {
        return JPAJsonFunctions.class;
    }

    /**
     * Creates a JSON array containing the specified values.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonArray(Object...)}.
     *
     * @param values the array values
     * @return JSON array expression
     */
    protected JsonArrayExpression jsonArray(Object... values) {
        return JPAJsonFunctions.jsonArray(values);
    }

    /**
     * Creates a JSON object from key-value pairs.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonObject(Object...)}.
     *
     * @param keyValuePairs alternating keys and values
     * @return JSON object expression
     */
    protected JsonObjectExpression jsonObject(Object... keyValuePairs) {
        return JPAJsonFunctions.jsonObject(keyValuePairs);
    }

    /**
     * Extracts data from a JSON document.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonExtract(Expression, String)}.
     *
     * @param jsonDoc the JSON document expression
     * @param path the JSON path
     * @return extracted JSON expression
     */
    protected JsonExpression<String> jsonExtract(
            Expression<?> jsonDoc, String path) {
        return JPAJsonFunctions.jsonExtract(jsonDoc, path);
    }

    /**
     * Tests whether a JSON document contains a value.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonContains(Expression, String)}.
     *
     * @param jsonDoc the JSON document expression
     * @param value the value to search for
     * @return boolean expression
     */
    protected com.querydsl.core.types.dsl.BooleanExpression jsonContains(Expression<?> jsonDoc, String value) {
        return JPAJsonFunctions.jsonContains(jsonDoc, value);
    }

    /**
     * Tests whether a JSON document contains a value at a path.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonContains(Expression, String, String)}.
     *
     * @param jsonDoc the JSON document expression
     * @param value the value to search for
     * @param path the JSON path
     * @return boolean expression
     */
    protected com.querydsl.core.types.dsl.BooleanExpression jsonContains(
            Expression<?> jsonDoc, String value, String path) {
        return JPAJsonFunctions.jsonContains(jsonDoc, value, path);
    }

    /**
     * Tests whether a value is a member of a JSON array.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#memberOf(Object, Expression)}.
     *
     * @param value the value to test
     * @param jsonArray the JSON array
     * @return boolean expression
     */
    protected com.querydsl.core.types.dsl.BooleanExpression memberOf(Object value, Expression<?> jsonArray) {
        return JPAJsonFunctions.memberOf(value, jsonArray);
    }

    /**
     * Aggregates values into a JSON array.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonArrayAgg(Expression)}.
     *
     * @param value the expression to aggregate
     * @return JSON array expression
     */
    protected JsonArrayExpression jsonArrayAgg(
            Expression<?> value) {
        return JPAJsonFunctions.jsonArrayAgg(value);
    }

    /**
     * Aggregates key-value pairs into a JSON object.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonObjectAgg(Expression, Expression)}.
     *
     * @param key the key expression
     * @param value the value expression
     * @return JSON object expression
     */
    protected JsonObjectExpression jsonObjectAgg(
            Expression<?> key, Expression<?> value) {
        return JPAJsonFunctions.jsonObjectAgg(key, value);
    }

    /**
     * Returns the type of a JSON value.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonType(Expression)}.
     *
     * @param jsonValue the JSON value
     * @return type as string expression
     */
    protected com.querydsl.core.types.dsl.StringExpression jsonType(Expression<?> jsonValue) {
        return JPAJsonFunctions.jsonType(jsonValue);
    }

    /**
     * Tests whether a value is valid JSON.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonValid(Expression)}.
     *
     * @param value the value to validate
     * @return boolean expression
     */
    protected com.querydsl.core.types.dsl.BooleanExpression jsonValid(Expression<?> value) {
        return JPAJsonFunctions.jsonValid(value);
    }

    /**
     * Returns the length of a JSON document.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonLength(Expression)}.
     *
     * @param jsonDoc the JSON document
     * @return length as number expression
     */
    protected com.querydsl.core.types.dsl.NumberExpression<Integer> jsonLength(Expression<?> jsonDoc) {
        return JPAJsonFunctions.jsonLength(jsonDoc);
    }

    /**
     * Returns the depth of a JSON document.
     *
     * <p>Convenience method delegating to {@link JPAJsonFunctions#jsonDepth(Expression)}.
     *
     * @param jsonDoc the JSON document
     * @return depth as number expression
     */
    protected com.querydsl.core.types.dsl.NumberExpression<Integer> jsonDepth(Expression<?> jsonDoc) {
        return JPAJsonFunctions.jsonDepth(jsonDoc);
    }
}
