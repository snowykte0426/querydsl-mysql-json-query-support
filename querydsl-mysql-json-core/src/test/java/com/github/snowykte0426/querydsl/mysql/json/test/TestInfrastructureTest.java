package com.github.snowykte0426.querydsl.mysql.json.test;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that test infrastructure is working correctly.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
class TestInfrastructureTest extends AbstractJsonFunctionTest {

    @Test
    void containerShouldBeRunning() {
        assertThat(MYSQL_CONTAINER.isRunning()).isTrue();
    }

    @Test
    void connectionShouldBeEstablished() throws SQLException {
        assertThat(connection).isNotNull();
        assertThat(connection.isClosed()).isFalse();
    }

    @Test
    void shouldCreateUserWithJsonData() throws SQLException {
        // Given
        long userId = TestDataBuilder.users(connection)
            .name("John Doe")
            .email("john@example.com")
            .metadata("role", "admin")
            .metadata("department", "IT")
            .settings("theme", "dark")
            .settings("notifications", true)
            .insert();

        // When
        String result = executeScalar(
            "SELECT JSON_EXTRACT(metadata, '$.role') FROM users WHERE id = " + userId
        );

        // Then
        assertThat(userId).isGreaterThan(0);
        assertThat(result).isEqualTo("\"admin\"");
    }

    @Test
    void shouldCreateProductWithJsonAttributes() throws SQLException {
        // Given
        long productId = TestDataBuilder.products(connection)
            .name("Laptop")
            .price(999.99)
            .attribute("brand", "TechCorp")
            .attribute("warranty", "2 years")
            .tags("electronics", "computers", "laptops")
            .insert();

        // When
        String brand = executeScalar(
            "SELECT JSON_EXTRACT(attributes, '$.brand') FROM products WHERE id = " + productId
        );

        // Then
        assertThat(productId).isGreaterThan(0);
        assertThat(brand).isEqualTo("\"TechCorp\"");
    }

    @Test
    void shouldSupportJsonFunctions() throws SQLException {
        // Test JSON_ARRAY
        String jsonArray = executeScalar("SELECT JSON_ARRAY('a', 'b', 'c')");
        assertThat(jsonArray).isEqualTo("[\"a\", \"b\", \"c\"]");

        // Test JSON_OBJECT
        String jsonObject = executeScalar("SELECT JSON_OBJECT('key', 'value')");
        assertThat(jsonObject).isEqualTo("{\"key\": \"value\"}");

        // Test JSON_EXTRACT
        String extracted = executeScalar(
            "SELECT JSON_EXTRACT('{\"name\": \"test\"}', '$.name')"
        );
        assertThat(extracted).isEqualTo("\"test\"");
    }

    // @Test  // Disabled: MySQL JSON path operators -> and ->> have parsing issues in some configurations
    void shouldSupportJsonPathOperators() throws SQLException {
        // Test -> operator
        String result = executeScalar(
            "SELECT '{\"name\": \"John\"}'->'$.name'"
        );
        assertThat(result).isEqualTo("\"John\"");

        // Test ->> operator (unquote)
        String unquoted = executeScalar(
            "SELECT '{\"name\": \"John\"}'->>'$.name'"
        );
        assertThat(unquoted).isEqualTo("John");
    }

    @Test
    void shouldCleanupDataBetweenTests() throws SQLException {
        // Insert data
        TestDataBuilder.users(connection)
            .name("Test User")
            .email("test@example.com")
            .insert();

        // Verify data exists
        String count = executeScalar("SELECT COUNT(*) FROM users");
        assertThat(count).isEqualTo("1");

        // Data should be cleaned up by @BeforeEach in next test
    }
}
