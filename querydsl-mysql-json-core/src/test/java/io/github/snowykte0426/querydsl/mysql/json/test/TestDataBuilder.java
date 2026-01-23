package io.github.snowykte0426.querydsl.mysql.json.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for creating test data with JSON columns.
 *
 * <p>
 * This class provides fluent API for inserting test data into database tables
 * with JSON columns. It handles JSON serialization automatically.
 * </p>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * TestDataBuilder.users(connection).name("John Doe").email("john@example.com").metadata("role", "admin")
 *         .metadata("department", "IT").insert();
 * }</pre>
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
public class TestDataBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a builder for inserting user test data.
     *
     * @param connection
     *            the database connection
     * @return UserBuilder instance
     */
    public static @NotNull UserBuilder users(Connection connection) {
        return new UserBuilder(connection);
    }

    /**
     * Creates a builder for inserting product test data.
     *
     * @param connection
     *            the database connection
     * @return ProductBuilder instance
     */
    public static @NotNull ProductBuilder products(Connection connection) {
        return new ProductBuilder(connection);
    }

    /**
     * Creates a builder for inserting order test data.
     *
     * @param connection
     *            the database connection
     * @return OrderBuilder instance
     */
    public static @NotNull OrderBuilder orders(Connection connection) {
        return new OrderBuilder(connection);
    }

    /**
     * Builder for user test data.
     */
    public static class UserBuilder {
        private final Connection connection;
        private String name;
        private String email;
        private final Map<String, Object> metadata = new HashMap<>();
        private final Map<String, Object> settings = new HashMap<>();

        private UserBuilder(Connection connection) {
            this.connection = connection;
        }

        public @NotNull UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public @NotNull UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public @NotNull UserBuilder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public @NotNull UserBuilder metadataJson(@NotNull Map<String, Object> json) {
            this.metadata.putAll(json);
            return this;
        }

        public @NotNull UserBuilder settings(String key, Object value) {
            this.settings.put(key, value);
            return this;
        }

        public @NotNull UserBuilder settingsJson(@NotNull Map<String, Object> json) {
            this.settings.putAll(json);
            return this;
        }

        /**
         * Inserts the user data into the database.
         *
         * @return the generated user ID
         * @throws SQLException
         *             if insertion fails
         */
        public long insert() throws SQLException {
            @NotNull String sql = "INSERT INTO users (name, email, metadata, settings) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, toJson(metadata));
                stmt.setString(4, toJson(settings));
                stmt.executeUpdate();

                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated key");
            }
        }
    }

    /**
     * Builder for product test data.
     */
    public static class ProductBuilder {
        private final Connection connection;
        private String name;
        private double price;
        private final Map<String, Object> attributes = new HashMap<>();
        private final List<String> tags = new ArrayList<>();

        private ProductBuilder(Connection connection) {
            this.connection = connection;
        }

        public @NotNull ProductBuilder name(String name) {
            this.name = name;
            return this;
        }

        public @NotNull ProductBuilder price(double price) {
            this.price = price;
            return this;
        }

        public @NotNull ProductBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public @NotNull ProductBuilder attributesJson(@NotNull Map<String, Object> json) {
            this.attributes.putAll(json);
            return this;
        }

        public @NotNull ProductBuilder tag(String tag) {
            this.tags.add(tag);
            return this;
        }

        public @NotNull ProductBuilder tags(String... tags) {
            this.tags.addAll(List.of(tags));
            return this;
        }

        /**
         * Inserts the product data into the database.
         *
         * @return the generated product ID
         * @throws SQLException
         *             if insertion fails
         */
        public long insert() throws SQLException {
            @NotNull String sql = "INSERT INTO products (name, price, attributes, tags) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setString(3, toJson(attributes));
                stmt.setString(4, toJson(tags));
                stmt.executeUpdate();

                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated key");
            }
        }
    }

    /**
     * Builder for order test data.
     */
    public static class OrderBuilder {
        private final Connection connection;
        private long userId;
        private final Map<String, Object> orderData = new HashMap<>();
        private final Map<String, Object> shippingInfo = new HashMap<>();

        private OrderBuilder(Connection connection) {
            this.connection = connection;
        }

        public @NotNull OrderBuilder userId(long userId) {
            this.userId = userId;
            return this;
        }

        public @NotNull OrderBuilder orderData(String key, Object value) {
            this.orderData.put(key, value);
            return this;
        }

        public @NotNull OrderBuilder orderDataJson(@NotNull Map<String, Object> json) {
            this.orderData.putAll(json);
            return this;
        }

        public @NotNull OrderBuilder shippingInfo(String key, Object value) {
            this.shippingInfo.put(key, value);
            return this;
        }

        public @NotNull OrderBuilder shippingInfoJson(@NotNull Map<String, Object> json) {
            this.shippingInfo.putAll(json);
            return this;
        }

        /**
         * Inserts the order data into the database.
         *
         * @return the generated order ID
         * @throws SQLException
         *             if insertion fails
         */
        public long insert() throws SQLException {
            @NotNull String sql = "INSERT INTO orders (user_id, order_data, shipping_info) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, userId);
                stmt.setString(2, toJson(orderData));
                stmt.setString(3, toJson(shippingInfo));
                stmt.executeUpdate();

                var rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("Failed to get generated key");
            }
        }
    }

    /**
     * Converts an object to JSON string.
     *
     * @param obj
     *            the object to convert
     * @return JSON string
     */
    private static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }
    }

    /**
     * Creates a JSON object from key-value pairs.
     *
     * @param keyValuePairs
     *            alternating keys and values
     * @return Map representing JSON object
     */
    public static @NotNull Map<String, Object> json(Object @NotNull ... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Key-value pairs must be even");
        }
        @NotNull Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
        }
        return map;
    }

    /**
     * Creates a JSON array from values.
     *
     * @param values
     *            the array values
     * @return List representing JSON array
     */
    public static @NotNull List<Object> jsonArray(Object... values) {
        return List.of(values);
    }
}
