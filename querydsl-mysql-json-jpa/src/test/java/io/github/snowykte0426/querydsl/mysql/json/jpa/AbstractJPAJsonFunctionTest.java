package io.github.snowykte0426.querydsl.mysql.json.jpa;

import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.Product;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.TestOrder;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base test class for JPA JSON function tests.
 *
 * <p>
 * This class provides common test infrastructure including:
 * <ul>
 * <li>MySQL container setup via Testcontainers</li>
 * <li>EntityManager and JPAQueryFactory configuration</li>
 * <li>Test data setup and cleanup</li>
 * <li>Helper methods for creating test entities</li>
 * </ul>
 *
 * <p>
 * Subclasses can focus on testing specific JSON functions without worrying
 * about infrastructure setup.
 */
@Testcontainers
public abstract class AbstractJPAJsonFunctionTest {

    @Container
    protected static final MySQLContainer<?> mysql = new MySQLContainer<>(
            DockerImageName.parse(System.getProperty("test.mysql.image", "mysql:8.0.33"))).withDatabaseName("json_test")
            .withUsername("test").withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci")
            .waitingFor(Wait.forLogMessage(".*ready for connections.*", 2)).withStartupTimeout(Duration.ofSeconds(120));

    protected static EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected JPAQueryFactory queryFactory;

    @BeforeAll
    static void setupEntityManagerFactory() {
        @NotNull
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url",
                mysql.getJdbcUrl() + "?connectTimeout=30000&socketTimeout=30000");
        properties.put("jakarta.persistence.jdbc.user", mysql.getUsername());
        properties.put("jakarta.persistence.jdbc.password", mysql.getPassword());
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        entityManagerFactory = Persistence.createEntityManagerFactory("test-pu", properties);
    }

    @AfterAll
    static void closeEntityManagerFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setup() {
        entityManager = entityManagerFactory.createEntityManager();
        queryFactory = new JPAQueryFactory(entityManager);
        entityManager.getTransaction().begin();
    }

    @AfterEach
    void cleanup() {
        if (entityManager != null) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
        }
    }

    /**
     * Creates and persists a test user.
     *
     * @param name
     *            the user name
     * @param email
     *            the user email
     * @param metadata
     *            the JSON metadata
     * @return the persisted user
     */
    protected @NotNull User createUser(String name, String email, String metadata) {
        @NotNull
        User user = new User(name, email, metadata);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    /**
     * Creates and persists a test user with settings and roles.
     *
     * @param name
     *            the user name
     * @param email
     *            the user email
     * @param metadata
     *            the JSON metadata
     * @param settings
     *            the JSON settings
     * @param roles
     *            the JSON roles array
     * @return the persisted user
     */
    protected @NotNull User createUser(String name, String email, String metadata, String settings, String roles) {
        @NotNull
        User user = new User(name, email, metadata);
        user.setSettings(settings);
        user.setRoles(roles);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    /**
     * Creates and persists a test product.
     *
     * @param name
     *            the product name
     * @param price
     *            the product price
     * @param category
     *            the product category
     * @param attributes
     *            the JSON attributes
     * @return the persisted product
     */
    protected @NotNull Product createProduct(String name, BigDecimal price, String category, String attributes) {
        @NotNull
        Product product = new Product(name, price, category);
        product.setAttributes(attributes);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }

    /**
     * Creates and persists a test product with tags.
     *
     * @param name
     *            the product name
     * @param price
     *            the product price
     * @param category
     *            the product category
     * @param attributes
     *            the JSON attributes
     * @param tags
     *            the JSON tags array
     * @return the persisted product
     */
    protected @NotNull Product createProduct(String name,
            BigDecimal price,
            String category,
            String attributes,
            String tags) {
        @NotNull
        Product product = new Product(name, price, category);
        product.setAttributes(attributes);
        product.setTags(tags);
        entityManager.persist(product);
        entityManager.flush();
        return product;
    }

    /**
     * Creates and persists a test order.
     *
     * @param orderNumber
     *            the order number
     * @param userId
     *            the user ID
     * @param totalAmount
     *            the total amount
     * @param orderData
     *            the JSON order data
     * @return the persisted order
     */
    protected @NotNull TestOrder createOrder(String orderNumber,
            Long userId,
            BigDecimal totalAmount,
            String orderData) {
        @NotNull
        TestOrder order = new TestOrder(orderNumber, userId, totalAmount);
        order.setOrderData(orderData);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    /**
     * Creates and persists a test order with shipping info.
     *
     * @param orderNumber
     *            the order number
     * @param userId
     *            the user ID
     * @param totalAmount
     *            the total amount
     * @param orderData
     *            the JSON order data
     * @param shippingInfo
     *            the JSON shipping info
     * @return the persisted order
     */
    protected @NotNull TestOrder createOrder(String orderNumber,
            Long userId,
            BigDecimal totalAmount,
            String orderData,
            String shippingInfo) {
        @NotNull
        TestOrder order = new TestOrder(orderNumber, userId, totalAmount);
        order.setOrderData(orderData);
        order.setShippingInfo(shippingInfo);
        entityManager.persist(order);
        entityManager.flush();
        return order;
    }

    /**
     * Sets up common test data for multiple tests. Override this method in
     * subclasses to add specific test data.
     */
    protected void setupTestData() {
        // Default implementation - override in subclasses
    }

    /**
     * Clears all test data from the database.
     */
    protected void clearTestData() {
        entityManager.createQuery("DELETE FROM TestOrder").executeUpdate();
        entityManager.createQuery("DELETE FROM Product").executeUpdate();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
    }

    /**
     * Executes a native SQL query and returns the result as a string. Useful for
     * testing JSON function output.
     *
     * @param sql
     *            the native SQL query
     * @return the query result as string
     */
    protected @Nullable String executeNativeQuery(String sql) {
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result != null ? result.toString() : null;
    }

    /**
     * Executes a native SQL query for scalar values.
     *
     * @param sql
     *            the native SQL query
     * @param <T>
     *            the result type
     * @return the scalar result
     */
    @SuppressWarnings("unchecked")
    protected <T> T executeScalar(String sql) {
        return (T) entityManager.createNativeQuery(sql).getSingleResult();
    }
}
