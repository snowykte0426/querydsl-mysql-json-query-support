package io.github.snowykte0426.querydsl.mysql.json.core.types;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Comprehensive tests for {@link JsonPath}.
 */
@DisplayName("JsonPath Tests")
class JsonPathTest {

    // ============ Valid Path Tests ============

    @ParameterizedTest
    @ValueSource(strings = {
            "$",                            // Root
            "$.user",                       // Simple member
            "$.user.name",                  // Nested member
            "$.a.b.c.d.e",                  // Deeply nested
            "$[0]",                         // Simple array
            "$[123]",                       // Large index
            "$[0][1][2]",                   // Nested arrays
            "$.users[0]",                   // Member then array
            "$[0].name",                    // Array then member
            "$.users[0].name",              // Mixed access
            "$.users[0].addresses[1].city", // Complex path
            "$[*]",                         // Array wildcard
            "$.*",                          // Object wildcard
            "$.settings.*",                 // Member then wildcard
            "$**.price",                    // Recursive descent
            "$.catalog.**.price",           // Recursive in path
            "$.a_b_c",                      // Underscores
            "$._private",                   // Leading underscore
            "$.userName",                   // CamelCase
            "$.user_name",                  // Snake case
            "$.user123",                    // Numbers in identifier
            "$.ABC",                        // Uppercase
    })
    @DisplayName("isValidPath should return true for valid paths")
    void isValidPath_withValidPath_shouldReturnTrue(String path) {
        assertThat(JsonPath.isValidPath(path)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "$",
            "$.user",
            "$.user.name",
            "$[0]",
            "$.users[0].name",
            "$[*]",
            "$.*",
            "$**.price"
    })
    @DisplayName("of() should create JsonPath for valid paths")
    void of_withValidPath_shouldCreateJsonPath(String path) {
        @NotNull
        JsonPath jsonPath = JsonPath.of(path);
        assertThat(jsonPath.getPath()).isEqualTo(path);
    }

    // ============ Invalid Path Tests ============

    @ParameterizedTest
    @ValueSource(strings = {
            "user",                         // Missing root
            "[0]",                          // Missing root with array
            "$.user.name.",                 // Trailing dot
            "$invalid",                     // No dot after root
            "$.123start",                   // Identifier starts with digit
            "$.user-name",                  // Hyphen not allowed
            "$.user name",                  // Space not allowed
            "$.user.name!",                 // Special character
            "$.",                           // Empty member
            "$..",                          // Double dot
            "$[]",                          // Empty array index
            "$[abc]",                       // Non-numeric index
            "$[-1]",                        // Negative index
            "$.user]",                      // Unmatched bracket
            "$.user[",                      // Unclosed bracket
            "$[0",                          // Missing closing bracket
            "$**",                          // Invalid recursive (no member)
            "$..**",                        // Invalid recursive (no member)
            "$.user.**",                    // Invalid recursive (no member)
            "$.user..name",                 // Double dot in path
            "$[*",                          // Unclosed wildcard
            "$.user[*",                     // Unclosed wildcard
    })
    @DisplayName("isValidPath should return false for invalid paths")
    void isValidPath_withInvalidPath_shouldReturnFalse(String path) {
        assertThat(JsonPath.isValidPath(path)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "$invalid",
            "$.123start",
            "$.user-name",
            "$.",
            "$[]"
    })
    @DisplayName("of() should throw IllegalArgumentException for invalid paths")
    void of_withInvalidPath_shouldThrowException(String path) {
        assertThatThrownBy(() -> JsonPath.of(path))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JSON path");
    }

    @Test
    @DisplayName("isValidPath should return false for null")
    void isValidPath_withNull_shouldReturnFalse() {
        assertThat(JsonPath.isValidPath(null)).isFalse();
    }

    @Test
    @DisplayName("isValidPath should return false for empty string")
    void isValidPath_withEmptyString_shouldReturnFalse() {
        assertThat(JsonPath.isValidPath("")).isFalse();
    }

