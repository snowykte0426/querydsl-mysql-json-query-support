package io.github.snowykte0426.querydsl.mysql.json.sql;

import org.junit.jupiter.api.Test;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON_TABLE function in SQL module.
 */
class SqlJsonTableFunctionsTest extends AbstractSqlJsonFunctionTest {

    @Test
    void jsonTable_shouldConvertJsonToTable() throws SQLException {
        String sql = "SELECT * FROM JSON_TABLE(" +
            "'[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]', " +
            "'$[*]' COLUMNS(" +
            "id INT PATH '$.id', " +
            "name VARCHAR(50) PATH '$.name'" +
            ")) AS jt";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int count = 0;
            while (rs.next()) {
                count++;
                assertThat(rs.getInt("id")).isIn(1, 2);
                assertThat(rs.getString("name")).isIn("Alice", "Bob");
            }
            assertThat(count).isEqualTo(2);
        }
    }

    @Test
    void jsonTable_withNestedPath_shouldExtractNestedData() throws SQLException {
        String sql = "SELECT * FROM JSON_TABLE(" +
            "'{\"users\":[{\"id\":1,\"name\":\"Alice\"},{\"id\":2,\"name\":\"Bob\"}]}', " +
            "'$.users[*]' COLUMNS(" +
            "id INT PATH '$.id', " +
            "name VARCHAR(50) PATH '$.name'" +
            ")) AS jt";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    void jsonTable_withExistsColumn_shouldCheckPathExistence() throws SQLException {
        String sql = "SELECT * FROM JSON_TABLE(" +
            "'[{\"id\":1,\"email\":\"a@test.com\"},{\"id\":2}]', " +
            "'$[*]' COLUMNS(" +
            "id INT PATH '$.id', " +
            "has_email INT EXISTS PATH '$.email'" +
            ")) AS jt";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int withEmail = 0;
            int withoutEmail = 0;
            while (rs.next()) {
                if (rs.getInt("has_email") == 1) {
                    withEmail++;
                } else {
                    withoutEmail++;
                }
            }
            assertThat(withEmail).isEqualTo(1);
            assertThat(withoutEmail).isEqualTo(1);
        }
    }

    @Test
    void jsonTable_withOrdinalityColumn_shouldGenerateRowNumbers() throws SQLException {
        String sql = "SELECT * FROM JSON_TABLE(" +
            "'[{\"name\":\"Alice\"},{\"name\":\"Bob\"},{\"name\":\"Charlie\"}]', " +
            "'$[*]' COLUMNS(" +
            "row_number FOR ORDINALITY, " +
            "name VARCHAR(50) PATH '$.name'" +
            ")) AS jt";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            int expectedRow = 1;
            while (rs.next()) {
                assertThat(rs.getInt("row_number")).isEqualTo(expectedRow);
                expectedRow++;
            }
            assertThat(expectedRow).isEqualTo(4); // Should have 3 rows
        }
    }

    @Test
    void jsonTable_withMultipleTypes_shouldHandleDifferentTypes() throws SQLException {
        String sql = "SELECT * FROM JSON_TABLE(" +
            "'[{\"id\":1,\"active\":true,\"score\":95.5}]', " +
            "'$[*]' COLUMNS(" +
            "id INT PATH '$.id', " +
            "active BOOLEAN PATH '$.active', " +
            "score DECIMAL(5,2) PATH '$.score'" +
            ")) AS jt";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt("id")).isEqualTo(1);
            assertThat(rs.getBoolean("active")).isTrue();
            assertThat(rs.getBigDecimal("score")).isEqualByComparingTo("95.5");
        }
    }
}
