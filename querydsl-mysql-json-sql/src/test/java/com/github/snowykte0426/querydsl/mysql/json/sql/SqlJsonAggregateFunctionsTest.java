package com.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON aggregate functions in SQL module.
 */
class SqlJsonAggregateFunctionsTest extends AbstractSqlJsonFunctionTest {

    @BeforeEach
    void setupTestData() throws SQLException {
        createUser("Alice", "alice@example.com", "{}");
        createUser("Bob", "bob@example.com", "{}");
        createUser("Charlie", "charlie@example.com", "{}");
    }

    @Test
    void jsonArrayAgg_shouldAggregateValues() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_ARRAYAGG(name) FROM users"
        );
        assertThat(result).contains("Alice", "Bob", "Charlie");
    }

    @Test
    void jsonArrayAgg_withNumbers_shouldWork() throws SQLException {
        createProduct("P1", BigDecimal.valueOf(100), "cat1", "{}");
        createProduct("P2", BigDecimal.valueOf(200), "cat1", "{}");
        
        String result = executeNativeQuery(
            "SELECT JSON_ARRAYAGG(price) FROM products"
        );
        assertThat(result).contains("100", "200");
    }

    @Test
    void jsonObjectAgg_shouldAggregateKeyValues() throws SQLException {
        String result = executeNativeQuery(
            "SELECT JSON_OBJECTAGG(name, email) FROM users LIMIT 3"
        );
        assertThat(result).contains("Alice", "alice@example.com");
    }

    @Test
    void jsonArrayAgg_withGroupBy_shouldGroupResults() throws SQLException {
        createProduct("P1", BigDecimal.valueOf(100), "electronics", "{}");
        createProduct("P2", BigDecimal.valueOf(200), "electronics", "{}");
        createProduct("P3", BigDecimal.valueOf(50), "books", "{}");
        
        String result = executeNativeQuery(
            "SELECT category, JSON_ARRAYAGG(name) FROM products GROUP BY category LIMIT 1"
        );
        assertThat(result).isNotNull();
    }

    @Test
    void jsonArrayAgg_empty_shouldReturnNull() throws SQLException {
        clearTestData();
        
        String result = executeNativeQuery(
            "SELECT JSON_ARRAYAGG(name) FROM users"
        );
        assertThat(result).isNull();
    }
}