    @Test
    @DisplayName("of() should throw NullPointerException for null")
    void of_withNull_shouldThrowException() {
        assertThatThrownBy(() -> JsonPath.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ============ ReDoS Resistance Tests ============

    @Test
    @DisplayName("isValidPath should resist ReDoS attack with repeated dots")
    void isValidPath_withReDoSAttackDots_shouldCompleteQuickly() {
        String attack = "$" + ".".repeat(1000) + "X";
        long start = System.nanoTime();
        boolean result = JsonPath.isValidPath(attack);
        long duration = System.nanoTime() - start;

        assertThat(result).isFalse();
        assertThat(duration).isLessThan(10_000_000); // Less than 10ms
    }

    @Test
    @DisplayName("isValidPath should resist ReDoS attack with repeated identifiers")
    void isValidPath_withReDoSAttackIdentifiers_shouldCompleteQuickly() {
        String attack = "$." + "a".repeat(1000) + "!";
        long start = System.nanoTime();
        boolean result = JsonPath.isValidPath(attack);
        long duration = System.nanoTime() - start;

        assertThat(result).isFalse();
        assertThat(duration).isLessThan(10_000_000); // Less than 10ms
    }

    @Test
    @DisplayName("isValidPath should resist ReDoS attack with repeated array indices")
    void isValidPath_withReDoSAttackArrays_shouldCompleteQuickly() {
        String attack = "$[" + "9".repeat(1000) + "!]";
        long start = System.nanoTime();
        boolean result = JsonPath.isValidPath(attack);
        long duration = System.nanoTime() - start;

        assertThat(result).isFalse();
        assertThat(duration).isLessThan(10_000_000); // Less than 10ms
    }

    // ============ Factory Method: member() ============

    @Test
    @DisplayName("member() should create path to member")
    void member_withValidKey_shouldCreateMemberPath() {
        @NotNull
        JsonPath path = JsonPath.member("user");
        assertThat(path.getPath()).isEqualTo("$.user");
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "userName", "user_name", "_private", "ABC", "user123"})
    @DisplayName("member() should accept valid identifiers")
    void member_withValidIdentifier_shouldSucceed(String key) {
        @NotNull
        JsonPath path = JsonPath.member(key);
        assertThat(path.getPath()).isEqualTo("$." + key);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123start", "user-name", "user name", "user.name", "user[0]", ""})
    @DisplayName("member() should reject invalid identifiers")
    void member_withInvalidIdentifier_shouldThrowException(String key) {
        assertThatThrownBy(() -> JsonPath.member(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid identifier");
    }

    @Test
    @DisplayName("member() should throw NullPointerException for null key")
    void member_withNullKey_shouldThrowException() {
        assertThatThrownBy(() -> JsonPath.member(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("key must not be null");
    }

    @Test
    @DisplayName("member() should provide helpful error message")
    void member_withInvalidKey_shouldProvideHelpfulMessage() {
        assertThatThrownBy(() -> JsonPath.member("123invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must start with letter/underscore")
                .hasMessageContaining("contain only alphanumeric/underscore");
    }

    // ============ Factory Method: arrayElement() ============

    @Test
    @DisplayName("arrayElement() should create path to array element")
    void arrayElement_withValidIndex_shouldCreateArrayPath() {
        @NotNull
        JsonPath path = JsonPath.arrayElement(0);
        assertThat(path.getPath()).isEqualTo("$[0]");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 10, 100, 999, 1000, Integer.MAX_VALUE})
    @DisplayName("arrayElement() should accept non-negative indices")
    void arrayElement_withNonNegativeIndex_shouldSucceed(int index) {
        @NotNull
        JsonPath path = JsonPath.arrayElement(index);
        assertThat(path.getPath()).isEqualTo("$[" + index + "]");
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -10, -100, Integer.MIN_VALUE})
    @DisplayName("arrayElement() should reject negative indices")
    void arrayElement_withNegativeIndex_shouldThrowException(int index) {
        assertThatThrownBy(() -> JsonPath.arrayElement(index))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Array index must be non-negative")
                .hasMessageContaining(String.valueOf(index));
    }

    // ============ Factory Method: recursiveDescent() ============

    @Test
    @DisplayName("recursiveDescent() should append recursive descent from root")
    void recursiveDescent_fromRoot_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.ROOT.recursiveDescent("price");
        assertThat(path.getPath()).isEqualTo("$.**.price");
    }

    @Test
    @DisplayName("recursiveDescent() should append recursive descent from member")
    void recursiveDescent_fromMember_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.member("catalog").recursiveDescent("price");
        assertThat(path.getPath()).isEqualTo("$.catalog.**.price");
    }

    @Test
    @DisplayName("recursiveDescent() should support chaining")
    void recursiveDescent_chained_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.ROOT
                .recursiveDescent("users")
                .recursiveDescent("name");
        assertThat(path.getPath()).isEqualTo("$.**.users.**.name");
    }

    @ParameterizedTest
    @ValueSource(strings = {"price", "userName", "_id", "value123"})
    @DisplayName("recursiveDescent() should accept valid identifiers")
    void recursiveDescent_withValidIdentifier_shouldSucceed(String key) {
        @NotNull
        JsonPath path = JsonPath.ROOT.recursiveDescent(key);
        assertThat(path.getPath()).isEqualTo("$.**." + key);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123start", "user-name", "user name", ""})
    @DisplayName("recursiveDescent() should reject invalid identifiers")
    void recursiveDescent_withInvalidIdentifier_shouldThrowException(String key) {
        assertThatThrownBy(() -> JsonPath.ROOT.recursiveDescent(key))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid identifier");
    }

    @Test
    @DisplayName("recursiveDescent() should throw NullPointerException for null key")
    void recursiveDescent_withNullKey_shouldThrowException() {
        assertThatThrownBy(() -> JsonPath.ROOT.recursiveDescent(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("key must not be null");
    }

    // ============ Factory Method: wildcard() ============

    @Test
    @DisplayName("wildcard() should append array wildcard from root")
    void wildcard_fromRoot_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.ROOT.wildcard();
        assertThat(path.getPath()).isEqualTo("$[*]");
    }

    @Test
    @DisplayName("wildcard() should append array wildcard from member")
    void wildcard_fromMember_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.member("users").wildcard();
        assertThat(path.getPath()).isEqualTo("$.users[*]");
    }

    @Test
    @DisplayName("wildcard() should support chaining")
    void wildcard_chained_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.ROOT.wildcard().wildcard();
        assertThat(path.getPath()).isEqualTo("$[*][*]");
    }

    @Test
    @DisplayName("wildcard() from arrayElement should create nested wildcard")
    void wildcard_fromArrayElement_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.arrayElement(0).wildcard();
        assertThat(path.getPath()).isEqualTo("$[0][*]");
    }

    // ============ Constants Tests ============

    @Test
    @DisplayName("ROOT constant should have path '$'")
    void root_shouldHaveCorrectPath() {
        assertThat(JsonPath.ROOT.getPath()).isEqualTo("$");
    }

    @Test
    @DisplayName("ROOT.isRoot() should return true")
    void root_isRoot_shouldReturnTrue() {
        assertThat(JsonPath.ROOT.isRoot()).isTrue();
    }

    @Test
    @DisplayName("isRoot() should return true only for root path")
    void isRoot_withRootPath_shouldReturnTrue() {
        @NotNull
        JsonPath root = JsonPath.of("$");
        assertThat(root.isRoot()).isTrue();
    }

    @Test
    @DisplayName("isRoot() should return false for non-root paths")
    void isRoot_withNonRootPath_shouldReturnFalse() {
        @NotNull
        JsonPath member = JsonPath.member("user");
        assertThat(member.isRoot()).isFalse();
    }

    // ============ ofUnchecked() Tests ============

    @Test
    @DisplayName("ofUnchecked() should bypass validation for valid paths")
    void ofUnchecked_withValidPath_shouldCreateJsonPath() {
        @NotNull
        JsonPath path = JsonPath.ofUnchecked("$.user.name");
        assertThat(path.getPath()).isEqualTo("$.user.name");
    }

    @Test
    @DisplayName("ofUnchecked() should bypass validation for invalid paths")
    void ofUnchecked_withInvalidPath_shouldCreateJsonPath() {
        @NotNull
        JsonPath path = JsonPath.ofUnchecked("$invalid");
        assertThat(path.getPath()).isEqualTo("$invalid");
    }

    @Test
    @DisplayName("ofUnchecked() should allow non-standard syntax")
    void ofUnchecked_withNonStandardSyntax_shouldCreateJsonPath() {
        @NotNull
        JsonPath path = JsonPath.ofUnchecked("$.user-name");
        assertThat(path.getPath()).isEqualTo("$.user-name");
    }

    // ============ Equality and HashCode Tests ============

    @Test
    @DisplayName("equals() should return true for same path")
    void equals_withSamePath_shouldReturnTrue() {
        @NotNull
        JsonPath path1 = JsonPath.of("$.user.name");
        @NotNull
        JsonPath path2 = JsonPath.of("$.user.name");
        assertThat(path1).isEqualTo(path2);
    }

    @Test
    @DisplayName("equals() should return false for different paths")
    void equals_withDifferentPath_shouldReturnFalse() {
        @NotNull
        JsonPath path1 = JsonPath.of("$.user.name");
        @NotNull
        JsonPath path2 = JsonPath.of("$.user.email");
        assertThat(path1).isNotEqualTo(path2);
    }

    @Test
    @DisplayName("equals() should return true for same instance")
    void equals_withSameInstance_shouldReturnTrue() {
        @NotNull
        JsonPath path = JsonPath.of("$.user");
        assertThat(path).isEqualTo(path);
    }

    @Test
    @DisplayName("equals() should return false for null")
    void equals_withNull_shouldReturnFalse() {
        @NotNull
        JsonPath path = JsonPath.of("$.user");
        assertThat(path.equals(null)).isFalse();
    }

    @Test
    @DisplayName("equals() should return false for different type")
    void equals_withDifferentType_shouldReturnFalse() {
        @NotNull
        JsonPath path = JsonPath.of("$.user");
        assertThat(path.equals("$.user")).isFalse();
    }

    @Test
    @DisplayName("hashCode() should be equal for same paths")
    void hashCode_withSamePath_shouldBeEqual() {
        @NotNull
        JsonPath path1 = JsonPath.of("$.user.name");
        @NotNull
        JsonPath path2 = JsonPath.of("$.user.name");
        assertThat(path1.hashCode()).isEqualTo(path2.hashCode());
    }

    @Test
    @DisplayName("hashCode() should be different for different paths")
    void hashCode_withDifferentPath_shouldBeDifferent() {
        @NotNull
        JsonPath path1 = JsonPath.of("$.user.name");
        @NotNull
        JsonPath path2 = JsonPath.of("$.user.email");
        assertThat(path1.hashCode()).isNotEqualTo(path2.hashCode());
    }

    // ============ toString() Tests ============

    @Test
    @DisplayName("toString() should return path string")
    void toString_shouldReturnPath() {
        @NotNull
        JsonPath path = JsonPath.of("$.user.name");
        assertThat(path.toString()).isEqualTo("$.user.name");
    }

    @Test
    @DisplayName("toString() should match getPath()")
    void toString_shouldMatchGetPath() {
        @NotNull
        JsonPath path = JsonPath.member("user");
        assertThat(path.toString()).isEqualTo(path.getPath());
    }

    // ============ Complex Path Building Tests ============

    @Test
    @DisplayName("Should build complex path with multiple operations")
    void buildComplexPath_shouldCreateCorrectPath() {
        @NotNull
        JsonPath path = JsonPath.member("users")
                .wildcard();
        assertThat(path.getPath()).isEqualTo("$.users[*]");
    }

    @Test
    @DisplayName("Should build path with member and array element")
    void buildPath_memberAndArray_shouldCreateCorrectPath() {
        // Note: Current API doesn't support this directly, would need enhancement
        @NotNull
        JsonPath base = JsonPath.member("users");
        // Would need: base.arrayElement(0).member("name")
        // Current workaround: use of() or ofUnchecked()
        @NotNull
        JsonPath path = JsonPath.of("$.users[0].name");
        assertThat(path.getPath()).isEqualTo("$.users[0].name");
    }

    // ============ Edge Cases ============

    @Test
    @DisplayName("Should handle very long valid identifier")
    void longValidIdentifier_shouldSucceed() {
        String longKey = "a".repeat(100);
        @NotNull
        JsonPath path = JsonPath.member(longKey);
        assertThat(path.getPath()).isEqualTo("$." + longKey);
    }

    @Test
    @DisplayName("Should handle very large array index")
    void largeArrayIndex_shouldSucceed() {
        int largeIndex = Integer.MAX_VALUE;
        @NotNull
        JsonPath path = JsonPath.arrayElement(largeIndex);
        assertThat(path.getPath()).isEqualTo("$[" + largeIndex + "]");
    }

    @Test
    @DisplayName("Should handle path with many nesting levels")
    void deeplyNestedPath_shouldSucceed() {
        @NotNull
        JsonPath path = JsonPath.of("$.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z");
        assertThat(path.getPath()).contains(".a.").contains(".z");
    }

    // ============ Serialization Tests ============

    @Test
    @DisplayName("JsonPath should be serializable")
    void jsonPath_shouldBeSerializable() {
        @NotNull
        JsonPath path = JsonPath.member("user");
        assertThat(path).isInstanceOf(java.io.Serializable.class);
    }

    // ============ Immutability Tests ============

    @Test
    @DisplayName("JsonPath operations should return new instances")
    void operations_shouldReturnNewInstances() {
        @NotNull
        JsonPath original = JsonPath.ROOT;
        @NotNull
        JsonPath withWildcard = original.wildcard();

        assertThat(original).isNotSameAs(withWildcard);
        assertThat(original.getPath()).isEqualTo("$");
        assertThat(withWildcard.getPath()).isEqualTo("$[*]");
    }

    @Test
    @DisplayName("recursiveDescent() should not modify original path")
    void recursiveDescent_shouldNotModifyOriginal() {
        @NotNull
        JsonPath original = JsonPath.member("catalog");
        @NotNull
        JsonPath withRecursive = original.recursiveDescent("price");

        assertThat(original.getPath()).isEqualTo("$.catalog");
        assertThat(withRecursive.getPath()).isEqualTo("$.catalog.**.price");
    }
}
