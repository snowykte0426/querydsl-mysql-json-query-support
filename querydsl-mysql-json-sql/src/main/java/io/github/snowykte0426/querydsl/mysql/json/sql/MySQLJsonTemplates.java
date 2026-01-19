package io.github.snowykte0426.querydsl.mysql.json.sql;

import io.github.snowykte0426.querydsl.mysql.json.core.operators.JsonOperatorTemplates;
import com.querydsl.core.types.Operator;
import com.querydsl.sql.MySQLTemplates;

import java.util.Map;

/**
 * MySQL SQL templates with JSON function support.
 *
 * <p>
 * This class extends {@link MySQLTemplates} to register all 35 MySQL JSON
 * operators from the core module, enabling type-safe JSON function usage in
 * QueryDSL SQL queries.
 *
 * <h2>Usage Example</h2>
 *
 * <pre>{@code
 * // Create configuration with JSON support
 * Configuration configuration = new Configuration(MySQLJsonTemplates.DEFAULT);
 *
 * // Create SQLQueryFactory
 * SQLQueryFactory queryFactory = new SQLQueryFactory(configuration, dataSource);
 *
 * // Use JSON functions in queries
 * QUser user = QUser.user;
 * List<Tuple> results = queryFactory.select(user.name, user.email).from(user)
 *         .where(SqlJsonFunctions.jsonExtract(user.metadata, "$.role").eq("\"admin\"")).fetch();
 * }</pre>
 *
 * <h2>Registered Operators</h2>
 * <p>
 * This template registers the following MySQL JSON function categories:
 * <ul>
 * <li><b>Creation Functions (3)</b>: JSON_ARRAY, JSON_OBJECT, JSON_QUOTE</li>
 * <li><b>Search Functions (10)</b>: JSON_EXTRACT, JSON_CONTAINS, JSON_SEARCH,
 * etc.</li>
 * <li><b>Modification Functions (10)</b>: JSON_SET, JSON_INSERT, JSON_REMOVE,
 * etc.</li>
 * <li><b>Attribute Functions (4)</b>: JSON_DEPTH, JSON_LENGTH, JSON_TYPE,
 * JSON_VALID</li>
 * <li><b>Utility Functions (3)</b>: JSON_PRETTY, JSON_STORAGE_SIZE,
 * JSON_STORAGE_FREE</li>
 * <li><b>Schema Functions (2)</b>: JSON_SCHEMA_VALID,
 * JSON_SCHEMA_VALIDATION_REPORT</li>
 * <li><b>Aggregate Functions (2)</b>: JSON_ARRAYAGG, JSON_OBJECTAGG</li>
 * <li><b>Table Functions (1)</b>: JSON_TABLE</li>
 * </ul>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.2
 * @see MySQLTemplates
 * @see JsonOperatorTemplates
 * @see SqlJsonFunctions
 */
public class MySQLJsonTemplates extends MySQLTemplates {

    /**
     * Default singleton instance with standard MySQL configuration and JSON
     * support.
     */
    public static final MySQLJsonTemplates DEFAULT = new MySQLJsonTemplates();

    /**
     * Creates a new MySQLJsonTemplates instance with default configuration.
     */
    protected MySQLJsonTemplates() {
        super();
        registerJsonOperators();
    }

    /**
     * Creates a new MySQLJsonTemplates instance with quoted identifiers.
     *
     * @param quote
     *            whether to quote identifiers
     */
    protected MySQLJsonTemplates(boolean quote) {
        super(quote);
        registerJsonOperators();
    }

    /**
     * Registers all MySQL JSON operators with their SQL templates.
     *
     * <p>
     * This method retrieves operator templates from {@link JsonOperatorTemplates}
     * and registers them with QueryDSL's template system. Each operator is
     * registered with precedence level -1 (no precedence), as most JSON functions
     * don't require special precedence handling.
     */
    private void registerJsonOperators() {
        // Get all JSON operator templates from core module
        Map<Operator, String> jsonTemplates = JsonOperatorTemplates.getTemplates();

        // Register each operator with QueryDSL SQL template system
        // Precedence -1 means no special precedence handling (most JSON functions)
        jsonTemplates.forEach((operator, template) -> add(operator, template, -1));
    }

    /**
     * Creates a MySQLJsonTemplates instance with quoted identifiers.
     *
     * <p>
     * Example usage:
     *
     * <pre>{@code
     * Configuration config = new Configuration(MySQLJsonTemplates.quoted());
     * }</pre>
     *
     * @return new MySQLJsonTemplates with quoted identifiers
     */
    public static MySQLJsonTemplates quoted() {
        return new MySQLJsonTemplates(true);
    }
}
