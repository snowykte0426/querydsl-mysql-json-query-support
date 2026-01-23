package io.github.snowykte0426.querydsl.mysql.json.jpa.hibernate;

import io.github.snowykte0426.querydsl.mysql.json.jpa.AbstractJPAJsonFunctionTest;
import io.github.snowykte0426.querydsl.mysql.json.jpa.JPAJsonFunctions;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.QUser;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import org.hibernate.boot.model.FunctionContributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for verifying that Hibernate's FunctionContributor SPI properly
 * registers MySQL JSON functions.
 * <p>
 * This test class verifies:
 * <ul>
 * <li>MySQLJsonFunctionContributor is discovered via ServiceLoader</li>
 * <li>All boolean JSON functions work in WHERE clauses without errors</li>
 * <li>QueryDSL queries using JSON functions execute successfully</li>
 * <li>No "Non-boolean expression used in predicate context" errors occur</li>
 * </ul>
 */
@DisplayName("Hibernate FunctionContributor Registration Tests")
class HibernateFunctionRegistrationTest extends AbstractJPAJsonFunctionTest {

    @Test
    @DisplayName("MySQLJsonFunctionContributor should be discoverable via ServiceLoader")
    void functionContributor_shouldBeDiscoverable() {
        @NotNull ServiceLoader<FunctionContributor> loader = ServiceLoader.load(FunctionContributor.class);

        boolean found = false;
        for (FunctionContributor contributor : loader) {
            if (contributor instanceof MySQLJsonFunctionContributor) {
                found = true;
                break;
            }
        }

        assertThat(found).as("MySQLJsonFunctionContributor should be discoverable via SPI").isTrue();
    }

    @Test
    @DisplayName("json_contains should work in QueryDSL WHERE clause")
    void jsonContains_inWhereClause_shouldWork() {
        // Given: User with roles JSON array
        User user = createUser("Alice", "alice@example.com", "{}", "{}", "[\"admin\", \"user\"]");

        // When: Query using json_contains in WHERE clause
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")).fetch();

            // Then: Should not throw "Non-boolean expression" error and should find the
            // user
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(user.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_contains should filter results correctly")
    void jsonContains_shouldFilterCorrectly() {
        // Given: Multiple users with different roles
        User admin = createUser("Admin", "admin@example.com", "{}", "{}", "[\"admin\", \"superuser\", \"user\"]");
        User regularUser = createUser("User", "user@example.com", "{}", "{}", "[\"user\"]");
        User guest = createUser("Guest", "guest@example.com", "{}", "{}", "[\"guest\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Query for users with "admin" role
        var adminUsers = queryFactory.selectFrom(QUser.user)
                .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")).fetch();

        // Then: Should only return the admin user
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getName()).isEqualTo("Admin");

        // When: Query for users with "user" role
        var normalUsers = queryFactory.selectFrom(QUser.user)
                .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"user\"")).fetch();

        // Then: Should return both admin and regular user
        assertThat(normalUsers).hasSize(2);
        assertThat(normalUsers).extracting(User::getName).containsExactlyInAnyOrder("Admin", "User");
    }

    @Test
    @DisplayName("json_valid should work in QueryDSL WHERE clause")
    void jsonValid_inWhereClause_shouldWork() {
        // Given: Users with valid and invalid JSON metadata
        User validUser = createUser("Valid", "valid@example.com", "{\"valid\": true}");

        // When: Query using json_valid in WHERE clause
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user).where(JPAJsonFunctions.jsonValid(QUser.user.metadata))
                    .fetch();

            // Then: Should not throw error and should find users with valid JSON
            assertThat(results).isNotEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_contains_path should work in QueryDSL WHERE clause")
    void jsonContainsPath_inWhereClause_shouldWork() {
        // Given: User with nested metadata
        User user = createUser("John", "john@example.com", "{\"profile\": {\"age\": 30, \"city\": \"NYC\"}}");

        // When: Query using json_contains_path in WHERE clause
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonContainsPath(QUser.user.metadata, "one", "$.profile.age")).fetch();

