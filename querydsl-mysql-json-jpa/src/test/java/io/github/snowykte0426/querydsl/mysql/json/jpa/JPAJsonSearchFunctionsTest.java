package io.github.snowykte0426.querydsl.mysql.json.jpa;

import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.QUser;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for MySQL JSON search functions in JPA environment.
 *
 * <p>
 * Tests cover:
 * <ul>
 * <li>JSON_EXTRACT - Extract data from JSON</li>
 * <li>JSON_VALUE - Extract scalar values</li>
 * <li>JSON_CONTAINS - Test if JSON contains value</li>
 * <li>JSON_CONTAINS_PATH - Test if path exists</li>
 * <li>JSON_KEYS - Get object keys</li>
 * <li>JSON_SEARCH - Find path to value</li>
 * <li>JSON_OVERLAPS - Test document overlap</li>
 * <li>MEMBER OF - Test array membership</li>
 * </ul>
 */
@DisplayName("JPA JSON Search Functions")
class JPAJsonSearchFunctionsTest extends AbstractJPAJsonFunctionTest {

    @BeforeEach
    void setupData() {
        // Create test users with JSON metadata
        createUser("John",
                "john@example.com",
                "{\"role\": \"admin\", \"permissions\": [\"read\", \"write\", \"delete\"], \"profile\": {\"age\": 30}}",
                "{\"theme\": \"dark\", \"notifications\": true}",
                "[\"admin\", \"user\"]");

        createUser("Jane",
                "jane@example.com",
                "{\"role\": \"user\", \"permissions\": [\"read\"], \"profile\": {\"age\": 25}}",
                "{\"theme\": \"light\", \"notifications\": false}",
                "[\"user\"]");

        createUser("Bob",
                "bob@example.com",
                "{\"role\": \"moderator\", \"permissions\": [\"read\", \"write\"], \"profile\": {\"age\": 35}}",
                null,
                "[\"moderator\", \"user\"]");

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("JSON_EXTRACT")
    class JsonExtractTests {

        @Test
        @DisplayName("should extract simple value")
        void extractSimpleValue() {
            String sql = "SELECT JSON_EXTRACT('{\"name\": \"John\"}', '$.name')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("\"John\"");
        }

        @Test
        @DisplayName("should extract nested value")
        void extractNestedValue() {
            String sql = "SELECT JSON_EXTRACT('{\"user\": {\"name\": \"John\"}}', '$.user.name')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("\"John\"");
        }

        @Test
        @DisplayName("should extract array element")
        void extractArrayElement() {
            String sql = "SELECT JSON_EXTRACT('[1, 2, 3]', '$[1]')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("2");
        }

        @Test
        @DisplayName("should return null for non-existent path")
        void returnNullForNonExistentPath() {
            String sql = "SELECT JSON_EXTRACT('{\"name\": \"John\"}', '$.age')";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should extract from user metadata column")
        void extractFromUserMetadata() {
            String sql = "SELECT JSON_EXTRACT(metadata, '$.role') FROM users WHERE email = 'john@example.com'";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("\"admin\"");
        }
    }

    @Nested
    @DisplayName("JSON_UNQUOTE_EXTRACT (->>)")
    class JsonUnquoteExtractTests {

        @Test
        @DisplayName("should extract and unquote value")
        void extractAndUnquoteValue() {
            String sql = "SELECT JSON_UNQUOTE(JSON_EXTRACT('{\"name\": \"John\"}', '$.name'))";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("John");
        }

        @Test
        @DisplayName("should use ->> operator")
        void useArrowOperator() {
            String sql = "SELECT metadata->>'$.role' FROM users WHERE email = 'john@example.com'";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("admin");
        }
    }

    @Nested
    @DisplayName("JSON_CONTAINS")
    class JsonContainsTests {

