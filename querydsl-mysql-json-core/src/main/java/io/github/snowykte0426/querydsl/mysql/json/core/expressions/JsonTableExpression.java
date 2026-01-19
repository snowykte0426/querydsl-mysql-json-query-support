package io.github.snowykte0426.querydsl.mysql.json.core.expressions;

import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonTableColumn;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder for MySQL JSON_TABLE function.
 *
 * <p>JSON_TABLE converts JSON data into a relational table format,
 * allowing JSON documents to be queried using standard SQL.
 *
 * <p>This class provides a fluent API for building JSON_TABLE expressions.
 * The final expression can be obtained via {@link #asExpression()}.
 *
 * <p>Example usage:
 * <pre>
 * SimpleExpression&lt;Object&gt; table = JsonTableExpression.builder()
 *     .jsonDoc(user.metadata)
 *     .path("$")
 *     .column("id", "INT", "$.id")
 *     .column("name", "VARCHAR(100)", "$.name")
 *     .alias("jt")
 *     .build()
 *     .asExpression();
 * </pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.1
 */
public class JsonTableExpression {

    private final Expression<?> jsonDoc;
    private final String path;
    private final List<JsonTableColumn> columns;
    private final String tableAlias;

    private JsonTableExpression(Builder builder) {
        this.jsonDoc = builder.jsonDoc;
        this.path = builder.path;
        this.columns = new ArrayList<>(builder.columns);
        this.tableAlias = builder.tableAlias;
    }

    /**
     * Creates a new builder for JSON_TABLE expression.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generates the complete JSON_TABLE SQL expression.
     *
     * <p>Format: {@code JSON_TABLE(json_doc, path COLUMNS(...)) AS alias}
     *
     * <p>This method generates a QueryDSL template string with {0} placeholder
     * for the JSON document expression. For direct SQL usage, use {@link #toCompleteSql()}.
     *
     * @return SQL template string for QueryDSL
     */
    public String toSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("JSON_TABLE(");

        // JSON document (will be parameterized by QueryDSL)
        sql.append("{0}");

        sql.append(", ");

        // Path
        sql.append(quoteIfNeeded(path));

        // Columns
        sql.append(" COLUMNS(");
        sql.append(columns.stream()
            .map(JsonTableColumn::toSql)
            .collect(Collectors.joining(", ")));
        sql.append(")");

        sql.append(")");

        // Table alias
        if (tableAlias != null && !tableAlias.isEmpty()) {
            sql.append(" AS ").append(tableAlias);
        }

        return sql.toString();
    }

    /**
     * Generates complete SQL with all values inlined (for testing).
     *
     * <p>This method converts the JSON document expression to its literal
     * representation and generates executable SQL.
     *
     * @return complete SQL string
     */
    public String toCompleteSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("JSON_TABLE(");

        // JSON document - convert expression to SQL literal
        if (jsonDoc instanceof com.querydsl.core.types.ConstantImpl) {
            Object value = ((com.querydsl.core.types.ConstantImpl<?>) jsonDoc).getConstant();
            if (value instanceof String) {
                sql.append("'").append(((String) value).replace("'", "''")).append("'");
            } else {
                sql.append(value);
            }
        } else {
            // For non-constant expressions, use toString() as fallback
            sql.append(jsonDoc.toString());
        }

        sql.append(", ");

        // Path
        sql.append(quoteIfNeeded(path));

        // Columns
        sql.append(" COLUMNS(");
        sql.append(columns.stream()
            .map(JsonTableColumn::toSql)
            .collect(Collectors.joining(", ")));
        sql.append(")");

        sql.append(")");

        // Table alias
        if (tableAlias != null && !tableAlias.isEmpty()) {
            sql.append(" AS ").append(tableAlias);
        }

        return sql.toString();
    }

    /**
     * Creates a QueryDSL expression for this JSON_TABLE.
     *
     * @return expression that can be used in queries
     */
    public SimpleExpression<Object> asExpression() {
        return Expressions.template(Object.class, toSql(), jsonDoc);
    }

    private String quoteIfNeeded(String value) {
        if (value == null) {
            return "NULL";
        }
        if (value.startsWith("'") && value.endsWith("'")) {
            return value;
        }
        return "'" + value.replace("'", "''") + "'";
    }

    public Expression<?> getJsonDoc() {
        return jsonDoc;
    }

    public String getPath() {
        return path;
    }

    public List<JsonTableColumn> getColumns() {
        return new ArrayList<>(columns);
    }

    public String getTableAlias() {
        return tableAlias;
    }

    /**
     * Builder for JsonTableExpression.
     */
    public static class Builder {
        private Expression<?> jsonDoc;
        private String path;
        private final List<JsonTableColumn> columns = new ArrayList<>();
        private String tableAlias;

        /**
         * Sets the JSON document to query.
         *
         * @param jsonDoc JSON document expression
         * @return this builder
         */
        public Builder jsonDoc(Expression<?> jsonDoc) {
            this.jsonDoc = jsonDoc;
            return this;
        }

        /**
         * Sets the JSON document from a string literal.
         *
         * @param jsonString JSON string
         * @return this builder
         */
        public Builder jsonDoc(String jsonString) {
            this.jsonDoc = Expressions.constant(jsonString);
            return this;
        }

        /**
         * Sets the root JSON path for the table.
         *
         * @param path JSON path (e.g., "$", "$[*]", "$.data")
         * @return this builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Adds a column definition.
         *
         * @param columnName the column name
         * @param sqlType the SQL data type
         * @param jsonPath the JSON path for this column
         * @return this builder
         */
        public Builder column(String columnName, String sqlType, String jsonPath) {
            this.columns.add(JsonTableColumn.column(columnName, sqlType, jsonPath));
            return this;
        }

        /**
         * Adds a column definition with custom settings.
         *
         * @param column the column definition
         * @return this builder
         */
        public Builder column(JsonTableColumn column) {
            this.columns.add(column);
            return this;
        }

        /**
         * Adds multiple column definitions.
         *
         * @param columns column definitions
         * @return this builder
         */
        public Builder columns(JsonTableColumn... columns) {
            this.columns.addAll(Arrays.asList(columns));
            return this;
        }

        /**
         * Adds an EXISTS column that returns 1/0 based on path existence.
         *
         * @param columnName the column name
         * @param jsonPath the JSON path to check
         * @return this builder
         */
        public Builder existsColumn(String columnName, String jsonPath) {
            this.columns.add(JsonTableColumn.exists(columnName, jsonPath));
            return this;
        }

        /**
         * Adds an ORDINALITY column for row numbering.
         *
         * @param columnName the column name
         * @return this builder
         */
        public Builder ordinalityColumn(String columnName) {
            this.columns.add(JsonTableColumn.ordinality(columnName));
            return this;
        }

        /**
         * Sets the table alias for this JSON_TABLE.
         *
         * @param alias the table alias (e.g., "jt", "data_table")
         * @return this builder
         */
        public Builder alias(String alias) {
            this.tableAlias = alias;
            return this;
        }

        /**
         * Builds the JSON_TABLE expression.
         *
         * @return JsonTableExpression instance
         */
        public JsonTableExpression build() {
            if (jsonDoc == null) {
                throw new IllegalStateException("jsonDoc is required");
            }
            if (path == null || path.isEmpty()) {
                throw new IllegalStateException("path is required");
            }
            if (columns.isEmpty()) {
                throw new IllegalStateException("at least one column is required");
            }
            return new JsonTableExpression(this);
        }
    }
}