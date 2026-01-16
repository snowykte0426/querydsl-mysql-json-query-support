package io.github.snowykte0426.querydsl.mysql.json.jpa.spring;

import io.github.snowykte0426.querydsl.mysql.json.jpa.JPAJsonFunctions;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Spring Data JPA with QueryDSL JSON functions.
 *
 * <p>Tests demonstrate how to use JSON functions in a Spring Data JPA environment
 * with native queries and QueryDSL expressions.
 */
@Testcontainers
@DisplayName("Spring Data JPA Integration Tests")
class SpringDataJPAIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.33")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JPAQueryFactory queryFactory;

    @BeforeAll
    static void setupEntityManagerFactory() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", mysql.getJdbcUrl());
        properties.put("jakarta.persistence.jdbc.user", mysql.getUsername());
        properties.put("jakarta.persistence.jdbc.password", mysql.getPassword());
        properties.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.show_sql", "false");

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
        setupTestData();
    }

    @AfterEach
    void teardown() {
        if (entityManager != null && entityManager.isOpen()) {
            EntityTransaction tx = entityManager.getTransaction();
            if (tx.isActive()) {
                tx.rollback();
            }
            entityManager.close();
        }
    }

    private void setupTestData() {
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();

        // Clean up existing data to avoid unique constraint violations
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();

        // Admin user with full metadata
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setMetadata("{\"role\": \"admin\", \"level\": 10, \"permissions\": [\"read\", \"write\", \"delete\"]}");
        admin.setSettings("{\"theme\": \"dark\", \"notifications\": true}");
        admin.setRoles("[\"ROLE_ADMIN\", \"ROLE_USER\"]");
        entityManager.persist(admin);

        // Regular user
        User regular = new User();
        regular.setName("Regular User");
        regular.setEmail("user@example.com");
        regular.setMetadata("{\"role\": \"user\", \"level\": 1, \"permissions\": [\"read\"]}");
        regular.setSettings("{\"theme\": \"light\", \"notifications\": false}");
        regular.setRoles("[\"ROLE_USER\"]");
        entityManager.persist(regular);

        // Guest user with minimal metadata
        User guest = new User();
        guest.setName("Guest User");
        guest.setEmail("guest@example.com");
        guest.setMetadata("{\"role\": \"guest\"}");
        guest.setSettings("{\"theme\": \"light\"}");
        guest.setRoles("[\"ROLE_GUEST\"]");
        entityManager.persist(guest);

        tx.commit();
        entityManager.clear();
    }

    @Nested
    @DisplayName("Native Query JSON Functions")
    class NativeQueryJsonFunctionsTests {

        @Test
        @DisplayName("should find users by JSON_EXTRACT role")
        void findUsersByJsonExtractRole() {
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT name, JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) as role FROM users WHERE JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) = 'admin'"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)[0]).isEqualTo("Admin User");
            assertThat(results.get(0)[1]).isEqualTo("admin");
        }

        @Test
        @DisplayName("should find users by JSON_CONTAINS permission")
        void findUsersByJsonContainsPermission() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_CONTAINS(JSON_EXTRACT(metadata, '$.permissions'), '\"write\"')"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isEqualTo("Admin User");
        }

        @Test
        @DisplayName("should find users by JSON_CONTAINS_PATH")
        void findUsersByJsonContainsPath() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_CONTAINS_PATH(metadata, 'one', '$.level')"
            ).getResultList();

            // Admin and Regular have level, Guest does not
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("should find users by JSON_LENGTH")
        void findUsersByJsonLength() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_LENGTH(roles) >= 2"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isEqualTo("Admin User");
        }

        @Test
        @DisplayName("should find users by JSON_DEPTH")
        void findUsersByJsonDepth() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_DEPTH(metadata) >= 3"
            ).getResultList();

            // Admin and Regular have nested permissions array (depth 3)
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("should find users by theme setting")
        void findUsersByThemeSetting() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_UNQUOTE(JSON_EXTRACT(settings, '$.theme')) = 'dark'"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isEqualTo("Admin User");
        }

        @Test
        @DisplayName("should order users by JSON value")
        void orderUsersByJsonValue() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_CONTAINS_PATH(metadata, 'one', '$.level') ORDER BY JSON_EXTRACT(metadata, '$.level') DESC"
            ).getResultList();

            assertThat(results).hasSize(2);
            assertThat(results.get(0)).isEqualTo("Admin User");
            assertThat(results.get(1)).isEqualTo("Regular User");
        }
    }

    @Nested
    @DisplayName("Complex JSON Queries")
    class ComplexJsonQueriesTests {

        @Test
        @DisplayName("should combine multiple JSON conditions")
        void combineMultipleJsonConditions() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT email FROM users WHERE " +
                    "JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) = 'user' AND " +
                    "JSON_UNQUOTE(JSON_EXTRACT(settings, '$.theme')) = 'light'"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("should use JSON_SEARCH for value lookup")
        void useJsonSearchForValueLookup() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_SEARCH(roles, 'one', 'ROLE_ADMIN') IS NOT NULL"
            ).getResultList();

            assertThat(results).hasSize(1);
            assertThat(results.get(0)).isEqualTo("Admin User");
        }

        @Test
        @DisplayName("should validate JSON with JSON_VALID")
        void validateJsonWithJsonValid() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_VALID(metadata) = 1"
            ).getResultList();

            // All users should have valid JSON metadata
            assertThat(results).hasSize(3);
        }

        @Test
        @DisplayName("should check JSON type")
        void checkJsonType() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT name FROM users WHERE JSON_TYPE(roles) = 'ARRAY'"
            ).getResultList();

            // All users should have array type for roles
            assertThat(results).hasSize(3);
        }
    }

    @Nested
    @DisplayName("JSON Projection Queries")
    class JsonProjectionQueriesTests {

        @Test
        @DisplayName("should project JSON value extraction")
        void projectJsonValueExtraction() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) FROM users ORDER BY name"
            ).getResultList();

            assertThat(results).containsExactly("admin", "guest", "user");
        }

        @Test
        @DisplayName("should project JSON length")
        void projectJsonLength() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT JSON_LENGTH(roles) FROM users ORDER BY name"
            ).getResultList();

            assertThat(results).hasSize(3);
            assertThat(((Number) results.get(0)).intValue()).isEqualTo(2);  // Admin
            assertThat(((Number) results.get(1)).intValue()).isEqualTo(1);  // Guest
            assertThat(((Number) results.get(2)).intValue()).isEqualTo(1);  // Regular
        }

        @Test
        @DisplayName("should project JSON depth")
        void projectJsonDepth() {
            @SuppressWarnings("unchecked")
            List<Object> results = entityManager.createNativeQuery(
                    "SELECT JSON_DEPTH(metadata) FROM users ORDER BY name"
            ).getResultList();

            assertThat(results).hasSize(3);
            // Admin has depth 3 (permissions array), Guest has depth 2, Regular has depth 3
            assertThat(((Number) results.get(0)).intValue()).isEqualTo(3);  // Admin
            assertThat(((Number) results.get(1)).intValue()).isEqualTo(2);  // Guest
            assertThat(((Number) results.get(2)).intValue()).isEqualTo(3);  // Regular
        }

        @Test
        @DisplayName("should project JSON keys")
        void projectJsonKeys() {
            Object result = entityManager.createNativeQuery(
                    "SELECT JSON_KEYS(metadata) FROM users WHERE email = 'guest@example.com'"
            ).getSingleResult();

            assertThat(result.toString()).contains("role");
        }
    }

    @Nested
    @DisplayName("JSON Aggregate Functions")
    class JsonAggregateFunctionsTests {

        @Test
        @DisplayName("should use JSON_ARRAYAGG")
        void useJsonArrayAgg() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_ARRAYAGG(name) FROM users"
            ).getSingleResult();

            assertThat(result).contains("Admin User", "Regular User", "Guest User");
        }

        @Test
        @DisplayName("should use JSON_OBJECTAGG")
        void useJsonObjectAgg() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_OBJECTAGG(email, JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role'))) FROM users"
            ).getSingleResult();

            assertThat(result).contains("admin@example.com");
            assertThat(result).contains("admin");
        }

        @Test
        @DisplayName("should aggregate by category")
        void aggregateByCategory() {
            @SuppressWarnings("unchecked")
            List<Object[]> results = entityManager.createNativeQuery(
                    "SELECT JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) as role, " +
                    "JSON_ARRAYAGG(name) as names FROM users " +
                    "GROUP BY JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')) " +
                    "ORDER BY role"
            ).getResultList();

            assertThat(results).hasSize(3);  // admin, guest, user
        }
    }

    @Nested
    @DisplayName("JSON Modification Functions")
    class JsonModificationFunctionsTests {

        @Test
        @DisplayName("should use JSON_SET in select")
        void useJsonSetInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_SET(metadata, '$.newField', 'newValue') FROM users WHERE email = 'guest@example.com'"
            ).getSingleResult();

            assertThat(result).contains("newField");
            assertThat(result).contains("newValue");
        }

        @Test
        @DisplayName("should use JSON_INSERT in select")
        void useJsonInsertInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_INSERT(metadata, '$.inserted', 123) FROM users WHERE email = 'guest@example.com'"
            ).getSingleResult();

            assertThat(result).contains("inserted");
            assertThat(result).contains("123");
        }

        @Test
        @DisplayName("should use JSON_REMOVE in select")
        void useJsonRemoveInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_REMOVE(metadata, '$.role') FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(result).doesNotContain("\"role\"");
            assertThat(result).contains("level");
        }

        @Test
        @DisplayName("should use JSON_REPLACE in select")
        void useJsonReplaceInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_REPLACE(metadata, '$.role', 'superadmin') FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(result).contains("superadmin");
        }

        @Test
        @DisplayName("should use JSON_ARRAY_APPEND in select")
        void useJsonArrayAppendInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_ARRAY_APPEND(roles, '$', 'ROLE_NEW') FROM users WHERE email = 'guest@example.com'"
            ).getSingleResult();

            assertThat(result).contains("ROLE_NEW");
            assertThat(result).contains("ROLE_GUEST");
        }

        @Test
        @DisplayName("should use JSON_MERGE_PATCH in select")
        void useJsonMergePatchInSelect() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_MERGE_PATCH(settings, '{\"language\": \"en\"}') FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(result).contains("language");
            assertThat(result).contains("theme");
        }
    }

    @Nested
    @DisplayName("JSON Utility Functions")
    class JsonUtilityFunctionsTests {

        @Test
        @DisplayName("should use JSON_PRETTY")
        void useJsonPretty() {
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_PRETTY(metadata) FROM users WHERE email = 'guest@example.com'"
            ).getSingleResult();

            assertThat(result).contains("\n");
            assertThat(result).contains("role");
        }

        @Test
        @DisplayName("should use JSON_STORAGE_SIZE")
        void useJsonStorageSize() {
            Object result = entityManager.createNativeQuery(
                    "SELECT JSON_STORAGE_SIZE(metadata) FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(((Number) result).intValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should use JSON_STORAGE_FREE")
        void useJsonStorageFree() {
            Object result = entityManager.createNativeQuery(
                    "SELECT JSON_STORAGE_FREE(metadata) FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            // For non-updated columns, should return 0
            assertThat(((Number) result).intValue()).isGreaterThanOrEqualTo(0);
        }
    }

    @Nested
    @DisplayName("JSON Schema Validation (MySQL 8.0.17+)")
    class JsonSchemaValidationTests {

        @Test
        @DisplayName("should validate JSON against schema")
        void validateJsonAgainstSchema() {
            String schema = "'{\"type\": \"object\", \"properties\": {\"role\": {\"type\": \"string\"}}}'";
            Object result = entityManager.createNativeQuery(
                    "SELECT JSON_SCHEMA_VALID(" + schema + ", metadata) FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should get validation report")
        void getValidationReport() {
            String schema = "'{\"type\": \"object\"}'";
            String result = (String) entityManager.createNativeQuery(
                    "SELECT JSON_SCHEMA_VALIDATION_REPORT(" + schema + ", metadata) FROM users WHERE email = 'admin@example.com'"
            ).getSingleResult();

            assertThat(result).contains("valid");
            assertThat(result).contains("true");
        }
    }

    @Nested
    @DisplayName("JPAJsonFunctions Expression Tests")
    class JPAJsonFunctionsExpressionTests {

        @Test
        @DisplayName("should create JSON array expression")
        void createJsonArrayExpression() {
            var expr = JPAJsonFunctions.jsonArray(1, 2, 3);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON object expression")
        void createJsonObjectExpression() {
            var expr = JPAJsonFunctions.jsonObject("key", "value");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON quote expression")
        void createJsonQuoteExpression() {
            var expr = JPAJsonFunctions.jsonQuote("test string");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON extract expression")
        void createJsonExtractExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonExtract(path, "$.role");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON contains expression")
        void createJsonContainsExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonContains(path, "\"admin\"");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON depth expression")
        void createJsonDepthExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonDepth(path);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON length expression")
        void createJsonLengthExpression() {
            StringPath path = Expressions.stringPath("roles");
            var expr = JPAJsonFunctions.jsonLength(path);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON type expression")
        void createJsonTypeExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonType(path);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON valid expression")
        void createJsonValidExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonValid(path);
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON set expression")
        void createJsonSetExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonSet(path, "$.newKey", "newValue");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON remove expression")
        void createJsonRemoveExpression() {
            StringPath path = Expressions.stringPath("metadata");
            var expr = JPAJsonFunctions.jsonRemove(path, "$.key");
            assertThat(expr).isNotNull();
        }

        @Test
        @DisplayName("should create JSON table builder")
        void createJsonTableBuilder() {
            var builder = JPAJsonFunctions.jsonTable();
            assertThat(builder).isNotNull();
        }
    }
}
