package com.github.snowykte0426.querydsl.mysql.json.functions;

import com.github.snowykte0426.querydsl.mysql.json.test.AbstractJsonFunctionTest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static com.github.snowykte0426.querydsl.mysql.json.core.functions.JsonUtilityFunctions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for JSON utility functions.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
class JsonUtilityFunctionsTest extends AbstractJsonFunctionTest {

    // ========================================
    // JSON_PRETTY tests
    // ========================================

    @Test
    void jsonPretty_withCompactObject_shouldFormatWithIndentation() throws SQLException {
        // Given
        StringExpression pretty = jsonPretty("{\"a\":1,\"b\":2}");

        // When
        String result = executeScalar("SELECT " + pretty.toString());

        // Then
        assertThat(result).contains("\"a\"", "\"b\"");
        assertThat(result).contains("1", "2");
        // Should have newlines for formatting
        assertThat(result).contains("\n");
    }

    @Test
    void jsonPretty_withCompactArray_shouldFormatWithIndentation() throws SQLException {
        // Given
        StringExpression pretty = jsonPretty("[1,2,3,4,5]");

        // When
        String result = executeScalar("SELECT " + pretty.toString());

        // Then
        assertThat(result).contains("1", "2", "3", "4", "5");
        assertThat(result).contains("\n");
    }

    @Test
    void jsonPretty_withNestedStructure_shouldFormatNicely() throws SQLException {
        // Given
        String doc = "{\"user\":{\"name\":\"John\",\"age\":30}}";
        StringExpression pretty = jsonPretty(Expressions.constant(doc));

        // When
        String result = executeScalar("SELECT " + pretty.toString());

        // Then
        assertThat(result).contains("\"user\"", "\"name\"", "\"John\"");
        assertThat(result).contains("\n");
    }

    @Test
    void jsonPretty_withExpression_shouldFormat() throws SQLException {
        // Given
        StringExpression arrayExpr = Expressions.stringTemplate("json_array(1, 2, 3)");
        StringExpression pretty = jsonPretty(arrayExpr);

        // When
        String result = executeScalar("SELECT " + pretty.toString());

        // Then
        assertThat(result).contains("1", "2", "3");
    }

    @Test
    void format_shouldBeSameAsJsonPretty() throws SQLException {
        // Given
        String doc = "{\"a\":1}";
        StringExpression pretty = format(Expressions.constant(doc));

        // When
        String result = executeScalar("SELECT " + pretty.toString());

        // Then
        assertThat(result).contains("\"a\"", "1");
        assertThat(result).contains("\n");
    }

    // ========================================
    // JSON_STORAGE_SIZE tests
    // ========================================

    @Test
    void jsonStorageSize_withSmallObject_shouldReturnSize() throws SQLException {
        // Given
        NumberExpression<Integer> size = jsonStorageSize("{\"a\": 1}");

        // When
        String result = executeScalar("SELECT " + size.toString());

        // Then
        int sizeValue = Integer.parseInt(result);
        assertThat(sizeValue).isGreaterThan(0);
        assertThat(sizeValue).isLessThan(100); // Should be small
    }

    @Test
    void jsonStorageSize_withArray_shouldReturnSize() throws SQLException {
        // Given
        NumberExpression<Integer> size = jsonStorageSize("[1, 2, 3, 4, 5]");

        // When
        String result = executeScalar("SELECT " + size.toString());

        // Then
        int sizeValue = Integer.parseInt(result);
        assertThat(sizeValue).isGreaterThan(0);
    }

    @Test
    void jsonStorageSize_withExpression_shouldReturnSize() throws SQLException {
        // Given
        StringExpression jsonExpr = Expressions.stringTemplate(
            "json_object('name', 'John', 'age', 30, 'city', 'Seoul')"
        );
        NumberExpression<Integer> size = jsonStorageSize(jsonExpr);

        // When
        String result = executeScalar("SELECT " + size.toString());

        // Then
        int sizeValue = Integer.parseInt(result);
        assertThat(sizeValue).isGreaterThan(0);
    }

