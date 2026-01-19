package io.github.snowykte0426.querydsl.mysql.json.jpa.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.spi.TypeConfiguration;

import static org.hibernate.query.sqm.produce.function.FunctionParameterType.STRING;

/**
 * Hibernate {@link FunctionContributor} that registers MySQL JSON functions.
 * <p>
 * This class is automatically discovered by Hibernate 6.4+ through the
 * {@code META-INF/services/org.hibernate.boot.model.FunctionContributor} SPI
 * file. No manual configuration is required - Hibernate will automatically load
 * this contributor when it's on the classpath.
 * <p>
 * This contributor registers all MySQL JSON functions with their correct return
 * types, enabling them to be used in HQL/JPQL queries without "Non-boolean
 * expression used in predicate context" errors.
 * <p>
 * <strong>Functions Registered:</strong>
 * <ul>
 * <li>Boolean functions: json_contains, json_contains_path, json_overlaps,
 * json_valid, etc.</li>
 * <li>String functions: json_extract, json_unquote, json_type, json_search,
 * etc.</li>
 * <li>Numeric functions: json_length, json_depth, json_storage_size, etc.</li>
 * </ul>
 *
 * @since 0.1.0-Beta.1
 * @see FunctionContributor
 */
public class MySQLJsonFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        final TypeConfiguration typeConfiguration = functionContributions.getTypeConfiguration();

        registerBooleanFunctions(functionContributions, typeConfiguration);
        registerStringFunctions(functionContributions, typeConfiguration);
        registerNumericFunctions(functionContributions, typeConfiguration);
    }

    /**
     * Registers JSON functions that return BOOLEAN values.
     * <p>
     * These functions can be used directly in WHERE clauses without additional type
     * conversions.
     *
     * @param fc
     *            the function contributions registry
     * @param typeConfig
     *            the type configuration for resolving return types
     */
    private void registerBooleanFunctions(FunctionContributions fc, TypeConfiguration typeConfig) {
        // json_contains: 2-3 arguments
        // json_contains(target, candidate) - Returns 1 if candidate is contained in target
        // json_contains(target, candidate, path) - Returns 1 if candidate is contained in target at path
        fc.getFunctionRegistry().registerBinaryTernaryPattern(
                "json_contains",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN),
                "json_contains(?1, ?2)",
                "json_contains(?1, ?2, ?3)",
                STRING, STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(target, candidate[, path])");

        // json_contains_path: 3-4 arguments (most common usage)
        // json_contains_path(target, one_or_all, path...) - Returns 1 if paths exist in target
        // Note: Support for 5+ paths can be added via additional registerPattern() calls if needed
        fc.getFunctionRegistry().registerTernaryQuaternaryPattern(
                "json_contains_path",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN),
                "json_contains_path(?1, ?2, ?3)",
                "json_contains_path(?1, ?2, ?3, ?4)",
                STRING, STRING, STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(target, one_or_all, path[, path2])");

        // json_overlaps(json1, json2) - Returns 1 if json1 and json2 have any
        // overlapping elements
        fc.getFunctionRegistry().registerPattern("json_overlaps",
                "json_overlaps(?1, ?2)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN));

        // json_valid(value) - Returns 1 if value is valid JSON
        fc.getFunctionRegistry().registerPattern("json_valid",
                "json_valid(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN));

        // json_schema_valid(schema, document) - Returns 1 if document validates against
        // schema
        fc.getFunctionRegistry().registerPattern("json_schema_valid",
                "json_schema_valid(?1, ?2)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN));

        // Note: MEMBER OF has special syntax: value MEMBER OF(json_array)
        // It's registered as a pattern but may require special handling in QueryDSL
        fc.getFunctionRegistry().registerPattern("member_of",
                "?1 member of(?2)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN));
    }

    /**
     * Registers JSON functions that return STRING values.
     *
     * @param fc
     *            the function contributions registry
     * @param typeConfig
     *            the type configuration for resolving return types
     */
    private void registerStringFunctions(FunctionContributions fc, TypeConfiguration typeConfig) {
        // json_extract: 2-3 arguments (most common usage)
        // json_extract(json_doc, path...) - Extracts data from JSON document
        // Note: Support for 4+ paths can be added via additional registerPattern() calls if needed
        fc.getFunctionRegistry().registerBinaryTernaryPattern(
                "json_extract",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING),
                "json_extract(?1, ?2)",
                "json_extract(?1, ?2, ?3)",
                STRING, STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(json_doc, path[, path2])");

        // json_unquote(json_val) - Unquotes JSON value and returns result as string
        fc.getFunctionRegistry().registerPattern("json_unquote",
                "json_unquote(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING));

        // json_type(json_val) - Returns type of JSON value as string
        fc.getFunctionRegistry().registerPattern("json_type",
                "json_type(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING));

        // json_search: 3-4 arguments (most common usage)
        // json_search(json_doc, one_or_all, search_str[, escape]) - Searches for string in JSON
        // Note: Support for 5+ arguments can be added via additional registerPattern() calls if needed
        fc.getFunctionRegistry().registerTernaryQuaternaryPattern(
                "json_search",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING),
                "json_search(?1, ?2, ?3)",
                "json_search(?1, ?2, ?3, ?4)",
                STRING, STRING, STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(json_doc, one_or_all, search_str[, escape])");

        // json_keys: 1-2 arguments
        // json_keys(json_doc) - Returns keys from JSON object as JSON array
        // json_keys(json_doc, path) - Returns keys at path
        fc.getFunctionRegistry().registerUnaryBinaryPattern(
                "json_keys",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING),
                "json_keys(?1)",
                "json_keys(?1, ?2)",
                STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(json_doc[, path])");

        // json_quote(string) - Quotes a string as JSON value
        fc.getFunctionRegistry().registerPattern("json_quote",
                "json_quote(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING));

        // json_pretty(json_val) - Pretty-prints JSON value
        fc.getFunctionRegistry().registerPattern("json_pretty",
                "json_pretty(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING));

        // json_schema_validation_report(schema, document) - Returns validation report
        fc.getFunctionRegistry().registerPattern("json_schema_validation_report",
                "json_schema_validation_report(?1, ?2)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING));

        // json_value: 2-3 arguments
        // json_value(json_doc, path) - Extracts scalar value from JSON
        // json_value(json_doc, path, RETURNING type) - with type casting
        fc.getFunctionRegistry().registerBinaryTernaryPattern(
                "json_value",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING),
                "json_value(?1, ?2)",
                "json_value(?1, ?2 returning ?3)",
                STRING, STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(json_doc, path[, RETURNING type])");
    }

    /**
     * Registers JSON functions that return NUMERIC values.
     *
     * @param fc
     *            the function contributions registry
     * @param typeConfig
     *            the type configuration for resolving return types
     */
    private void registerNumericFunctions(FunctionContributions fc, TypeConfiguration typeConfig) {
        // json_length: 1-2 arguments
        // json_length(json_doc) - Returns length of JSON document
        // json_length(json_doc, path) - Returns length at path
        fc.getFunctionRegistry().registerUnaryBinaryPattern(
                "json_length",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER),
                "json_length(?1)",
                "json_length(?1, ?2)",
                STRING, STRING,
                typeConfig
        ).setArgumentListSignature("(json_doc[, path])");

        // json_depth(json_doc) - Returns maximum depth of JSON document
        fc.getFunctionRegistry().registerPattern("json_depth",
                "json_depth(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER));

        // json_storage_size(json_doc) - Returns approximate storage size in bytes
        fc.getFunctionRegistry().registerPattern("json_storage_size",
                "json_storage_size(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER));

        // json_storage_free(json_doc) - Returns freed space in bytes after partial
        // update
        fc.getFunctionRegistry().registerPattern("json_storage_free",
                "json_storage_free(?1)",
                typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER));
    }
}
