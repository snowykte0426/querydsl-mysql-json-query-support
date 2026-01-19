package io.github.snowykte0426.querydsl.mysql.json.jpa;

import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.QUser;
import io.github.snowykte0426.querydsl.mysql.json.jpa.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JSON_CONTAINS convenience methods (auto-escaping).
 *
 * <p>
 * Tests the new jsonContainsString, jsonContainsNumber, and jsonContainsBoolean
 * methods that automatically escape values as JSON literals.
 */
class JPAJsonContainsConvenienceTest extends AbstractJPAJsonFunctionTest {

    private static final QUser user = QUser.user;

    // ========================================
    // jsonContainsString tests
    // ========================================

    @Test
    void jsonContainsString_basicString_shouldWork() {
        // Given: User with roles array containing "admin"
        User adminUser = new User("Alice", "alice@example.com");
        adminUser.setRoles("[\"admin\", \"user\"]");
        entityManager.persist(adminUser);

        User normalUser = new User("Bob", "bob@example.com");
        normalUser.setRoles("[\"user\"]");
        entityManager.persist(normalUser);

        entityManager.flush();
        entityManager.clear();

        // When: Query using jsonContainsString (no manual escaping needed)
        List<User> admins = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.roles, "admin"))
                .fetch();

        // Then: Only admin user should be returned
        assertEquals(1, admins.size());
        assertEquals("Alice", admins.get(0).getName());
    }

    @Test
    void jsonContainsString_withSpecialCharacters_shouldEscape() {
        // Given: User with scope containing special characters
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"scope\": \"student:read\"}");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@example.com");
        user2.setMetadata("{\"scope\": \"teacher:write\"}");
        entityManager.persist(user2);

        entityManager.flush();
        entityManager.clear();

        // When: Query for "student:read" using jsonContainsString with path
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.metadata, "student:read", "$.scope"))
                .fetch();

        // Then: Only user1 should be returned
        assertEquals(1, results.size());
        assertEquals("User1", results.get(0).getName());
    }

    @Test
    void jsonContainsString_withQuotes_shouldEscape() {
        // Given: User with value containing quotes
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"message\": \"He said \\\"Hi\\\"\"}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for value with quotes (auto-escaped)
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.metadata, "He said \"Hi\"", "$.message"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
        assertEquals("User1", results.get(0).getName());
    }

    @Test
    void jsonContainsString_notFound_shouldReturnEmpty() {
        // Given: User without admin role
        User normalUser = new User("Bob", "bob@example.com");
        normalUser.setRoles("[\"user\", \"guest\"]");
        entityManager.persist(normalUser);

        entityManager.flush();
        entityManager.clear();

        // When: Query for "admin"
        List<User> admins = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.roles, "admin"))
                .fetch();

        // Then: No results
        assertTrue(admins.isEmpty());
    }

    // ========================================
    // jsonContainsNumber tests
    // ========================================

    @Test
    void jsonContainsNumber_integer_shouldWork() {
        // Given: User with age in metadata
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"age\": 25}");
        entityManager.persist(user1);

        User user2 = new User("User2", "user2@example.com");
        user2.setMetadata("{\"age\": 30}");
        entityManager.persist(user2);

        entityManager.flush();
        entityManager.clear();

        // When: Query for age 25
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsNumber(user.metadata, 25, "$.age"))
                .fetch();

        // Then: Only user1 should be returned
        assertEquals(1, results.size());
        assertEquals("User1", results.get(0).getName());
    }

    @Test
    void jsonContainsNumber_decimal_shouldWork() {
        // Given: User with price in metadata
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"price\": 99.99}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for price 99.99
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsNumber(user.metadata, 99.99, "$.price"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
        assertEquals("User1", results.get(0).getName());
    }

    @Test
    void jsonContainsNumber_inArray_shouldWork() {
        // Given: User with array of numbers
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"scores\": [85, 90, 95]}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for number in array
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsNumber(user.metadata, 90, "$.scores"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
        assertEquals("User1", results.get(0).getName());
    }

    // ========================================
    // jsonContainsBoolean tests
    // ========================================

    @Test
    void jsonContainsBoolean_true_shouldWork() {
        // Given: Users with active flag
        User activeUser = new User("ActiveUser", "active@example.com");
        activeUser.setSettings("{\"active\": true}");
        entityManager.persist(activeUser);

        User inactiveUser = new User("InactiveUser", "inactive@example.com");
        inactiveUser.setSettings("{\"active\": false}");
        entityManager.persist(inactiveUser);

        entityManager.flush();
        entityManager.clear();

        // When: Query for active=true
        List<User> activeUsers = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsBoolean(user.settings, true, "$.active"))
                .fetch();

        // Then: Only active user should be returned
        assertEquals(1, activeUsers.size());
        assertEquals("ActiveUser", activeUsers.get(0).getName());
    }

    @Test
    void jsonContainsBoolean_false_shouldWork() {
        // Given: Users with verified flag
        User verifiedUser = new User("VerifiedUser", "verified@example.com");
        verifiedUser.setSettings("{\"verified\": true}");
        entityManager.persist(verifiedUser);

        User unverifiedUser = new User("UnverifiedUser", "unverified@example.com");
        unverifiedUser.setSettings("{\"verified\": false}");
        entityManager.persist(unverifiedUser);

        entityManager.flush();
        entityManager.clear();

        // When: Query for verified=false
        List<User> unverifiedUsers = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsBoolean(user.settings, false, "$.verified"))
                .fetch();

        // Then: Only unverified user should be returned
        assertEquals(1, unverifiedUsers.size());
        assertEquals("UnverifiedUser", unverifiedUsers.get(0).getName());
    }

    // ========================================
    // Realistic scenario from user's bug report
    // ========================================

    @Test
    void realisticScenario_apiKeyScopes_shouldWork() {
        // Given: API keys with different scopes
        // This mimics the user's actual production scenario
        User apiKey1 = new User("API Key 1", "key1@example.com");
        apiKey1.setRoles("[\"student:read\", \"student:write\"]");
        entityManager.persist(apiKey1);

        User apiKey2 = new User("API Key 2", "key2@example.com");
        apiKey2.setRoles("[\"teacher:read\"]");
        entityManager.persist(apiKey2);

        entityManager.flush();
        entityManager.clear();

        // When: Query for "student:read" scope (user's actual use case)
        // BEFORE FIX: Would need to write:
        //   JPAJsonFunctions.jsonContains(user.roles, "\"student:read\"")
        // AFTER FIX: Can write:
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.roles, "student:read"))
                .fetch();

        // Then: Only apiKey1 should be returned
        assertEquals(1, results.size());
        assertEquals("API Key 1", results.get(0).getName());
    }

    // ========================================
    // Edge cases
    // ========================================

    @Test
    void jsonContainsString_emptyString_shouldWork() {
        // Given: User with empty string value
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"value\": \"\"}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for empty string
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.metadata, "", "$.value"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
    }

    @Test
    void jsonContainsNumber_zero_shouldWork() {
        // Given: User with zero value
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"count\": 0}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for zero
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsNumber(user.metadata, 0, "$.count"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
    }

    @Test
    void jsonContainsString_withBackslashes_shouldEscape() {
        // Given: User with value containing backslashes
        User user1 = new User("User1", "user1@example.com");
        user1.setMetadata("{\"path\": \"C:\\\\Users\\\\test\"}");
        entityManager.persist(user1);

        entityManager.flush();
        entityManager.clear();

        // When: Query for path with backslashes
        List<User> results = queryFactory.selectFrom(user)
                .where(JPAJsonFunctions.jsonContainsString(user.metadata, "C:\\Users\\test", "$.path"))
                .fetch();

        // Then: User should be found
        assertEquals(1, results.size());
    }
}