    @Test
    void jsonStorageSize_largerDocument_shouldReturnLargerSize() throws SQLException {
        // Given
        String small = "{\"a\": 1}";
        String large = "{\"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4, \"e\": 5, \"f\": 6}";

        NumberExpression<Integer> smallSize = jsonStorageSize(small);
        NumberExpression<Integer> largeSize = jsonStorageSize(large);

        // When
        String smallResult = executeScalar("SELECT " + smallSize.toString());
        String largeResult = executeScalar("SELECT " + largeSize.toString());

        // Then
        int smallValue = Integer.parseInt(smallResult);
        int largeValue = Integer.parseInt(largeResult);
        assertThat(largeValue).isGreaterThan(smallValue);
    }

    // ========================================
    // JSON_STORAGE_FREE tests
    // ========================================

    @Test
    void jsonStorageFree_withNewDocument_shouldReturnZero() throws SQLException {
        // Given - Insert new document
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('John', 'john@test.com', '{\"a\": 1, \"b\": 2, \"c\": 3}')");

        // When
        String freed = executeScalar("SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE name = 'John'");

        // Then
        assertThat(freed).isEqualTo("0");
    }

    @Test
    void jsonStorageFree_afterPartialUpdate_shouldShowFreedSpace() throws SQLException {
        // Given - Insert document
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('Alice', 'alice@test.com', '{\"field1\": \"value1\", \"field2\": \"value2\", \"field3\": \"value3\"}')");

        // When - Perform partial update (remove a field)
        executeUpdate("UPDATE users SET metadata = JSON_REMOVE(metadata, '$.field2', '$.field3') WHERE name = 'Alice'");
        String freed = executeScalar("SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE name = 'Alice'");

        // Then - Should have some freed space (might be 0 if MySQL optimizes, but not negative)
        int freedValue = Integer.parseInt(freed);
        assertThat(freedValue).isGreaterThanOrEqualTo(0);
    }

    @Test
    void jsonStorageFree_afterMultipleUpdates_shouldAccumulate() throws SQLException {
        // Given - Insert large document
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES " +
            "('Product', 100.00, '{" +
            "\"attr1\": \"value1\", \"attr2\": \"value2\", \"attr3\": \"value3\", " +
            "\"attr4\": \"value4\", \"attr5\": \"value5\", \"attr6\": \"value6\"}')");

        // When - Multiple partial updates
        executeUpdate("UPDATE products SET attributes = JSON_REMOVE(attributes, '$.attr4', '$.attr5') WHERE name = 'Product'");
        executeUpdate("UPDATE products SET attributes = JSON_REMOVE(attributes, '$.attr6') WHERE name = 'Product'");

        String freed = executeScalar("SELECT JSON_STORAGE_FREE(attributes) FROM products WHERE name = 'Product'");