            // Then: Should not throw error and should find the user
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(user.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_overlaps should work in QueryDSL WHERE clause")
    void jsonOverlaps_inWhereClause_shouldWork() {
        // Given: User with roles
        User user = createUser("Bob", "bob@example.com", "{}", "{}", "[\"user\", \"moderator\"]");

        // When: Query using json_overlaps in WHERE clause
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonOverlaps(QUser.user.roles, "[\"moderator\", \"admin\"]")).fetch();

            // Then: Should not throw error and should find the user
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(user.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Complex json_contains query should work without errors")
    void complexJsonContainsQuery_shouldWork() {
        // Given: Multiple users with complex role structures
        User superAdmin = createUser("SuperAdmin",
                "super@example.com",
                "{}",
                "{}",
                "[\"admin\", \"superuser\", \"developer\"]");

        User developer = createUser("Developer", "dev@example.com", "{}", "{}", "[\"developer\", \"user\"]");

        User basicUser = createUser("Basic", "basic@example.com", "{}", "{}", "[\"user\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Complex query with multiple json_contains conditions
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")
                            .or(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"developer\"")))
                    .orderBy(QUser.user.name.asc()).fetch();

            // Then: Should not throw error and should find correct users
            assertThat(results).hasSize(2);
            assertThat(results).extracting(User::getName).containsExactly("Developer", "SuperAdmin");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_contains in complex predicate should work")
    void jsonContains_inComplexPredicate_shouldWork() {
        // Given: Users with roles and metadata
        User activeAdmin = createUser("ActiveAdmin",
                "active@example.com",
                "{\"status\": \"active\"}",
                "{}",
                "[\"admin\"]");

        User inactiveAdmin = createUser("InactiveAdmin",
                "inactive@example.com",
                "{\"status\": \"inactive\"}",
                "{}",
                "[\"admin\"]");

        User activeUser = createUser("ActiveUser",
                "activeuser@example.com",
                "{\"status\": \"active\"}",
                "{}",
                "[\"user\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Query combining json_contains with json_extract
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")
                            .and(JPAJsonFunctions.jsonExtract(QUser.user.metadata, "$.status").eq("\"active\"")))
                    .fetch();

            // Then: Should not throw error and should find only active admin
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("ActiveAdmin");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Multiple boolean JSON functions in single query should work")
    void multipleBooleanFunctions_inSingleQuery_shouldWork() {
        // Given: User with complete data
        User user = createUser("Complete",
                "complete@example.com",
                "{\"verified\": true, \"profile\": {\"complete\": true}}",
                "{}",
                "[\"user\", \"verified\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Query using multiple boolean JSON functions
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonValid(QUser.user.metadata)
                            .and(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"verified\""))
                            .and(JPAJsonFunctions.jsonContainsPath(QUser.user.metadata, "one", "$.profile.complete")))
                    .fetch();

            // Then: Should not throw error and should find the user
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(user.getId());
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_contains with negation should work")
    void jsonContains_withNegation_shouldWork() {
        // Given: Users with different roles
        User admin = createUser("Admin", "admin@test.com", "{}", "{}", "[\"admin\"]");
        User user = createUser("User", "user@test.com", "{}", "{}", "[\"user\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Query for users WITHOUT admin role
        assertThatCode(() -> {
            var results = queryFactory.selectFrom(QUser.user)
                    .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"").not()).fetch();

            // Then: Should not throw error and should find only regular user
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("User");
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("json_contains should work with subquery")
    void jsonContains_withSubquery_shouldWork() {
        // Given: Users with various roles
        createUser("Admin1", "admin1@test.com", "{}", "{}", "[\"admin\"]");
        createUser("Admin2", "admin2@test.com", "{}", "{}", "[\"admin\"]");
        createUser("User1", "user1@test.com", "{}", "{}", "[\"user\"]");

        entityManager.flush();
        entityManager.clear();

        // When: Count query with json_contains
        assertThatCode(() -> {
            @Nullable Long count = queryFactory.select(QUser.user.count()).from(QUser.user)
                    .where(JPAJsonFunctions.jsonContains(QUser.user.roles, "\"admin\"")).fetchOne();

            // Then: Should not throw error and should return correct count
            assertThat(count).isEqualTo(2L);
        }).doesNotThrowAnyException();
    }
}
