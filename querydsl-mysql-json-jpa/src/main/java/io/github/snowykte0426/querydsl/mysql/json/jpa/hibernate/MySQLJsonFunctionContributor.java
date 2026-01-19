package io.github.snowykte0426.querydsl.mysql.json.jpa.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.query.sqm.function.NamedSqmFunctionDescriptor;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.type.BasicType;
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
        final BasicType<Boolean> booleanType = typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN);

        // json_contains: 2-3 arguments
        fc.getFunctionRegistry()
                .registerBinaryTernaryPattern("json_contains",
                        booleanType,
                        "json_contains(?1, ?2)",
                        "json_contains(?1, ?2, ?3)",
                        STRING,
                        STRING,
                        STRING,
                        typeConfig)
                .setArgumentListSignature("(target, candidate[, path])");

        // json_contains_path: 3-5 arguments
        fc.getFunctionRegistry()
                .registerTernaryQuaternaryPattern("json_contains_path",
                        booleanType,
                        "json_contains_path(?1, ?2, ?3)",
                        "json_contains_path(?1, ?2, ?3, ?4)",
                        STRING,
                        STRING,
                        STRING,
                        STRING,
                        typeConfig)
                .setArgumentListSignature("(target, one_or_all, path1[, path2])");

        // Support for 5 arguments (target, one_or_all, path1, path2, path3)
        fc.getFunctionRegistry()
                .registerPattern("json_contains_path_5", "json_contains_path(?1, ?2, ?3, ?4, ?5)", booleanType);

        // json_overlaps(json1, json2)
        fc.getFunctionRegistry().registerPattern("json_overlaps", "json_overlaps(?1, ?2)", booleanType);

        // json_valid(value)
        fc.getFunctionRegistry().registerPattern("json_valid", "json_valid(?1)", booleanType);

        // json_schema_valid(schema, document)
        fc.getFunctionRegistry().registerPattern("json_schema_valid", "json_schema_valid(?1, ?2)", booleanType);

        // member_of
        fc.getFunctionRegistry().registerPattern("member_of", "?1 member of(?2)", booleanType);
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
        final BasicType<String> stringType = typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.STRING);

        // json_extract: Register with NamedSqmFunctionDescriptor and NULL argument type
        // resolver
        // to allow correct type inference for JSON columns and parameters.
        fc.getFunctionRegistry().register("json_extract",
                new NamedSqmFunctionDescriptor("json_extract",
                        false,
                        StandardArgumentsValidators.exactly(2),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null // Allow argument type inference
                ));

        // Support for more arguments (3, 4, 5)
        fc.getFunctionRegistry().register("json_extract_3",
                new NamedSqmFunctionDescriptor("json_extract",
                        false,
                        StandardArgumentsValidators.exactly(3),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));
        fc.getFunctionRegistry().register("json_extract_4",
                new NamedSqmFunctionDescriptor("json_extract",
                        false,
                        StandardArgumentsValidators.exactly(4),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));
        fc.getFunctionRegistry().register("json_extract_5",
                new NamedSqmFunctionDescriptor("json_extract",
                        false,
                        StandardArgumentsValidators.exactly(5),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));

        // json_unquote(json_val)
        fc.getFunctionRegistry().registerPattern("json_unquote", "json_unquote(?1)", stringType);

        // json_type(json_val)
        fc.getFunctionRegistry().registerPattern("json_type", "json_type(?1)", stringType);

        // json_search: Register manually for type inference
        fc.getFunctionRegistry().register("json_search",
                new NamedSqmFunctionDescriptor("json_search",
                        false,
                        StandardArgumentsValidators.exactly(3),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));

        // Support for 4 and 5 arguments
        fc.getFunctionRegistry().register("json_search_4",
                new NamedSqmFunctionDescriptor("json_search",
                        false,
                        StandardArgumentsValidators.exactly(4),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));
        fc.getFunctionRegistry().register("json_search_5",
                new NamedSqmFunctionDescriptor("json_search",
                        false,
                        StandardArgumentsValidators.exactly(5),
                        StandardFunctionReturnTypeResolvers.invariant(stringType),
                        null));

        // json_keys: 1-2 arguments
        fc.getFunctionRegistry()
                .registerUnaryBinaryPattern("json_keys",
                        stringType,
                        "json_keys(?1)",
                        "json_keys(?1, ?2)",
                        STRING,
                        STRING,
                        typeConfig)
                .setArgumentListSignature("(json_doc[, path])");

        // json_quote(string)
        fc.getFunctionRegistry().registerPattern("json_quote", "json_quote(?1)", stringType);

        // json_pretty(json_val)
        fc.getFunctionRegistry().registerPattern("json_pretty", "json_pretty(?1)", stringType);

        // json_schema_validation_report(schema, document)
        fc.getFunctionRegistry()
                .registerPattern("json_schema_validation_report", "json_schema_validation_report(?1, ?2)", stringType);

        // json_value: 2-3 arguments
        fc.getFunctionRegistry()
                .registerBinaryTernaryPattern("json_value",
                        stringType,
                        "json_value(?1, ?2)",
                        "json_value(?1, ?2 returning ?3)",
                        STRING,
                        STRING,
                        STRING,
                        typeConfig)
                .setArgumentListSignature("(json_doc, path[, RETURNING type])");
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
        final BasicType<Integer> intType = typeConfig.getBasicTypeRegistry().resolve(StandardBasicTypes.INTEGER);

        // json_length: 1-2 arguments
        fc.getFunctionRegistry()
                .registerUnaryBinaryPattern("json_length",
                        intType,
                        "json_length(?1)",
                        "json_length(?1, ?2)",
                        STRING,
                        STRING,
                        typeConfig)
                .setArgumentListSignature("(json_doc[, path])");

        // json_depth(json_doc)
        fc.getFunctionRegistry().registerPattern("json_depth", "json_depth(?1)", intType);

        // json_storage_size(json_doc)
        fc.getFunctionRegistry().registerPattern("json_storage_size", "json_storage_size(?1)", intType);

        // json_storage_free(json_doc)
        fc.getFunctionRegistry().registerPattern("json_storage_free", "json_storage_free(?1)", intType);
    }
}