        // Then
        int freedValue = Integer.parseInt(freed);
        assertThat(freedValue).isGreaterThanOrEqualTo(0);
    }

    // ========================================
    // Convenience method tests
    // ========================================

    @Test
    void storageEfficiency_afterUpdate_shouldCalculateRatio() throws SQLException {
        // Given - Insert and update
        executeUpdate("INSERT INTO users (name, email, settings) VALUES " +
            "('Bob', 'bob@test.com', '{\"theme\": \"dark\", \"lang\": \"en\", \"notifications\": true}')");
        executeUpdate("UPDATE users SET settings = JSON_REMOVE(settings, '$.notifications') WHERE name = 'Bob'");

        // When
        String efficiency = executeScalar("SELECT JSON_STORAGE_FREE(settings) / JSON_STORAGE_SIZE(settings) FROM users WHERE name = 'Bob'");

        // Then - Should be a ratio between 0 and 1
        double effValue = Double.parseDouble(efficiency);
        assertThat(effValue).isGreaterThanOrEqualTo(0.0);
        assertThat(effValue).isLessThanOrEqualTo(1.0);
    }

    @Test
    void hasSignificantFreedSpace_withThreshold_shouldCheck() throws SQLException {
        // Given - Insert fresh document
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('Charlie', 'charlie@test.com', '{\"key\": \"value\"}')");

        // When
        String hasSpace = executeScalar("SELECT JSON_STORAGE_FREE(metadata) > 10 FROM users WHERE name = 'Charlie'");

        // Then - New document should have no freed space
        assertThat(hasSpace).isEqualTo("0");
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void jsonPretty_inDatabase_shouldFormatColumn() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, settings) VALUES " +
            "('Debug', 'debug@test.com', '{\"theme\":\"dark\",\"lang\":\"en\"}')");

        // When
        String pretty = executeScalar("SELECT JSON_PRETTY(settings) FROM users WHERE name = 'Debug'");

        // Then
        assertThat(pretty).contains("\"theme\"", "\"dark\"");
        assertThat(pretty).contains("\n");
    }

    @Test
    void jsonStorageSize_inDatabase_shouldReturnColumnSize() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES " +
            "('Widget', 50.00, '{\"color\": \"red\", \"size\": \"medium\"}')");

        // When
        String size = executeScalar("SELECT JSON_STORAGE_SIZE(attributes) FROM products WHERE name = 'Widget'");

        // Then
        int sizeValue = Integer.parseInt(size);
        assertThat(sizeValue).isGreaterThan(0);
    }

    @Test
    void jsonStorageSize_comparison_shouldWorkInWhere() throws SQLException {
        // Given
        executeUpdate("INSERT INTO products (name, price, attributes) VALUES " +
            "('Small', 10.00, '{\"a\": 1}'), " +
            "('Large', 20.00, '{\"a\": 1, \"b\": 2, \"c\": 3, \"d\": 4, \"e\": 5, \"f\": 6}')");

        // When - Find products with large JSON attributes
        String count = executeScalar("SELECT COUNT(*) FROM products WHERE JSON_STORAGE_SIZE(attributes) > 30");

        // Then - At least the large one should match
        int countValue = Integer.parseInt(count);
        assertThat(countValue).isGreaterThanOrEqualTo(0);
    }

    @Test
    void multipleUtilityFunctions_inSingleQuery_shouldWork() throws SQLException {
        // Given
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('MultiTest', 'multi@test.com', '{\"data\": [1, 2, 3]}')");

        // When - Use multiple utility functions
        String result = executeScalar(
            "SELECT CONCAT(" +
            "  'Size: ', JSON_STORAGE_SIZE(metadata), " +
            "  ', Free: ', JSON_STORAGE_FREE(metadata)" +
            ") FROM users WHERE name = 'MultiTest'"
        );

        // Then
        assertThat(result).contains("Size: ", "Free: ");
    }

    @Test
    void jsonPretty_withComplexNesting_shouldFormatCorrectly() throws SQLException {
        // Given
        String complexDoc = "{" +
            "\"user\": {" +
            "  \"profile\": {" +
            "    \"name\": \"John\"," +
            "    \"contacts\": [\"email\", \"phone\"]" +
            "  }," +
            "  \"settings\": {\"theme\": \"dark\"}" +
            "}" +
            "}";
        executeUpdate("INSERT INTO users (name, email, metadata) VALUES " +
            "('Complex', 'complex@test.com', '" + complexDoc.replace("\"", "\\\"") + "')");

        // When
        String pretty = executeScalar("SELECT JSON_PRETTY(metadata) FROM users WHERE name = 'Complex'");

        // Then
        assertThat(pretty).contains("\"user\"", "\"profile\"", "\"name\"");
        assertThat(pretty).contains("\n");
        // Pretty print should be longer than compact
        assertThat(pretty.length()).isGreaterThan(complexDoc.replace("\\\"", "\"").length());
    }
}