        @Test
        @DisplayName("should return true when value is contained")
        void returnTrueWhenContained() {
            String sql = "SELECT JSON_CONTAINS('[1, 2, 3]', '2')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return false when value is not contained")
        void returnFalseWhenNotContained() {
            String sql = "SELECT JSON_CONTAINS('[1, 2, 3]', '5')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should check containment at path")
        void checkContainmentAtPath() {
            String sql = "SELECT JSON_CONTAINS('{\"a\": [1, 2, 3]}', '2', '$.a')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should find users with specific permission")
        void findUsersWithPermission() {
            String sql = "SELECT COUNT(*) FROM users WHERE JSON_CONTAINS(metadata, '\"write\"', '$.permissions')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).longValue()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("JSON_CONTAINS_PATH")
    class JsonContainsPathTests {

        @Test
        @DisplayName("should return true when path exists")
        void returnTrueWhenPathExists() {
            String sql = "SELECT JSON_CONTAINS_PATH('{\"a\": {\"b\": 1}}', 'one', '$.a.b')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return false when path does not exist")
        void returnFalseWhenPathNotExists() {
            String sql = "SELECT JSON_CONTAINS_PATH('{\"a\": 1}', 'one', '$.b')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should check 'all' paths")
        void checkAllPaths() {
            String sql = "SELECT JSON_CONTAINS_PATH('{\"a\": 1, \"b\": 2}', 'all', '$.a', '$.b')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should check 'one' path")
        void checkOnePath() {
            String sql = "SELECT JSON_CONTAINS_PATH('{\"a\": 1}', 'one', '$.a', '$.b')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("JSON_KEYS")
    class JsonKeysTests {

        @Test
        @DisplayName("should return keys from object")
        void returnKeysFromObject() {
            String sql = "SELECT JSON_KEYS('{\"a\": 1, \"b\": 2, \"c\": 3}')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"a\"", "\"b\"", "\"c\"");
        }

        @Test
        @DisplayName("should return keys at path")
        void returnKeysAtPath() {
            String sql = "SELECT JSON_KEYS('{\"outer\": {\"inner1\": 1, \"inner2\": 2}}', '$.outer')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("\"inner1\"", "\"inner2\"");
        }

        @Test
        @DisplayName("should return null for non-object")
        void returnNullForNonObject() {
            String sql = "SELECT JSON_KEYS('[1, 2, 3]')";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("JSON_SEARCH")
    class JsonSearchTests {

        @Test
        @DisplayName("should find path to value")
        void findPathToValue() {
            String sql = "SELECT JSON_SEARCH('[\"a\", \"b\", \"c\"]', 'one', 'b')";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("\"$[1]\"");
        }

        @Test
        @DisplayName("should find all paths")
        void findAllPaths() {
            String sql = "SELECT JSON_SEARCH('[\"a\", \"b\", \"a\"]', 'all', 'a')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("$[0]", "$[2]");
        }

        @Test
        @DisplayName("should support wildcards")
        void supportWildcards() {
            String sql = "SELECT JSON_SEARCH('[\"abc\", \"def\", \"axy\"]', 'all', 'a%')";
            String result = executeNativeQuery(sql);

            assertThat(result).contains("$[0]", "$[2]");
        }

        @Test
        @DisplayName("should return null when not found")
        void returnNullWhenNotFound() {
            String sql = "SELECT JSON_SEARCH('[\"a\", \"b\", \"c\"]', 'one', 'z')";
            Object result = entityManager.createNativeQuery(sql).getSingleResult();

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("JSON_OVERLAPS (MySQL 8.0.17+)")
    class JsonOverlapsTests {

        @Test
        @DisplayName("should return true when arrays overlap")
        void returnTrueWhenArraysOverlap() {
            String sql = "SELECT JSON_OVERLAPS('[1, 2, 3]', '[2, 4, 6]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return false when arrays do not overlap")
        void returnFalseWhenNoOverlap() {
            String sql = "SELECT JSON_OVERLAPS('[1, 2, 3]', '[4, 5, 6]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should work with objects")
        void workWithObjects() {
            String sql = "SELECT JSON_OVERLAPS('{\"a\": 1}', '{\"a\": 1, \"b\": 2}')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("MEMBER OF (MySQL 8.0.17+)")
    class MemberOfTests {

        @Test
        @DisplayName("should return true when value is member")
        void returnTrueWhenMember() {
            String sql = "SELECT 2 MEMBER OF('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return false when value is not member")
        void returnFalseWhenNotMember() {
            String sql = "SELECT 5 MEMBER OF('[1, 2, 3]')";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(0);
        }

        @Test
        @DisplayName("should work with string values")
        void workWithStrings() {
            // Use CAST to ensure JSON compatibility
            String sql = "SELECT CAST('\"admin\"' AS JSON) MEMBER OF(roles) FROM users WHERE email = 'john@example.com'";
            Object result = executeScalar(sql);

            assertThat(((Number) result).intValue()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("JPAJsonExpression Integration")
    class JPAJsonExpressionTests {

        @Test
        @DisplayName("should use JPAJsonExpression for fluent API")
        void useJPAJsonExpressionForFluentAPI() {
            // Test that JPAJsonExpression can wrap metadata column
            String sql = "SELECT JSON_EXTRACT(metadata, '$.profile.age') FROM users WHERE email = 'john@example.com'";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("30");
        }

        @Test
        @DisplayName("should chain multiple operations")
        void chainMultipleOperations() {
            // Test chaining: extract then check type
            String sql = "SELECT JSON_TYPE(JSON_EXTRACT(metadata, '$.permissions')) FROM users WHERE email = 'john@example.com'";
            String result = executeNativeQuery(sql);

            assertThat(result).isEqualTo("ARRAY");
        }
    }

    @Nested
    @DisplayName("QueryDSL Integration (Hibernate 6.4+ Support)")
    class QueryDSLIntegrationTests {

        @Test
        @DisplayName("jsonContains should work in QueryDSL WHERE clause without errors")
        void jsonContains_inQueryDSLWhereClause_shouldWork() {
            // When: Query using jsonContains in WHERE clause with QueryDSL
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")).fetch();

                // Then: Should not throw "Non-boolean expression" error
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getName()).isEqualTo("John");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonContains with path should work in QueryDSL WHERE clause")
        void jsonContainsWithPath_inQueryDSLWhereClause_shouldWork() {
            // When: Query using jsonContains with path in WHERE clause
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonContains(QUser.user.metadata, "\"write\"", "$.permissions"))
                        .fetch();

                // Then: Should not throw error and find users with write permission
                assertThat(results).hasSize(2);
                assertThat(results).extracting(User::getName).containsExactlyInAnyOrder("John", "Bob");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonContainsPath should work in QueryDSL WHERE clause")
        void jsonContainsPath_inQueryDSLWhereClause_shouldWork() {
            // When: Query using jsonContainsPath in WHERE clause
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonContainsPath(QUser.user.metadata, "one", "$.profile.age")).fetch();

                // Then: Should not throw error and find all users with profile.age
                assertThat(results).hasSize(3);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonOverlaps should work in QueryDSL WHERE clause")
        void jsonOverlaps_inQueryDSLWhereClause_shouldWork() {
            // When: Query using jsonOverlaps in WHERE clause
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonOverlaps(QUser.user.roles, "[\"admin\", \"superuser\"]")).fetch();

                // Then: Should not throw error and find users with overlapping roles
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getName()).isEqualTo("John");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonValid should work in QueryDSL WHERE clause")
        void jsonValid_inQueryDSLWhereClause_shouldWork() {
            // When: Query using jsonValid in WHERE clause
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonValid(QUser.user.metadata)).fetch();

                // Then: Should not throw error and find all users with valid JSON
                assertThat(results).hasSize(3);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Complex query with multiple JSON functions should work")
        void complexQueryWithMultipleJsonFunctions_shouldWork() {
            // When: Complex query combining multiple JSON functions
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")
                                .and(JPAJsonFunctions.jsonContainsPath(QUser.user.metadata, "one", "$.permissions"))
                                .and(JPAJsonFunctions.jsonExtract(QUser.user.metadata, "$.role").eq("\"admin\"")))
                        .fetch();

                // Then: Should not throw error and find matching users
                assertThat(results).hasSize(1);
                assertThat(results.get(0).getName()).isEqualTo("John");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonContains in ORDER BY should work")
        void jsonContains_inOrderBy_shouldWork() {
            // When: Query using jsonContains in ORDER BY
            assertThatCode(() -> {
                List<User> results = queryFactory.selectFrom(QUser.user)
                        .orderBy(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"").desc(),
                                QUser.user.name.asc())
                        .fetch();

                // Then: Should not throw error and order correctly
                assertThat(results).hasSize(3);
                assertThat(results.get(0).getName()).isEqualTo("John"); // has admin role
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonContains with COUNT should work")
        void jsonContains_withCount_shouldWork() {
            // When: COUNT query with jsonContains
            assertThatCode(() -> {
                Long count = queryFactory.select(QUser.user.count()).from(QUser.user)
                        .where(JPAJsonFunctions.jsonContains(QUser.user.metadata, "\"read\"", "$.permissions"))
                        .fetchOne();

                // Then: Should not throw error and return correct count
                assertThat(count).isEqualTo(3L);
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("jsonExtract with jsonContains should work together")
        void jsonExtractWithJsonContains_shouldWorkTogether() {
            // When: Query selecting json_extract result with jsonContains filter
            assertThatCode(() -> {
                List<String> roles = queryFactory.select(JPAJsonFunctions.jsonExtract(QUser.user.metadata, "$.role"))
                        .from(QUser.user)
                        .where(JPAJsonFunctions.jsonContains(QUser.user.metadata, "\"write\"", "$.permissions"))
                        .fetch();

                // Then: Should not throw error and return correct roles
                assertThat(roles).hasSize(2);
                assertThat(roles).containsExactlyInAnyOrder("\"admin\"", "\"moderator\"");
            }).doesNotThrowAnyException();
        }
    }
}
