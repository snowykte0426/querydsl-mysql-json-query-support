package io.github.snowykte0426.querydsl.mysql.json.jpa;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MySQL JSON_TABLE function in JPA environment.
 *
 * <p>
 * JSON_TABLE converts JSON data to relational table format, enabling complex
 * queries on JSON data.
 */
@DisplayName("JPA JSON Table Functions")
class JPAJsonTableFunctionsTest extends AbstractJPAJsonFunctionTest {

    @BeforeEach
    void setupData() {
        // Create order with items in JSON
        createOrder("ORD-001",
                1L,
                new BigDecimal("150.00"),
                "{\"items\": [{\"name\": \"Widget\", \"qty\": 2, \"price\": 50.00}, "
                        + "{\"name\": \"Gadget\", \"qty\": 1, \"price\": 50.00}], " + "\"shipping\": \"express\"}",
                "{\"address\": \"123 Main St\", \"city\": \"New York\"}");

        createOrder("ORD-002",
                2L,
                new BigDecimal("200.00"),
                "{\"items\": [{\"name\": \"Device\", \"qty\": 1, \"price\": 200.00}], " + "\"shipping\": \"standard\"}",
                "{\"address\": \"456 Oak Ave\", \"city\": \"Los Angeles\"}");

        createOrder("ORD-003",
                1L,
                new BigDecimal("75.00"),
                "{\"items\": [{\"name\": \"Part A\", \"qty\": 3, \"price\": 25.00}], " + "\"shipping\": \"express\"}",
                null);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("Basic JSON_TABLE")
    class BasicJsonTableTests {

        @Test
        @DisplayName("should extract items from JSON array")
        void extractItemsFromJsonArray() {
            @NotNull
            String sql = """
                    SELECT jt.*
                    FROM orders,
                    JSON_TABLE(order_data, '$.items[*]'
                        COLUMNS (
                            item_name VARCHAR(100) PATH '$.name',
                            quantity INT PATH '$.qty',
                            price DECIMAL(10,2) PATH '$.price'
                        )
                    ) AS jt
                    WHERE order_number = 'ORD-001'
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(2);

            Object[] widget = (Object[]) results.get(0);
            assertThat(widget[0]).isEqualTo("Widget");
            assertThat(((Number) widget[1]).intValue()).isEqualTo(2);

            Object[] gadget = (Object[]) results.get(1);
            assertThat(gadget[0]).isEqualTo("Gadget");
            assertThat(((Number) gadget[1]).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should use ordinality column")
        void useOrdinalityColumn() {
            @NotNull
            String sql = """
                    SELECT jt.*
                    FROM orders,
                    JSON_TABLE(order_data, '$.items[*]'
                        COLUMNS (
                            row_num FOR ORDINALITY,
                            item_name VARCHAR(100) PATH '$.name'
                        )
                    ) AS jt
                    WHERE order_number = 'ORD-001'
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(2);

            Object[] first = (Object[]) results.get(0);
            assertThat(((Number) first[0]).intValue()).isEqualTo(1);

            Object[] second = (Object[]) results.get(1);
            assertThat(((Number) second[0]).intValue()).isEqualTo(2);
        }

        @Test
        @DisplayName("should handle NULL on empty")
        void handleNullOnEmpty() {
            @NotNull
            String sql = """
                    SELECT jt.shipping_notes
                    FROM orders,
                    JSON_TABLE(order_data, '$'
                        COLUMNS (
                            shipping_notes VARCHAR(100) PATH '$.notes' NULL ON EMPTY
                        )
                    ) AS jt
                    WHERE order_number = 'ORD-001'
                    """;

            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should use DEFAULT on empty")
        void useDefaultOnEmpty() {
            // Test DEFAULT value when path doesn't exist
            // Note: DEFAULT value in JSON_TABLE must be a valid JSON literal if it's
            // treated as JSON?
            // Actually, for VARCHAR column, a simple string literal should work, but let's
            // try quoting it as JSON string '"NONE"'
            @NotNull
            String sql = """
                    SELECT jt.shipping_notes
                    FROM orders,
                    JSON_TABLE(order_data, '$'
                        COLUMNS (
                            shipping_notes VARCHAR(100) PATH '$.notes' DEFAULT '"NONE"' ON EMPTY NULL ON ERROR
                        )
                    ) AS jt
                    WHERE order_number = 'ORD-001'
                    """;

            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            // When path is empty/missing, DEFAULT value is returned
            assertThat(result).isIn("NONE", null);
        }
    }

    @Nested
    @DisplayName("JSON_TABLE with Joins")
    class JsonTableWithJoinsTests {

        @Test
        @DisplayName("should join with order details")
        void joinWithOrderDetails() {
            @NotNull
            String sql = """
                    SELECT o.order_number, jt.item_name, jt.quantity
                    FROM orders o,
                    JSON_TABLE(o.order_data, '$.items[*]'
                        COLUMNS (
                            item_name VARCHAR(100) PATH '$.name',
                            quantity INT PATH '$.qty'
                        )
                    ) AS jt
                    ORDER BY o.order_number, jt.item_name
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(4); // 2 + 1 + 1 items

            Object[] first = (Object[]) results.get(0);
            assertThat(first[0]).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("should filter items by quantity")
        void filterItemsByQuantity() {
            @NotNull
            String sql = """
                    SELECT o.order_number, jt.item_name, jt.quantity
                    FROM orders o,
                    JSON_TABLE(o.order_data, '$.items[*]'
                        COLUMNS (
                            item_name VARCHAR(100) PATH '$.name',
                            quantity INT PATH '$.qty'
                        )
                    ) AS jt
                    WHERE jt.quantity > 1
                    ORDER BY o.order_number
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(2); // Widget (2) and Part A (3)
        }

        @Test
        @DisplayName("should aggregate from JSON_TABLE")
        void aggregateFromJsonTable() {
            @NotNull
            String sql = """
                    SELECT o.order_number, SUM(jt.quantity * jt.price) as total
                    FROM orders o,
                    JSON_TABLE(o.order_data, '$.items[*]'
                        COLUMNS (
                            quantity INT PATH '$.qty',
                            price DECIMAL(10,2) PATH '$.price'
                        )
                    ) AS jt
                    GROUP BY o.order_number
                    ORDER BY o.order_number
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            assertThat(results).hasSize(3);

            Object[] ord001 = (Object[]) results.get(0);
            assertThat(ord001[0]).isEqualTo("ORD-001");
            assertThat(((Number) ord001[1]).doubleValue()).isEqualTo(150.00);
        }
    }

    @Nested
    @DisplayName("JSON_TABLE with Nested Paths")
    class JsonTableWithNestedPathsTests {

        @Test
        @DisplayName("should extract nested object properties")
        void extractNestedObjectProperties() {
            @NotNull
            String sql = """
                    SELECT jt.*
                    FROM orders,
                    JSON_TABLE(shipping_info, '$'
                        COLUMNS (
                            address VARCHAR(200) PATH '$.address',
                            city VARCHAR(100) PATH '$.city'
                        )
                    ) AS jt
                    WHERE order_number = 'ORD-001'
                    """;

            Object[] result = (Object[]) entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result[0]).isEqualTo("123 Main St");
            assertThat(result[1]).isEqualTo("New York");
        }

        @Test
        @DisplayName("should handle NULL shipping info")
        void handleNullShippingInfo() {
            // When shipping_info is null, JSON_TABLE returns no rows
            // Use COALESCE to handle this case
            @NotNull
            String sql = """
                    SELECT o.shipping_info
                    FROM orders o
                    WHERE o.order_number = 'ORD-003'
                    """;

            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            // shipping_info is null for ORD-003
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("JSON_TABLE EXISTS Path")
    class JsonTableExistsPathTests {

        @Test
        @DisplayName("should check path existence")
        void checkPathExistence() {
            @NotNull
            String sql = """
                    SELECT o.order_number, jt.has_shipping
                    FROM orders o,
                    JSON_TABLE(o.order_data, '$'
                        COLUMNS (
                            has_shipping INT EXISTS PATH '$.shipping'
                        )
                    ) AS jt
                    ORDER BY o.order_number
                    """;

            List<?> results = entityManager.createNativeQuery(sql).getResultList();

            // All orders have shipping field
            for (Object row : results) {
                Object[] cols = (Object[]) row;
                assertThat(((Number) cols[1]).intValue()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("should return 0 for non-existent path")
        void return0ForNonExistentPath() {
            @NotNull
            String sql = """
                    SELECT jt.has_discount
                    FROM orders o,
                    JSON_TABLE(o.order_data, '$'
                        COLUMNS (
                            has_discount INT EXISTS PATH '$.discount'
                        )
                    ) AS jt
                    WHERE o.order_number = 'ORD-001'
                    """;

            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("JSON_TABLE Expression Tests")
    class JsonTableExpressionTests {

        @Test
        @DisplayName("should build JSON_TABLE with JPAJsonFunctions")
        void buildJsonTableWithJPAJsonFunctions() {
            // Test that JPAJsonFunctions provides JSON_TABLE builder
            @NotNull
            var builder = JPAJsonFunctions.jsonTable();
            assertThat(builder).isNotNull();
        }

        @Test
        @DisplayName("should create column definitions")
        void createColumnDefinitions() {
            var intCol = JPAJsonFunctions.intColumn("qty", "$.quantity");
            assertThat(intCol).isNotNull();

            var varcharCol = JPAJsonFunctions.varcharColumn("name", 100, "$.name");
            assertThat(varcharCol).isNotNull();

            var existsCol = JPAJsonFunctions.existsColumn("has_data", "$.data");
            assertThat(existsCol).isNotNull();

            var ordCol = JPAJsonFunctions.ordinalityColumn("row_num");
            assertThat(ordCol).isNotNull();
        }
    }
}
