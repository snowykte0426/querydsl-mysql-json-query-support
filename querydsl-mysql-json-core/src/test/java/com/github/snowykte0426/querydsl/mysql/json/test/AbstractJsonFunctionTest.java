package com.github.snowykte0426.querydsl.mysql.json.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base test class for JSON function tests using Testcontainers.
 *
 * <p>This class provides:
 * <ul>
 *   <li>MySQL 8.0.33 container with JSON support</li>
 *   <li>Database connection management</li>
 *   <li>Test schema initialization</li>
 *   <li>Common test utilities</li>
 * </ul>
 *
 * <p>Subclasses should implement test methods using the provided {@link #getConnection()} method.
 *
 * @author snowykte0426
 * @since 1.0.0
 */
@Testcontainers
public abstract class AbstractJsonFunctionTest {

    /**
     * MySQL container with version 8.0.33 (supports all JSON functions).
     */
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(
        DockerImageName.parse("mysql:8.0.33")
    )
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withReuse(true);

    protected static Connection connection;

    /**
     * Starts the MySQL container and establishes connection.
     */
    @BeforeAll
    static void setUp() throws SQLException {
        MYSQL_CONTAINER.start();
        connection = DriverManager.getConnection(
            MYSQL_CONTAINER.getJdbcUrl(),
            MYSQL_CONTAINER.getUsername(),
            MYSQL_CONTAINER.getPassword()
        );
        initializeTestSchema();
    }

    /**
     * Closes the database connection after all tests.
     */
    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Initializes test database schema.
     * Creates test tables with JSON columns for testing.
     */
    private static void initializeTestSchema() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table with JSON metadata
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    metadata JSON,
                    settings JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Products table with JSON attributes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(200) NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    attributes JSON,
                    tags JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            // Orders table with JSON details
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    order_data JSON,
                    shipping_info JSON,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);
        }
    }

    /**
     * Cleans up test data before each test.
     */
    @BeforeEach
    void cleanupData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM users");
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM orders");
        }
    }

    /**
     * Returns the database connection for test queries.
     *
     * @return active database connection
     */
    protected Connection getConnection() {
        return connection;
    }

    /**
     * Executes a SQL query and returns result count.
     *
     * @param sql the SQL query to execute
     * @return number of affected rows
     * @throws SQLException if query execution fails
     */
    protected int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Executes a SQL query and returns a single string result.
     *
     * @param sql the SQL query to execute
     * @return the result string, or null if no result
     * @throws SQLException if query execution fails
     */
    protected String executeScalar(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    /**
     * Returns the MySQL JDBC URL for the test container.
     *
     * @return JDBC connection URL
     */
    protected String getJdbcUrl() {
        return MYSQL_CONTAINER.getJdbcUrl();
    }

    /**
     * Returns the database username.
     *
     * @return username
     */
    protected String getUsername() {
        return MYSQL_CONTAINER.getUsername();
    }

    /**
     * Returns the database password.
     *
     * @return password
     */
    protected String getPassword() {
        return MYSQL_CONTAINER.getPassword();
    }
}
