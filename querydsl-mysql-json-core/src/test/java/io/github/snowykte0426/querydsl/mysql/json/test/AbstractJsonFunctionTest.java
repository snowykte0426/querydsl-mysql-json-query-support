package io.github.snowykte0426.querydsl.mysql.json.test;

import com.querydsl.core.types.Expression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.*;
import java.time.Duration;
import java.util.List;

/**
 * Base test class for JSON function tests using Testcontainers.
 *
 * <p>
 * This class provides:
 * <ul>
 * <li>MySQL 8.0.33 container with JSON support</li>
 * <li>Database connection management</li>
 * <li>Test schema initialization</li>
 * <li>Common test utilities</li>
 * </ul>
 *
 * <p>
 * Subclasses should implement test methods using the provided
 * {@link #getConnection()} method.
 *
 * @author snowykte0426
 * @since 0.1.0-Dev.3
 */
@Testcontainers
public abstract class AbstractJsonFunctionTest {

    /**
     * MySQL container with version configured via system property (default:
     * 8.0.33).
     */
    @Container
    protected static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>(
            DockerImageName.parse(System.getProperty("test.mysql.image", "mysql:8.0.33"))).withDatabaseName("testdb")
            .withUsername("test").withPassword("test").withReuse(true)
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2)).withStartupTimeout(Duration.ofSeconds(120));

    protected static Connection connection;

    /**
     * QueryDSL configuration for MySQL with JSON support.
     */
    protected static Configuration querydslConfig;

    /**
     * SQL serializer for converting QueryDSL expressions to SQL strings.
     */
    protected static SQLSerializer serializer;

    /**
     * Starts the MySQL container and establishes connection.
     */
    @BeforeAll
    static void setUp() throws SQLException {
        MYSQL_CONTAINER.start();
        connection = DriverManager.getConnection(
                MYSQL_CONTAINER.getJdbcUrl() + "?connectTimeout=30000&socketTimeout=30000",
                MYSQL_CONTAINER.getUsername(),
                MYSQL_CONTAINER.getPassword());

        // Initialize QueryDSL configuration for SQL serialization
        // Create templates with literal rendering enabled
        var templates = MySQLTemplates.builder().printSchema().quote().newLineToSingleSpace().build();

        querydslConfig = new Configuration(templates);
        querydslConfig.setUseLiterals(true);
        serializer = new SQLSerializer(querydslConfig);

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
     * Initializes test database schema. Creates test tables with JSON columns for
     * testing.
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
     * @param sql
     *            the SQL query to execute
     * @return number of affected rows
     * @throws SQLException
     *             if query execution fails
     */
    protected int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    /**
     * Executes a SQL query and returns a single string result.
     *
     * @param sql
     *            the SQL query to execute
     * @return the result string, or null if no result
     * @throws SQLException
     *             if query execution fails
     */
    protected String executeScalar(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement(); var rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        }
    }

    /**
     * Executes a QueryDSL expression and returns a single string result. This
     * method converts the expression to SQL using the MySQL serializer and handles
     * parameter binding with PreparedStatement.
     *
     * @param expression
     *            the QueryDSL expression to execute
     * @return the result string, or null if no result
     * @throws SQLException
     *             if query execution fails
     */
    protected String executeScalar(Expression<?> expression) throws SQLException {
        SQLSerializer localSerializer = new SQLSerializer(querydslConfig);
        localSerializer.handle(expression);

        String sql = "SELECT " + localSerializer.toString();
        List<Object> constants = localSerializer.getConstants();

        // If no parameters, use simple statement
        if (constants.isEmpty()) {
            return executeScalar(sql);
        }

        // Use PreparedStatement for parameterized queries
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // Bind parameters
            for (int i = 0; i < constants.size(); i++) {
                stmt.setObject(i + 1, constants.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return null;
            }
        }
    }

    /**
     * Converts a QueryDSL expression to SQL string.
     *
     * @param expression
     *            the expression to convert
     * @return SQL string representation
     */
    protected String toSql(Expression<?> expression) {
        // Create a new serializer instance for each conversion to avoid state issues
        SQLSerializer newSerializer = new SQLSerializer(querydslConfig);
        newSerializer.handle(expression);
        return newSerializer.toString();
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
