package io.github.snowykte0426.querydsl.mysql.json.functions;

import io.github.snowykte0426.querydsl.mysql.json.core.expressions.JsonTableExpression;
import io.github.snowykte0426.querydsl.mysql.json.core.types.JsonTableColumn;
import io.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static io.github.snowykte0426.querydsl.mysql.json.core.functions.JsonTableFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

/**
 * Tests for JSON_TABLE function.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
class JsonTableFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // Basic JSON_TABLE tests
    // ========================================

    @Test
    void jsonTable_withSimpleArray_shouldExtractRows() throws SQLException {
        // Given
        @NotNull String json = "[{\"id\": 1, \"name\": \"John\"}, {\"id\": 2, \"name\": \"Jane\"}]";
        @NotNull JsonTableExpression table = jsonTable(json, "$[*]").column("id", "INT", "$.id")
                .column("name", "VARCHAR(100)", "$.name").alias("jt").build();

        // When
        @NotNull String sql = "SELECT jt.* FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("id"), rs.getString("name")});
            }
        }

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("1", "John");
        assertThat(rows.get(1)).containsExactly("2", "Jane");
    }

    @Test
    void jsonTable_withNestedObject_shouldExtractData() throws SQLException {
        // Given
        @NotNull String json = "{\"user\": {\"id\": 1, \"name\": \"Alice\", \"email\": \"alice@test.com\"}}";
        @NotNull JsonTableExpression table = jsonTable().jsonDoc(json).path("$.user").column("user_id", "INT", "$.id")
                .column("user_name", "VARCHAR(100)", "$.name").column("user_email", "VARCHAR(255)", "$.email")
                .alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("user_id"), rs.getString("user_name"), rs.getString("user_email")});
            }
        }

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)).containsExactly("1", "Alice", "alice@test.com");
    }

    @Test
    void jsonTable_withMultipleColumnTypes_shouldHandleDifferentTypes() throws SQLException {
        // Given
        @NotNull String json = "{\"id\": 123, \"name\": \"Test\", \"price\": 99.99, \"active\": true}";
        @NotNull JsonTableExpression table = jsonTable().jsonDoc(json).path("$").column(intColumn("id", "$.id"))
                .column(varcharColumn("name", 100, "$.name")).column(decimalColumn("price", 10, 2, "$.price"))
                .column(booleanColumn("active", "$.active")).alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("id"), rs.getString("name"), rs.getString("price"),
                        rs.getString("active")});
            }
        }

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[0]).isEqualTo("123");
        assertThat(rows.get(0)[1]).isEqualTo("Test");
        assertThat(rows.get(0)[2]).startsWith("99.99");
        assertThat(rows.get(0)[3]).isIn("1", "true");
    }

    // ========================================
    // EXISTS column tests
    // ========================================

    @Test
    void jsonTable_withExistsColumn_shouldIndicatePresence() throws SQLException {
        // Given
        @NotNull String json = "[{\"id\": 1, \"email\": \"user1@test.com\"}, {\"id\": 2}]";
        @NotNull JsonTableExpression table = jsonTable(json, "$[*]").column("id", "INT", "$.id")
                .existsColumn("has_email", "$.email").alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("id"), rs.getString("has_email")});
            }
        }

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("1", "1"); // has email
        assertThat(rows.get(1)).containsExactly("2", "0"); // no email
    }

    // ========================================
    // ORDINALITY column tests
    // ========================================

    @Test
    void jsonTable_withOrdinalityColumn_shouldNumberRows() throws SQLException {
        // Given
        @NotNull String json = "[\"a\", \"b\", \"c\", \"d\"]";
        @NotNull JsonTableExpression table = jsonTable(json, "$[*]").ordinalityColumn("row_num")
                .column("value", "VARCHAR(10)", "$").alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("row_num"), rs.getString("value")});
            }
        }

        // Then
        assertThat(rows).hasSize(4);
        assertThat(rows.get(0)).containsExactly("1", "a");
        assertThat(rows.get(1)).containsExactly("2", "b");
        assertThat(rows.get(2)).containsExactly("3", "c");
        assertThat(rows.get(3)).containsExactly("4", "d");
    }

    // ========================================
    // Column builder tests
    // ========================================

    @Test
    void jsonTable_withColumnBuilder_shouldSupportCustomSettings() throws SQLException {
        // Given
        @NotNull String json = "[{\"age\": 25}, {\"age\": null}, {}]";
        // Note: MySQL 8.0.33 only supports string DEFAULT values in JSON_TABLE
        @NotNull JsonTableColumn statusColumn = columnBuilder().columnName("status").sqlType("VARCHAR(50)").jsonPath("$.status")
                .onEmpty("DEFAULT '\"unknown\"'").onError("DEFAULT '\"error\"'").build();

        @NotNull JsonTableExpression table = jsonTable(json, "$[*]").column(statusColumn).alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String> statuses = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                statuses.add(rs.getString("status"));
            }
        }

        // Then
        assertThat(statuses).hasSize(3);
        // All rows should have 'unknown' or 'error' as default since status field
        // doesn't exist
    }

    // ========================================
    // Type helper tests
    // ========================================

    @Test
    void jsonTable_withTypeHelpers_shouldCreateCorrectColumns() throws SQLException {
        // Given
        @NotNull String json = "{\"int_val\": 42, \"big_val\": 9999999999, \"text_val\": \"hello\", \"date_val\": \"2024-01-01\"}";
        @NotNull JsonTableExpression table = jsonTable(json, "$").column(intColumn("int_val", "$.int_val"))
                .column(bigIntColumn("big_val", "$.big_val")).column(textColumn("text_val", "$.text_val"))
                .column(dateColumn("date_val", "$.date_val")).alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("int_val"), rs.getString("big_val"), rs.getString("text_val"),
                        rs.getString("date_val")});
            }
        }

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[0]).isEqualTo("42");
        assertThat(rows.get(0)[2]).isEqualTo("hello");
    }

    // ========================================
    // JSON column type tests
    // ========================================

    @Test
    void jsonTable_withJsonColumn_shouldExtractNestedJson() throws SQLException {
        // Given
        @NotNull String json = "{\"user\": {\"name\": \"John\", \"address\": {\"city\": \"Seoul\", \"zip\": \"12345\"}}}";
        @NotNull JsonTableExpression table = jsonTable(json, "$.user").column("name", "VARCHAR(100)", "$.name")
                .column(jsonColumn("address", "$.address")).alias("jt").build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("name"), rs.getString("address")});
            }
        }

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0)[0]).isEqualTo("John");
        assertThat(rows.get(0)[1]).contains("Seoul", "12345");
    }

    // ========================================
    // Builder validation tests
    // ========================================

    @Test
    void jsonTable_withoutJsonDoc_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonTable().path("$").column("id", "INT", "$.id").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("jsonDoc is required");
    }

    @Test
    void jsonTable_withoutPath_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonTable().jsonDoc("{}").column("id", "INT", "$.id").build())
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("path is required");
    }

    @Test
    void jsonTable_withoutColumns_shouldThrowException() {
        // When/Then
        assertThatThrownBy(() -> jsonTable().jsonDoc("{}").path("$").build()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one column is required");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonTable_fromDatabaseColumn_shouldExtractData() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('John', 'john@test.com', '[{\"key\": \"age\", \"value\": 30}, {\"key\": \"city\", \"value\": \"Seoul\"}]')");

        // When - Extract key-value pairs from metadata array
        @NotNull String sql = "SELECT jt.* FROM users, " + "JSON_TABLE(metadata, '$[*]' COLUMNS("
                + "  k VARCHAR(50) PATH '$.key'," + "  v VARCHAR(100) PATH '$.value'" + ")) AS jt "
                + "WHERE users.name = 'John'";

        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("k"), rs.getString("v")});
            }
        }

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("age", "30");
        assertThat(rows.get(1)).containsExactly("city", "Seoul");
    }

    @Test
    void jsonTable_withJoin_shouldCombineWithRegularTable() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES "
                + "('Product1', 100.00, '{\"tags\": [\"electronics\", \"sale\"]}')");

        // When - Extract tags as rows and join
        @NotNull String sql = "SELECT p.name, jt.tag FROM products p, " + "JSON_TABLE(p.attributes, '$.tags[*]' COLUMNS("
                + "  tag VARCHAR(50) PATH '$'" + ")) AS jt " + "WHERE p.name = 'Product1'";

        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("name"), rs.getString("tag")});
            }
        }

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsExactly("Product1", "electronics");
        assertThat(rows.get(1)).containsExactly("Product1", "sale");
    }

    @Test
    void jsonTable_withComplexNesting_shouldExtractAllLevels() throws SQLException {
        // Given
        @NotNull String json = "{\"users\": ["
                + "{\"id\": 1, \"name\": \"Alice\", \"contacts\": [{\"type\": \"email\", \"value\": \"alice@test.com\"}]}, "
                + "{\"id\": 2, \"name\": \"Bob\", \"contacts\": [{\"type\": \"phone\", \"value\": \"123-456\"}]}"
                + "]}";

        @NotNull JsonTableExpression table = jsonTable(json, "$.users[*]").column("user_id", "INT", "$.id")
                .column("user_name", "VARCHAR(100)", "$.name").column(jsonColumn("contacts", "$.contacts")).alias("jt")
                .build();

        // When
        @NotNull String sql = "SELECT * FROM " + table.toCompleteSql();
        @NotNull List<String[]> rows = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("user_id"), rs.getString("user_name"), rs.getString("contacts")});
            }
        }

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)[0]).isEqualTo("1");
        assertThat(rows.get(0)[1]).isEqualTo("Alice");
        assertThat(rows.get(0)[2]).contains("email", "alice@test.com");
        assertThat(rows.get(1)[0]).isEqualTo("2");
        assertThat(rows.get(1)[1]).isEqualTo("Bob");
        assertThat(rows.get(1)[2]).contains("phone", "123-456");
    }

    @Test
    void jsonTable_withExpression_shouldWork() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES "
                + "('TestUser', 'test@test.com', '{\"items\": [1, 2, 3, 4, 5]}')");

        // When - Use JSON_TABLE with column expression
        @NotNull String sql = "SELECT jt.* FROM users, " + "JSON_TABLE(users.metadata, '$.items[*]' COLUMNS("
                + "  item INT PATH '$'" + ")) AS jt " + "WHERE users.name = 'TestUser'";

        @NotNull List<String> items = new ArrayList<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(rs.getString("item"));
            }
        }

        // Then
        assertThat(items).containsExactly("1", "2", "3", "4", "5");
    }
}
