package io.github.snowykte0426.querydsl.mysql.json.sql;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQueryFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.time.Duration;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base test class for SQL JSON function tests.
 *
 * <p>This class provides common test infrastructure including:
 * <ul>
 *   <li>MySQL container setup via Testcontainers</li>
 *   <li>SQLQueryFactory configuration with MySQLJsonTemplates</li>
 *   <li>Test database schema initialization</li>
 *   <li>Helper methods for creating test data</li>
 *   <li>Automatic cleanup after each test</li>
 * </ul>
 *
 * <p>Subclasses can focus on testing specific JSON functions without
 * worrying about infrastructure setup.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
@Testcontainers
public abstract class AbstractSqlJsonFunctionTest {

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>(
        DockerImageName.parse(System.getProperty("test.mysql.image", "mysql:8.0.33"))
    )
        .withDatabaseName("json_test")
        .withUsername("test")
        .withPassword("test")
        .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
        .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2))
        .withStartupTimeout(Duration.ofSeconds(120));

    protected static Configuration configuration;
    protected static SQLQueryFactory queryFactory;
    protected static DataSource dataSource;
    protected Connection connection;

    @BeforeAll
    static void setupInfrastructure() throws SQLException {
        // Create configuration with MySQL JSON support
        configuration = new Configuration(MySQLJsonTemplates.DEFAULT);

        // Setup HikariCP DataSource
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysql.getJdbcUrl() + "?connectTimeout=30000&socketTimeout=30000");
        hikariConfig.setUsername(mysql.getUsername());
        hikariConfig.setPassword(mysql.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(hikariConfig);

        // Create SQLQueryFactory
        queryFactory = new SQLQueryFactory(configuration, dataSource);

        // Initialize test schema
        initializeTestSchema();
    }

    @AfterAll
    static void teardownInfrastructure() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    @BeforeEach
    void setupConnection() throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(true);
    }

    @AfterEach
    void cleanupConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            // Clean up test data
            clearTestData();
            connection.close();
        }
    }

    /**
     * Initializes the test database schema.
     * Creates users, products, and orders tables with JSON columns.
     */
    private static void initializeTestSchema() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Create users table with JSON columns
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    metadata JSON,
                    settings JSON,
                    roles JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            // Create products table with JSON columns
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(200) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    category VARCHAR(50),
                    attributes JSON,
                    tags JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

            // Create orders table with JSON columns
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    order_number VARCHAR(50) NOT NULL UNIQUE,
                    user_id BIGINT NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    order_data JSON,
                    shipping_info JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
        }
    }

    /**
     * Clears all test data from the database.
     */
    protected void clearTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM orders");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM users");
        }
    }

    /**
     * Creates and persists a test user with metadata.
     *
     * @param name the user name
     * @param email the user email
     * @param metadata the JSON metadata
     * @return the generated user ID
     * @throws SQLException if database error occurs
     */
    protected Long createUser(String name, String email, String metadata) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO users (name, email, metadata) VALUES ('%s', '%s', '%s')",
                name.replace("'", "''"),
                email.replace("'", "''"),
                metadata.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Creates and persists a test user with settings and roles.
     *
     * @param name the user name
     * @param email the user email
     * @param metadata the JSON metadata
     * @param settings the JSON settings
     * @param roles the JSON roles array
     * @return the generated user ID
     * @throws SQLException if database error occurs
     */
    protected Long createUser(String name, String email, String metadata, String settings, String roles) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO users (name, email, metadata, settings, roles) VALUES ('%s', '%s', '%s', '%s', '%s')",
                name.replace("'", "''"),
                email.replace("'", "''"),
                metadata.replace("'", "''"),
                settings.replace("'", "''"),
                roles.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Creates and persists a test product.
     *
     * @param name the product name
     * @param price the product price
     * @param category the product category
     * @param attributes the JSON attributes
     * @return the generated product ID
     * @throws SQLException if database error occurs
     */
    protected Long createProduct(String name, BigDecimal price, String category, String attributes) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO products (name, price, category, attributes) VALUES ('%s', %s, '%s', '%s')",
                name.replace("'", "''"),
                price.toString(),
                category.replace("'", "''"),
                attributes.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Creates and persists a test product with tags.
     *
     * @param name the product name
     * @param price the product price
     * @param category the product category
     * @param attributes the JSON attributes
     * @param tags the JSON tags array
     * @return the generated product ID
     * @throws SQLException if database error occurs
     */
    protected Long createProduct(String name, BigDecimal price, String category, String attributes, String tags) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO products (name, price, category, attributes, tags) VALUES ('%s', %s, '%s', '%s', '%s')",
                name.replace("'", "''"),
                price.toString(),
                category.replace("'", "''"),
                attributes.replace("'", "''"),
                tags.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Creates and persists a test order.
     *
     * @param orderNumber the order number
     * @param userId the user ID
     * @param totalAmount the total amount
     * @param orderData the JSON order data
     * @return the generated order ID
     * @throws SQLException if database error occurs
     */
    protected Long createOrder(String orderNumber, Long userId, BigDecimal totalAmount, String orderData) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO orders (order_number, user_id, total_amount, order_data) VALUES ('%s', %d, %s, '%s')",
                orderNumber.replace("'", "''"),
                userId,
                totalAmount.toString(),
                orderData.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Creates and persists a test order with shipping info.
     *
     * @param orderNumber the order number
     * @param userId the user ID
     * @param totalAmount the total amount
     * @param orderData the JSON order data
     * @param shippingInfo the JSON shipping info
     * @return the generated order ID
     * @throws SQLException if database error occurs
     */
    protected Long createOrder(String orderNumber, Long userId, BigDecimal totalAmount,
                              String orderData, String shippingInfo) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            String sql = String.format(
                "INSERT INTO orders (order_number, user_id, total_amount, order_data, shipping_info) VALUES ('%s', %d, %s, '%s', '%s')",
                orderNumber.replace("'", "''"),
                userId,
                totalAmount.toString(),
                orderData.replace("'", "''"),
                shippingInfo.replace("'", "''")
            );
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            var rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Failed to get generated key");
        }
    }

    /**
     * Executes a native SQL query and returns the result as a string.
     * Useful for testing JSON function output.
     *
     * @param sql the native SQL query
     * @return the query result as string
     * @throws SQLException if database error occurs
     */
    protected String executeNativeQuery(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    /**
     * Executes a native SQL query for scalar integer values.
     *
     * @param sql the native SQL query
     * @return the integer result
     * @throws SQLException if database error occurs
     */
    protected Integer executeScalarInt(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        }
    }

    /**
     * Executes a native SQL query for scalar boolean values.
     *
     * @param sql the native SQL query
     * @return the boolean result
     * @throws SQLException if database error occurs
     */
    protected Boolean executeScalarBoolean(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            return null;
        }
    }
}
