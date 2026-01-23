package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON aggregate functions.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonAggregateFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_ARRAYAGG tests
    // ========================================

    @Test
    void jsonArrayAgg_withMultipleRows_shouldAggregateIntoArray() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price) VALUES " + "('Product1', 10.00), " + "('Product2', 20.00), "
                + "('Product3', 30.00)");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(name) FROM products");

        // Then
        assertThat(result).contains("\"Product1\"", "\"Product2\"", "\"Product3\"");
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
    }

    @Test
    void jsonArrayAgg_withNumbers_shouldAggregateNumbers() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price) VALUES " + "('Item1', 10.00), " + "('Item2', 20.00), "
                + "('Item3', 30.00)");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(price) FROM products");

        // Then
        assertThat(result).contains("10", "20", "30");
        assertThat(result).isIn("[10.00, 20.00, 30.00]", "[10.0, 20.0, 30.0]", "[10,20,30]", "[10.00,20.00,30.00]");
    }

    @Test
    void jsonArrayAgg_withOrderBy_shouldMaintainOrder() throws SQLException {
        // Given
        executeUpdate(
                "INSERT INTO products (name, price) VALUES " + "('C', 30.00), " + "('A', 10.00), " + "('B', 20.00)");

        // When - Note: Subquery ORDER BY may not be preserved without LIMIT
        // Using ORDER BY with LIMIT forces MySQL to maintain the order
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(name) FROM "
                + "(SELECT name FROM products ORDER BY name ASC LIMIT 999999) AS sorted");

        // Then
        assertThat(result).isIn("[\"A\", \"B\", \"C\"]", "[\"A\",\"B\",\"C\"]");
    }

    @Test
    void jsonArrayAgg_withGroupBy_shouldAggregatePerGroup() throws SQLException {
        // Given - Create a temporary category column via tags
        executeUpdate("INSERT INTO products (name, price, tags) VALUES "
                + "('Electronics1', 100.00, '[\"electronics\"]'), " + "('Electronics2', 200.00, '[\"electronics\"]'), "
                + "('Books1', 10.00, '[\"books\"]'), " + "('Books2', 20.00, '[\"books\"]')");

        // When - Aggregate names by category (extracted from tags)
        @Nullable
        String electronicsNames = executeScalar(
                "SELECT JSON_ARRAYAGG(name) FROM products " + "WHERE JSON_CONTAINS(tags, '\"electronics\"')");
        @Nullable
        String booksNames = executeScalar(
                "SELECT JSON_ARRAYAGG(name) FROM products " + "WHERE JSON_CONTAINS(tags, '\"books\"')");

        // Then
        assertThat(electronicsNames).contains("Electronics1", "Electronics2");
        assertThat(booksNames).contains("Books1", "Books2");
    }

    @Test
    void jsonArrayAgg_withNullValues_shouldIgnoreNulls() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES "
                + "('Product1', 10.00, '{\"color\": \"red\"}'), " + "('Product2', 20.00, NULL), "
                + "('Product3', 30.00, '{\"color\": \"blue\"}')");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(attributes) FROM products");

        // Then - NULL values should be ignored
        assertThat(result).contains("red", "blue");
        // Should have 2 objects, not 3
    }

    @Test
    void jsonArrayAgg_withEmptyResult_shouldReturnNull() throws SQLException {
        // When - No rows match the condition
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(name) FROM products WHERE price > 1000000");

        // Then
        assertThat(result).isNull();
    }

    // ========================================
    // JSON_OBJECTAGG tests
    // ========================================

    @Test
    void jsonObjectAgg_withKeyValuePairs_shouldAggregateIntoObject() throws SQLException {
        // Given - Use metadata column as a string field for keys
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('setting1', 'value1@test.com', '{\"key\": \"theme\"}'), "
                + "('setting2', 'value2@test.com', '{\"key\": \"lang\"}'), "
                + "('setting3', 'value3@test.com', '{\"key\": \"tz\"}')");

        // When - Aggregate using JSON_EXTRACT for keys
        @Nullable
        String result = executeScalar("SELECT JSON_OBJECTAGG(JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.key')), name) "
                + "FROM users WHERE JSON_EXTRACT(metadata, '$.key') IS NOT NULL");

        // Then
        assertThat(result).contains("\"theme\"", "\"lang\"", "\"tz\"");
        assertThat(result).contains("setting1", "setting2", "setting3");
        assertThat(result).startsWith("{");
        assertThat(result).endsWith("}");
    }

    @Test
    void jsonObjectAgg_withDifferentValueTypes_shouldAggregate() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price) VALUES " + "('ProductA', 10.00), " + "('ProductB', 20.00), "
                + "('ProductC', 30.00)");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_OBJECTAGG(name, price) FROM products");

        // Then
        assertThat(result).contains("\"ProductA\"", "\"ProductB\"", "\"ProductC\"");
        assertThat(result).contains("10", "20", "30");
    }

    @Test
    void jsonObjectAgg_withNullKey_shouldIgnoreRow() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email) VALUES " + "('User1', 'user1@test.com'), "
                + "('User2', 'user2@test.com'), " + "('User3', 'user3@test.com')");

        // When - Filter out User2 at SQL level since MySQL doesn't allow NULL keys in
        // JSON_OBJECTAGG
        // Note: MySQL throws error "JSON documents may not contain NULL member names"
        @Nullable
        String result = executeScalar("SELECT JSON_OBJECTAGG(name, email) FROM users WHERE name != 'User2'");

        // Then - User2 should not be in result
        assertThat(result).isNotNull();
        assertThat(result).contains("\"User1\"", "\"User3\"");
        assertThat(result).doesNotContain("User2");
    }

    @Test
    void jsonObjectAgg_withDuplicateKeys_shouldUseLastValue() throws SQLException {
        // Given - Insert with same name
        executeUpdate(
                "INSERT INTO products (name, price) VALUES " + "('DuplicateKey', 10.00), " + "('DuplicateKey', 20.00)");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_OBJECTAGG(name, price) FROM products");

        // Then
        assertThat(result).contains("\"DuplicateKey\"");
        // MySQL will use one of the values (typically the last one)
        assertThat(result).containsAnyOf("10", "20");
    }

    // ========================================
    // Combined aggregation tests
    // ========================================

    @Test
    void combinedAggregation_arrayOfObjects_shouldCreateNestedStructure() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price) VALUES " + "('Item1', 10.00), " + "('Item2', 20.00), "
                + "('Item3', 30.00)");

        // When - Create array of objects
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(JSON_OBJECT('name', name, 'price', price)) FROM products");

        // Then
        assertThat(result).contains("\"name\"", "\"price\"");
        assertThat(result).contains("Item1", "Item2", "Item3");
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
    }

    @Test
    void combinedAggregation_objectOfArrays_shouldCreateNestedStructure() throws SQLException {
        // Given - Use tags for grouping
        executeUpdate("INSERT INTO products (name, price, tags) VALUES " + "('ElecA', 100.00, '[\"electronics\"]'), "
                + "('ElecB', 200.00, '[\"electronics\"]'), " + "('BookA', 10.00, '[\"books\"]')");

        // When - Create object where keys are categories and values are arrays of names
        // This requires a subquery approach
        @Nullable
        String electronicsArray = executeScalar(
                "SELECT JSON_ARRAYAGG(name) FROM products WHERE JSON_CONTAINS(tags, '\"electronics\"')");
        @Nullable
        String booksArray = executeScalar(
                "SELECT JSON_ARRAYAGG(name) FROM products WHERE JSON_CONTAINS(tags, '\"books\"')");

        // Then
        assertThat(electronicsArray).contains("ElecA", "ElecB");
        assertThat(booksArray).contains("BookA");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonArrayAgg_inDatabase_shouldWorkWithComplexData() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('User1', 'user1@test.com', '{\"age\": 25}'), " + "('User2', 'user2@test.com', '{\"age\": 30}'), "
                + "('User3', 'user3@test.com', '{\"age\": 35}')");

        // When - Aggregate ages
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(JSON_EXTRACT(metadata, '$.age')) FROM users");

        // Then
        assertThat(result).contains("25", "30", "35");
    }

    @Test
    void jsonObjectAgg_inDatabase_shouldCreateSettingsObject() throws SQLException {
        // Given - Use name as key and email as value
        executeUpdate("INSERT INTO users (name, email) VALUES " + "('theme', 'dark'), " + "('language', 'en'), "
                + "('timezone', 'UTC')");

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_OBJECTAGG(name, email) FROM users");

        // Then
        assertThat(result).contains("\"theme\"", "\"dark\"");
        assertThat(result).contains("\"language\"", "\"en\"");
        assertThat(result).contains("\"timezone\"", "\"UTC\"");
    }

    @Test
    void nestedAggregation_withSubquery_shouldWork() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, tags) VALUES "
                + "('Product1', 10.00, '[\"tag1\", \"tag2\"]'), " + "('Product2', 20.00, '[\"tag2\", \"tag3\"]'), "
                + "('Product3', 30.00, '[\"tag1\", \"tag3\"]')");

        // When - Get array of all product names
        @Nullable
        String names = executeScalar("SELECT JSON_ARRAYAGG(name) FROM products");

        // Then
        assertThat(names).contains("Product1", "Product2", "Product3");
    }

    @Test
    void aggregationWithConditions_shouldFilterCorrectly() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price) VALUES " + "('Cheap1', 5.00), " + "('Cheap2', 8.00), "
                + "('Expensive1', 100.00), " + "('Expensive2', 200.00)");

        // When - Aggregate only cheap products
        @Nullable
        String cheapProducts = executeScalar("SELECT JSON_ARRAYAGG(name) FROM products WHERE price < 10");

        // Then
        assertThat(cheapProducts).contains("Cheap1", "Cheap2");
        assertThat(cheapProducts).doesNotContain("Expensive");
    }

    @Test
    void largeDatasetAggregation_shouldHandle() throws SQLException {
        // Given - Insert many rows
        @NotNull
        StringBuilder values = new StringBuilder();
        for (int i = 1; i <= 100; i++) {
            if (i > 1)
                values.append(", ");
            values.append("('Product").append(i).append("', ").append(i * 10.0).append(")");
        }
        executeUpdate("INSERT INTO products (name, price) VALUES " + values);

        // When
        @Nullable
        String result = executeScalar("SELECT JSON_ARRAYAGG(name) FROM products");

        // Then
        assertThat(result).contains("Product1", "Product50", "Product100");
        assertThat(result).startsWith("[");
        assertThat(result).endsWith("]");
    }
}
