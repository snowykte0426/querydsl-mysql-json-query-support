package io.github.snowykte0426.querydsl.mysql.json.core.types;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a column definition in a JSON_TABLE expression.
 *
 * <p>
 * This class defines how JSON data should be mapped to relational columns in a
 * JSON_TABLE query. Each column has a name, SQL type, and JSON path.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.2
 */
public class JsonTableColumn {

    private final String columnName;
    private final String sqlType;
    private final String jsonPath;
    private final boolean exists;
    private final boolean ordinality;
    private final String onEmpty;
    private final String onError;

    private JsonTableColumn(Builder builder) {
        this.columnName = builder.columnName;
        this.sqlType = builder.sqlType;
        this.jsonPath = builder.jsonPath;
        this.exists = builder.exists;
        this.ordinality = builder.ordinality;
        this.onEmpty = builder.onEmpty;
        this.onError = builder.onError;
    }

    /**
     * Creates a standard column definition.
     *
     * @param columnName
     *            the column name
     * @param sqlType
     *            the SQL data type (e.g., "INT", "VARCHAR(100)")
     * @param jsonPath
     *            the JSON path expression
     * @return column definition
     */
    public static JsonTableColumn column(String columnName, String sqlType, String jsonPath) {
        return builder().columnName(columnName).sqlType(sqlType).jsonPath(jsonPath).build();
    }

    /**
     * Creates an EXISTS column that returns 1 if path exists, 0 otherwise.
     *
     * @param columnName
     *            the column name
     * @param jsonPath
     *            the JSON path to check
     * @return EXISTS column definition
     */
    public static JsonTableColumn exists(String columnName, String jsonPath) {
        return builder().columnName(columnName).exists(true).jsonPath(jsonPath).build();
    }

    /**
     * Creates an ORDINALITY column that provides row numbering.
     *
     * @param columnName
     *            the column name
     * @return ORDINALITY column definition
     */
    public static JsonTableColumn ordinality(String columnName) {
        return builder().columnName(columnName).ordinality(true).build();
    }

    /**
     * Creates a builder for column definition.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Generates the SQL column definition string.
     *
     * @return SQL fragment for this column
     */
    public String toSql() {
        if (ordinality) {
            return columnName + " FOR ORDINALITY";
        }

        if (exists) {
            return columnName + " " + (sqlType != null ? sqlType : "INT") + " EXISTS PATH " + quoteIfNeeded(jsonPath);
        }

        StringBuilder sql = new StringBuilder();
        sql.append(columnName);

        if (sqlType != null) {
            sql.append(" ").append(sqlType);
        }

        sql.append(" PATH ").append(quoteIfNeeded(jsonPath));

        if (onEmpty != null) {
            sql.append(" ").append(onEmpty).append(" ON EMPTY");
        }

        if (onError != null) {
            sql.append(" ").append(onError).append(" ON ERROR");
        }

        return sql.toString();
    }

    private String quoteIfNeeded(String path) {
        if (path == null) {
            return "NULL";
        }
        if (path.startsWith("'") && path.endsWith("'")) {
            return path;
        }
        return "'" + path.replace("'", "''") + "'";
    }

    public String getColumnName() {
        return columnName;
    }

    public String getSqlType() {
        return sqlType;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public boolean isExists() {
        return exists;
    }

    public boolean isOrdinality() {
        return ordinality;
    }

    /**
     * Builder for JsonTableColumn.
     */
    public static class Builder {
        private String columnName;
        private String sqlType;
        private String jsonPath;
        private boolean exists = false;
        private boolean ordinality = false;
        private String onEmpty;
        private String onError;

        /**
         * Sets the column name.
         *
         * @param columnName
         *            the column name
         * @return this builder
         */
        public Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        /**
         * Sets the SQL data type.
         *
         * @param sqlType
         *            the SQL type (e.g., "INT", "VARCHAR(100)", "JSON")
         * @return this builder
         */
        public Builder sqlType(String sqlType) {
            this.sqlType = sqlType;
            return this;
        }

        /**
         * Sets the JSON path expression.
         *
         * @param jsonPath
         *            the JSON path
         * @return this builder
         */
        public Builder jsonPath(String jsonPath) {
            this.jsonPath = jsonPath;
            return this;
        }

        /**
         * Makes this an EXISTS column.
         *
         * @param exists
         *            true for EXISTS column
         * @return this builder
         */
        public Builder exists(boolean exists) {
            this.exists = exists;
            return this;
        }

        /**
         * Makes this an ORDINALITY column.
         *
         * @param ordinality
         *            true for ORDINALITY column
         * @return this builder
         */
        public Builder ordinality(boolean ordinality) {
            this.ordinality = ordinality;
            return this;
        }

        /**
         * Sets the default value or action on empty result.
         *
         * @param onEmpty
         *            default value (e.g., "DEFAULT 0", "NULL", "ERROR")
         * @return this builder
         */
        public Builder onEmpty(@Nullable String onEmpty) {
            this.onEmpty = onEmpty;
            return this;
        }

        /**
         * Sets the default value or action on error.
         *
         * @param onError
         *            default value (e.g., "DEFAULT 0", "NULL", "ERROR")
         * @return this builder
         */
        public Builder onError(@Nullable String onError) {
            this.onError = onError;
            return this;
        }

        /**
         * Builds the column definition.
         *
         * @return JsonTableColumn instance
         */
        public JsonTableColumn build() {
            if (columnName == null) {
                throw new IllegalStateException("columnName is required");
            }
            if (!ordinality && !exists && jsonPath == null) {
                throw new IllegalStateException("jsonPath is required for non-ORDINALITY columns");
            }
            return new JsonTableColumn(this);
        }
    }
}
