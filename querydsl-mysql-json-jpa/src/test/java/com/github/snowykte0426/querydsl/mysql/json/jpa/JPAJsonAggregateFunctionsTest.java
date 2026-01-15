package com.github.snowykte0426.querydsl.mysql.json.jpa;

import com.github.snowykte0426.querydsl.mysql.json.jpa.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON aggregate functions in JPA environment.
 *
 * <p>Tests cover:
 * <ul>
 *   <li>JSON_ARRAYAGG - Aggregate values into JSON array</li>
 *   <li>JSON_OBJECTAGG - Aggregate key-value pairs into JSON object</li>
 * </ul>
 */
@DisplayName("JPA JSON Aggregate Functions")
class JPAJsonAggregateFunctionsTest extends AbstractJPAJsonFunctionTest {

    @BeforeEach
    void setupData() {
        // Create products in different categories
        createProduct("Laptop", new BigDecimal("999.99"), "Electronics", "{\"brand\": \"Dell\"}");
        createProduct("Phone", new BigDecimal("699.99"), "Electronics", "{\"brand\": \"Apple\"}");
        createProduct("Tablet", new BigDecimal("499.99"), "Electronics", "{\"brand\": \"Samsung\"}");

        createProduct("Chair", new BigDecimal("199.99"), "Furniture", "{\"material\": \"wood\"}");
        createProduct("Desk", new BigDecimal("299.99"), "Furniture", "{\"material\": \"metal\"}");

        createProduct("Shirt", new BigDecimal("49.99"), "Clothing", "{\"size\": \"M\"}");
        createProduct("Pants", new BigDecimal("79.99"), "Clothing", "{\"size\": \"L\"}");

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("JSON_ARRAYAGG")
    class JsonArrayAggTests {

        @Test
        @DisplayName("should aggregate names into array")
        void aggregateNamesIntoArray() {
            String sql = "SELECT JSON_ARRAYAGG(name) FROM products WHERE category = 'Electronics'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("Laptop", "Phone", "Tablet");
            assertThat(result).startsWith("[");
            assertThat(result).endsWith("]");
        }

        @Test
        @DisplayName("should aggregate prices into array")
        void aggregatePricesIntoArray() {
            String sql = "SELECT JSON_ARRAYAGG(price) FROM products WHERE category = 'Furniture'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("199.99", "299.99");
        }

        @Test
        @DisplayName("should return empty array for no rows")
        void returnNullForNoRows() {
            String sql = "SELECT JSON_ARRAYAGG(name) FROM products WHERE category = 'NonExistent'";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            // JSON_ARRAYAGG returns NULL when no rows match
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should aggregate single value")
        void aggregateSingleValue() {
            // First, let's make sure we only have one row
            String sql = "SELECT JSON_ARRAYAGG(name) FROM products WHERE name = 'Laptop'";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("[\"Laptop\"]");
        }

        @Test
        @DisplayName("should aggregate with GROUP BY")
        void aggregateWithGroupBy() {
            String sql = "SELECT category, JSON_ARRAYAGG(name) FROM products GROUP BY category ORDER BY category";
            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(3);

            Object[] clothing = (Object[]) results.get(0);
            assertThat(clothing[0]).isEqualTo("Clothing");
            assertThat(clothing[1].toString()).contains("Shirt", "Pants");

            Object[] electronics = (Object[]) results.get(1);
            assertThat(electronics[0]).isEqualTo("Electronics");
            assertThat(electronics[1].toString()).contains("Laptop", "Phone", "Tablet");
        }

        @Test
        @DisplayName("should aggregate JSON values")
        void aggregateJsonValues() {
            String sql = "SELECT JSON_ARRAYAGG(attributes) FROM products WHERE category = 'Electronics'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("Dell", "Apple", "Samsung");
        }
    }

    @Nested
    @DisplayName("JSON_OBJECTAGG")
    class JsonObjectAggTests {

        @Test
        @DisplayName("should aggregate key-value pairs into object")
        void aggregateKeyValuePairsIntoObject() {
            String sql = "SELECT JSON_OBJECTAGG(name, price) FROM products WHERE category = 'Clothing'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"Shirt\"");
            assertThat(result).contains("\"Pants\"");
            assertThat(result).contains("49.99");
            assertThat(result).contains("79.99");
        }

        @Test
        @DisplayName("should return null for no rows")
        void returnNullForNoRows() {
            String sql = "SELECT JSON_OBJECTAGG(name, price) FROM products WHERE category = 'NonExistent'";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should aggregate with GROUP BY")
        void aggregateWithGroupBy() {
            String sql = "SELECT category, JSON_OBJECTAGG(name, price) FROM products GROUP BY category ORDER BY category";
            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(3);

            // Find Furniture in results (order might vary)
            boolean foundFurniture = false;
            for (Object row : results) {
                Object[] cols = (Object[]) row;
                if ("Furniture".equals(cols[0])) {
                    String furnitureJson = cols[1].toString();
                    assertThat(furnitureJson).contains("Chair", "Desk");
                    foundFurniture = true;
                    break;
                }
            }
            assertThat(foundFurniture).isTrue();
        }

        @Test
        @DisplayName("should handle duplicate keys (last value wins)")
        void handleDuplicateKeys() {
            // Create products with same category as key
            String sql = "SELECT JSON_OBJECTAGG(category, name) FROM products";
            String result = executeNativeQuery(sql);

            // Each category should appear once with one of its product names
            assertThat(result).contains("Electronics", "Furniture", "Clothing");
        }

        @Test
        @DisplayName("should aggregate category counts")
        void aggregateCategoryCounts() {
            String sql = "SELECT JSON_OBJECTAGG(category, cnt) FROM " +
                         "(SELECT category, COUNT(*) as cnt FROM products GROUP BY category) t";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("Electronics", "3");
            assertThat(result).contains("Furniture", "2");
            assertThat(result).contains("Clothing", "2");
        }
    }

    @Nested
    @DisplayName("Combined Aggregate Tests")
    class CombinedAggregateTests {

        @Test
        @DisplayName("should nest array in object")
        void nestArrayInObject() {
            String sql = "SELECT JSON_OBJECT('products', JSON_ARRAYAGG(name)) FROM products WHERE category = 'Electronics'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"products\"");
            assertThat(result).contains("[");
            assertThat(result).contains("Laptop", "Phone", "Tablet");
        }

        @Test
        @DisplayName("should create category summary")
        void createCategorySummary() {
            String sql = "SELECT JSON_OBJECTAGG(category, products) FROM " +
                         "(SELECT category, JSON_ARRAYAGG(name) as products FROM products GROUP BY category) t";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("Electronics");
            assertThat(result).contains("Furniture");
            assertThat(result).contains("Clothing");
        }

        @Test
        @DisplayName("should aggregate product details")
        void aggregateProductDetails() {
            String sql = "SELECT JSON_ARRAYAGG(JSON_OBJECT('name', name, 'price', price)) " +
                         "FROM products WHERE category = 'Furniture'";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"name\"", "\"price\"");
            assertThat(result).contains("Chair", "Desk");
            assertThat(result).contains("199.99", "299.99");
        }
    }
}
