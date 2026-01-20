# MySQL JSON Functions Reference

Complete reference guide for all 35 MySQL JSON functions supported by QueryDSL MySQL JSON Query Support.

## Table of Contents

- [Creation Functions (3)](#creation-functions)
- [Search Functions (10)](#search-functions)
- [Modification Functions (9)](#modification-functions)
- [Attribute Functions (4)](#attribute-functions)
- [Utility Functions (3)](#utility-functions)
- [Schema Validation Functions (2)](#schema-validation-functions)
- [Aggregate Functions (2)](#aggregate-functions)
- [Table Functions (1)](#table-functions)

---

## Creation Functions

Functions that create JSON values.

### JSON_ARRAY

Creates a JSON array from values.

**MySQL SQL:**
```sql
JSON_ARRAY(val1, val2, ...)
```

**JPA Module:**
```java
// From values
JsonArrayExpression arr = JPAJsonFunctions.jsonArray(1, 2, 3);
// Result: [1, 2, 3]

// From expressions
JsonArrayExpression arr = JPAJsonFunctions.jsonArray(
    user.id,
    user.name
);

// Empty array
JsonArrayExpression empty = JPAJsonFunctions.emptyJsonArray();
// Result: []

// From collection
List<String> items = Arrays.asList("a", "b", "c");
JsonArrayExpression arr = JPAJsonFunctions.jsonArrayFrom(items);
```

**SQL Module:**
```java
JsonArrayExpression arr = SqlJsonFunctions.jsonArray("apple", "banana", "cherry");

JsonArrayExpression arr = SqlJsonFunctions.jsonArray(
    products.id,
    products.name
);
```

**Parameters:**
- `values...`: Variable number of values or expressions

**Returns:** `JsonArrayExpression`

---

### JSON_OBJECT

Creates a JSON object from key-value pairs.

**MySQL SQL:**
```sql
JSON_OBJECT(key1, val1, key2, val2, ...)
```

**JPA Module:**
```java
// From key-value pairs
JsonObjectExpression obj = JPAJsonFunctions.jsonObject(
    "name", "John",
    "age", 30,
    "city", "New York"
);
// Result: {"name":"John","age":30,"city":"New York"}

// With expressions
JsonObjectExpression obj = JPAJsonFunctions.jsonObject(
    "id", user.id,
    "name", user.name
);

// Empty object
JsonObjectExpression empty = JPAJsonFunctions.emptyJsonObject();
// Result: {}

// From Map
Map<String, Object> data = Map.of("key", "value");
JsonObjectExpression obj = JPAJsonFunctions.jsonObjectFrom(data);

// Builder pattern
JsonObjectExpression obj = JPAJsonFunctions.jsonObjectBuilder()
    .add("name", "John")
    .add("age", 30)
    .build();
```

**SQL Module:**
```java
JsonObjectExpression obj = SqlJsonFunctions.jsonObject(
    "product", "Laptop",
    "price", 999.99
);
```

**Parameters:**
- `keyValuePairs...`: Alternating keys and values

**Returns:** `JsonObjectExpression`

**Note:** Keys must be strings, values can be any type.

---

### JSON_QUOTE

Quotes a string as a JSON string literal.

**MySQL SQL:**
```sql
JSON_QUOTE(string)
```

**JPA Module:**
```java
// From string
JsonValueExpression quoted = JPAJsonFunctions.jsonQuote("hello");
// Result: "hello"

// From expression
JsonValueExpression quoted = JPAJsonFunctions.jsonQuote(user.name);

// JSON null
StringExpression nullValue = JPAJsonFunctions.jsonNull();
// Result: null (JSON null, not database NULL)
```

**SQL Module:**
```java
JsonValueExpression quoted = SqlJsonFunctions.jsonQuote("value");
```

**Parameters:**
- `value`: String or string expression to quote

**Returns:** `JsonValueExpression`

**Note:** Escapes special characters for valid JSON.

---

## Search Functions

Functions that search and extract data from JSON documents.

### JSON_EXTRACT

Extracts data from a JSON document using path expressions.

**MySQL SQL:**
```sql
JSON_EXTRACT(json_doc, path)
json_doc -> path  -- Shorthand
```

**JPA Module:**
```java
// Extract single path
JsonExpression<String> role = JPAJsonFunctions.jsonExtract(
    user.metadata,
    "$.role"
);

// Extract multiple paths
JsonExpression<String> data = JPAJsonFunctions.jsonExtract(
    user.metadata,
    "$.name",
    "$.email"
);
// Result: ["John", "john@example.com"]

// Nested path
JsonExpression<String> city = JPAJsonFunctions.jsonExtract(
    user.metadata,
    "$.address.city"
);

// Array element
JsonExpression<String> firstTag = JPAJsonFunctions.jsonExtract(
    user.tags,
    "$[0]"
);

// Fluent API
JPAJsonExpression expr = JPAJsonExpression.of(user.metadata);
JsonExpression<String> role = expr.extract("$.role");
```

**SQL Module:**
```java
JsonExpression<String> category = SqlJsonFunctions.jsonExtract(
    products.attributes,
    "$.category"
);

// Fluent API
SqlJsonExpression expr = SqlJsonExpression.of(users.metadata);
JsonExpression<String> plan = expr.extract("$.plan");
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path string(s)

**Returns:** `JsonExpression<String>`

**Path Syntax:**
- `$` - Root
- `.key` - Object member
- `[N]` - Array element (0-indexed)
- `[*]` - All array elements
- `**` - Recursive descent

---

### JSON_UNQUOTE / ->>

Extracts and unquotes a JSON value.

**MySQL SQL:**
```sql
JSON_UNQUOTE(JSON_EXTRACT(json_doc, path))
json_doc ->> path  -- Shorthand
```

**JPA Module:**
```java
// Extract and unquote
StringExpression role = JPAJsonFunctions.jsonUnquoteExtract(
    user.metadata,
    "$.role"
);
// If metadata is {"role":"admin"}, returns: admin (not "admin")

// Direct unquote
StringExpression unquoted = JPAJsonFunctions.jsonUnquote("\"hello\"");
// Result: hello

// Fluent API
StringExpression name = JPAJsonExpression.of(user.metadata)
    .extractUnquoted("$.name");
```

**SQL Module:**
```java
StringExpression status = SqlJsonFunctions.jsonUnquoteExtract(
    users.settings,
    "$.status"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path string

**Returns:** `StringExpression`

**Note:** Removes surrounding quotes from strings. Non-string values returned as-is.

---

### JSON_VALUE

Extracts a scalar value from a JSON document.

**MySQL SQL:**
```sql
JSON_VALUE(json_doc, path)  -- MySQL 8.0.21+
```

**JPA Module:**
```java
JsonValueExpression value = JPAJsonFunctions.jsonValue(
    user.metadata,
    "$.age"
);

// Fluent API
JsonValueExpression value = JPAJsonExpression.of(user.metadata)
    .value("$.age");
```

**SQL Module:**
```java
JsonValueExpression price = SqlJsonFunctions.jsonValue(
    products.data,
    "$.price"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path string

**Returns:** `JsonValueExpression`

**Note:** Designed for scalar values (strings, numbers, booleans, null).

---

### JSON_CONTAINS

Tests whether a JSON document contains a specific value.

**MySQL SQL:**
```sql
JSON_CONTAINS(json_doc, val)
JSON_CONTAINS(json_doc, val, path)
```

**JPA Module:**
```java
// Check if contains value
BooleanExpression hasAdmin = JPAJsonFunctions.jsonContains(
    user.roles,
    "\"admin\""
);

// Check at specific path
BooleanExpression hasFeature = JPAJsonFunctions.jsonContains(
    user.metadata,
    "\"darkMode\"",
    "$.features"
);

// Check with expression
BooleanExpression contains = JPAJsonFunctions.jsonContains(
    user.tags,
    Expressions.constant("\"premium\"")
);

// Fluent API
BooleanExpression has = JPAJsonExpression.of(user.metadata)
    .contains("\"value\"", "$.path");
```

**SQL Module:**
```java
BooleanExpression hasCategory = SqlJsonFunctions.jsonContains(
    products.categories,
    "\"electronics\""
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `value`: Value to search for (as JSON string)
- `path`: (Optional) JSON path to check

**Returns:** `BooleanExpression`

**Note:** Value must be a valid JSON string (e.g., `"\"admin\""` not `"admin"`).

---

### JSON_CONTAINS_PATH

Tests whether a JSON document contains data at specified paths.

**MySQL SQL:**
```sql
JSON_CONTAINS_PATH(json_doc, one_or_all, path, ...)
```

**JPA Module:**
```java
// Check if ANY path exists
BooleanExpression hasPath = JPAJsonFunctions.jsonContainsPath(
    user.metadata,
    "one",
    "$.email",
    "$.phone"
);

// Check if ALL paths exist
BooleanExpression hasAllPaths = JPAJsonFunctions.jsonContainsPath(
    user.metadata,
    "all",
    "$.name",
    "$.email"
);

// Fluent API
BooleanExpression exists = JPAJsonExpression.of(user.metadata)
    .containsPath("one", "$.field1", "$.field2");
```

**SQL Module:**
```java
BooleanExpression hasPath = SqlJsonFunctions.jsonContainsPath(
    users.settings,
    "all",
    "$.theme",
    "$.language"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `oneOrAll`: `"one"` (match any) or `"all"` (match all)
- `paths...`: Variable number of path strings

**Returns:** `BooleanExpression`

---

### JSON_KEYS

Returns the keys from a JSON object as a JSON array.

**MySQL SQL:**
```sql
JSON_KEYS(json_doc)
JSON_KEYS(json_doc, path)
```

**JPA Module:**
```java
// Get top-level keys
JsonArrayExpression keys = JPAJsonFunctions.jsonKeys(user.metadata);
// If metadata is {"name":"John","age":30}, returns: ["name","age"]

// Get keys at path
JsonArrayExpression subKeys = JPAJsonFunctions.jsonKeys(
    user.metadata,
    "$.address"
);

// Fluent API
JsonArrayExpression keys = JPAJsonExpression.of(user.metadata)
    .keys();

JsonArrayExpression subKeys = JPAJsonExpression.of(user.metadata)
    .keys("$.nested");
```

**SQL Module:**
```java
JsonArrayExpression keys = SqlJsonFunctions.jsonKeys(users.metadata);
```

**Parameters:**
- `jsonDoc`: JSON object expression
- `path`: (Optional) Path to object

**Returns:** `JsonArrayExpression`

**Note:** Only works on JSON objects, not arrays.

---

### JSON_SEARCH

Searches for a string within a JSON document and returns its path(s).

**MySQL SQL:**
```sql
JSON_SEARCH(json_doc, 'one', search_str)
JSON_SEARCH(json_doc, 'all', search_str)
```

**JPA Module:**
```java
// Find first occurrence
JsonValueExpression path = JPAJsonFunctions.jsonSearch(
    user.metadata,
    "John"
);

// Find all occurrences
JsonValueExpression paths = JPAJsonFunctions.jsonSearch(
    user.metadata,
    "all",
    "admin"
);

// Fluent API
JsonValueExpression path = JPAJsonExpression.of(user.metadata)
    .search("value");

JsonValueExpression paths = JPAJsonExpression.of(user.metadata)
    .search("all", "pattern");
```

**SQL Module:**
```java
JsonValueExpression path = SqlJsonFunctions.jsonSearch(
    users.data,
    "all",
    "search_value"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `oneOrAll`: `"one"` (first match) or `"all"` (all matches)
- `searchString`: String to search for

**Returns:** `JsonValueExpression`

**Note:** Returns path(s) where value is found, or NULL if not found.

---

### JSON_OVERLAPS

Tests whether two JSON documents have common elements.

**MySQL SQL:**
```sql
JSON_OVERLAPS(json_doc1, json_doc2)  -- MySQL 8.0.17+
```

**JPA Module:**
```java
// Compare two JSON documents
BooleanExpression overlaps = JPAJsonFunctions.jsonOverlaps(
    user1.interests,
    user2.interests
);

// Compare with literal
BooleanExpression overlaps = JPAJsonFunctions.jsonOverlaps(
    user.tags,
    "[\"premium\",\"verified\"]"
);

// Fluent API
BooleanExpression overlaps = JPAJsonExpression.of(user.tags)
    .overlaps(otherUser.tags);
```

**SQL Module:**
```java
BooleanExpression overlaps = SqlJsonFunctions.jsonOverlaps(
    users1.preferences,
    users2.preferences
);
```

**Parameters:**
- `jsonDoc1`: First JSON document
- `jsonDoc2`: Second JSON document or literal

**Returns:** `BooleanExpression`

**Note:** Works with arrays and objects. For arrays, checks element overlap.

---

### MEMBER OF

Tests whether a value is a member of a JSON array.

**MySQL SQL:**
```sql
value MEMBER OF(json_array)  -- MySQL 8.0.17+
```

**JPA Module:**
```java
// Check if value in array
BooleanExpression isMember = JPAJsonFunctions.memberOf(
    "admin",
    user.roles
);

// Check with expression
BooleanExpression isMember = JPAJsonFunctions.memberOf(
    user.role,
    allowedRoles
);

// Query usage
List<User> admins = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.memberOf("admin", user.roles))
    .fetch();
```

**SQL Module:**
```java
BooleanExpression isMember = SqlJsonFunctions.memberOf(
    "premium",
    users.subscriptions
);

// In queries
List<Tuple> results = queryFactory
    .select(users.all())
    .from(users)
    .where(SqlJsonFunctions.memberOf("verified", users.badges))
    .fetch();
```

**Parameters:**
- `value`: Value to check (object or expression)
- `jsonArray`: JSON array expression

**Returns:** `BooleanExpression`

**Note:** Value is compared for equality with array elements.

---

## Modification Functions

Functions that modify JSON documents (return new documents, don't mutate).

### JSON_SET

Inserts or updates values in a JSON document.

**MySQL SQL:**
```sql
JSON_SET(json_doc, path, val, ...)
```

**JPA Module:**
```java
// Set single value
JsonValueExpression updated = JPAJsonFunctions.jsonSet(
    user.metadata,
    "$.lastLogin",
    "2024-01-17"
);

// Set multiple values
StringExpression updated = JPAJsonFunctions.jsonSet(
    user.metadata,
    "$.status", "active",
    "$.lastSeen", "2024-01-17"
);

// In UPDATE query
queryFactory
    .update(user)
    .set(user.metadata, JPAJsonFunctions.jsonSet(
        user.metadata,
        "$.updated",
        "true"
    ))
    .where(user.id.eq(userId))
    .execute();

// Fluent API
JsonValueExpression updated = JPAJsonExpression.of(user.metadata)
    .set("$.field", "value");
```

**SQL Module:**
```java
JsonValueExpression updated = SqlJsonFunctions.jsonSet(
    users.settings,
    "$.theme",
    "dark"
);

// In UPDATE
queryFactory
    .update(users)
    .set(users.metadata, SqlJsonFunctions.jsonSet(
        users.metadata,
        "$.active",
        true
    ))
    .execute();
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path
- `value`: Value to set
- Additional path-value pairs (optional)

**Returns:** `JsonValueExpression` or `StringExpression`

**Note:** Creates path if it doesn't exist, updates if it does.

---

### JSON_INSERT

Inserts values without replacing existing values.

**MySQL SQL:**
```sql
JSON_INSERT(json_doc, path, val, ...)
```

**JPA Module:**
```java
// Insert only if path doesn't exist
JsonValueExpression inserted = JPAJsonFunctions.jsonInsert(
    user.metadata,
    "$.createdAt",
    "2024-01-17"
);

// Multiple inserts
StringExpression inserted = JPAJsonFunctions.jsonInsert(
    user.metadata,
    "$.field1", "value1",
    "$.field2", "value2"
);

// Fluent API
JsonValueExpression inserted = JPAJsonExpression.of(user.metadata)
    .insert("$.newField", "newValue");
```

**SQL Module:**
```java
JsonValueExpression inserted = SqlJsonFunctions.jsonInsert(
    users.config,
    "$.initialized",
    "true"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path
- `value`: Value to insert
- Additional path-value pairs (optional)

**Returns:** `JsonValueExpression` or `StringExpression`

**Note:** Does nothing if path already exists (unlike JSON_SET).

---

### JSON_REPLACE

Replaces existing values in a JSON document.

**MySQL SQL:**
```sql
JSON_REPLACE(json_doc, path, val, ...)
```

**JPA Module:**
```java
// Replace only if path exists
JsonValueExpression replaced = JPAJsonFunctions.jsonReplace(
    user.metadata,
    "$.status",
    "inactive"
);

// Multiple replacements
StringExpression replaced = JPAJsonFunctions.jsonReplace(
    user.metadata,
    "$.field1", "newValue1",
    "$.field2", "newValue2"
);

// Fluent API
JsonValueExpression replaced = JPAJsonExpression.of(user.metadata)
    .replace("$.field", "newValue");
```

**SQL Module:**
```java
JsonValueExpression replaced = SqlJsonFunctions.jsonReplace(
    users.profile,
    "$.bio",
    "Updated bio"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: JSON path
- `value`: New value
- Additional path-value pairs (optional)

**Returns:** `JsonValueExpression` or `StringExpression`

**Note:** Does nothing if path doesn't exist (unlike JSON_SET).

---

### JSON_REMOVE

Removes data from a JSON document.

**MySQL SQL:**
```sql
JSON_REMOVE(json_doc, path, ...)
```

**JPA Module:**
```java
// Remove single path
StringExpression removed = JPAJsonFunctions.jsonRemove(
    user.metadata,
    "$.temporaryField"
);

// Remove multiple paths
JsonValueExpression removed = JPAJsonFunctions.jsonRemove(
    user.metadata,
    "$.field1",
    "$.field2",
    "$.field3"
);

// Remove array element
StringExpression removed = JPAJsonFunctions.jsonRemove(
    user.tags,
    "$[2]"  // Remove 3rd element
);

// Fluent API
JsonValueExpression removed = JPAJsonExpression.of(user.metadata)
    .remove("$.field");
```

**SQL Module:**
```java
StringExpression removed = SqlJsonFunctions.jsonRemove(
    users.data,
    "$.deprecated"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `paths...`: One or more paths to remove

**Returns:** `StringExpression` or `JsonValueExpression`

**Note:** Silently ignores non-existent paths.

---

### JSON_ARRAY_APPEND

Appends values to JSON arrays.

**MySQL SQL:**
```sql
JSON_ARRAY_APPEND(json_doc, path, val, ...)
```

**JPA Module:**
```java
// Append to array at root
JsonArrayExpression appended = JPAJsonFunctions.jsonArrayAppend(
    user.tags,
    "$",
    "newTag"
);

// Append to nested array
JsonArrayExpression appended = JPAJsonFunctions.jsonArrayAppend(
    user.metadata,
    "$.interests",
    "coding"
);

// Multiple appends
JsonArrayExpression appended = JPAJsonFunctions.jsonArrayAppend(
    user.tags,
    "$", "tag1",
    "$", "tag2"
);

// Fluent API
JsonArrayExpression appended = JPAJsonExpression.of(user.tags)
    .arrayAppend("$", "value");
```

**SQL Module:**
```java
JsonArrayExpression appended = SqlJsonFunctions.jsonArrayAppend(
    users.categories,
    "$",
    "new_category"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: Path to array
- `value`: Value to append
- Additional path-value pairs (optional)

**Returns:** `JsonArrayExpression`

**Note:** If target is not an array, wraps it in an array.

---

### JSON_ARRAY_INSERT

Inserts values into JSON arrays at specific positions.

**MySQL SQL:**
```sql
JSON_ARRAY_INSERT(json_doc, path, val, ...)
```

**JPA Module:**
```java
// Insert at specific index
JsonArrayExpression inserted = JPAJsonFunctions.jsonArrayInsert(
    user.tags,
    "$[1]",  // Insert at index 1
    "urgentTag"
);

// Multiple inserts
JsonArrayExpression inserted = JPAJsonFunctions.jsonArrayInsert(
    user.items,
    "$[0]", "firstItem",
    "$[5]", "sixthItem"
);

// Fluent API
JsonArrayExpression inserted = JPAJsonExpression.of(user.list)
    .arrayInsert("$[0]", "value");
```

**SQL Module:**
```java
JsonArrayExpression inserted = SqlJsonFunctions.jsonArrayInsert(
    users.priorities,
    "$[0]",
    "high_priority"
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: Path with array index (e.g., `$[2]`)
- `value`: Value to insert

**Returns:** `JsonArrayExpression`

**Note:** Shifts existing elements to the right.

---

### JSON_MERGE_PATCH

Merges JSON documents using RFC 7386 semantics.

**MySQL SQL:**
```sql
JSON_MERGE_PATCH(json_doc1, json_doc2, ...)
```

**JPA Module:**
```java
// Merge two documents (second overwrites first)
JsonObjectExpression merged = JPAJsonFunctions.jsonMergePatch(
    user.metadata,
    Expressions.constant("{\"status\":\"updated\"}")
);

// Merge multiple
StringExpression merged = JPAJsonFunctions.jsonMergePatch(
    user.settings,
    defaultSettings,
    customSettings
);

// Fluent API
JsonObjectExpression merged = JPAJsonExpression.of(user.config)
    .mergePatch(newConfig);
```

**SQL Module:**
```java
JsonObjectExpression merged = SqlJsonFunctions.jsonMergePatch(
    users.preferences,
    users.overrides
);
```

**Parameters:**
- `jsonDocs...`: Two or more JSON documents

**Returns:** `JsonObjectExpression` or `StringExpression`

**Note:** RFC 7386 - Later documents override earlier. `null` removes keys.

---

### JSON_MERGE_PRESERVE

Merges JSON documents, preserving duplicate keys as arrays.

**MySQL SQL:**
```sql
JSON_MERGE_PRESERVE(json_doc1, json_doc2, ...)
```

**JPA Module:**
```java
// Merge preserving duplicates
StringExpression merged = JPAJsonFunctions.jsonMergePreserve(
    user.tags1,
    user.tags2
);

// Fluent API
StringExpression merged = JPAJsonExpression.of(user.data)
    .mergePreserve(additionalData);
```

**SQL Module:**
```java
StringExpression merged = SqlJsonFunctions.jsonMergePreserve(
    users.attributes1,
    users.attributes2
);
```

**Parameters:**
- `jsonDocs...`: Two or more JSON documents

**Returns:** `StringExpression`

**Note:** Duplicate keys become arrays with all values.

---

### JSON_UNQUOTE

Unquotes a JSON string.

**MySQL SQL:**
```sql
JSON_UNQUOTE(json_val)
```

**JPA Module:**
```java
// Unquote a JSON string
StringExpression unquoted = JPAJsonFunctions.jsonUnquote(
    "\"hello world\""
);
// Result: hello world

// Unquote extracted value
StringExpression name = JPAJsonFunctions.jsonUnquote(
    JPAJsonFunctions.jsonExtract(user.metadata, "$.name")
);

// Fluent API
StringExpression unquoted = JPAJsonExpression.of(user.quoted)
    .unquote();
```

**SQL Module:**
```java
StringExpression unquoted = SqlJsonFunctions.jsonUnquote(
    SqlJsonFunctions.jsonExtract(users.data, "$.field")
);
```

**Parameters:**
- `jsonValue`: JSON string expression

**Returns:** `StringExpression`

**Note:** Removes quotes and unescapes characters.

---

## Attribute Functions

Functions that return information about JSON values.

### JSON_DEPTH

Returns the maximum depth of a JSON document.

**MySQL SQL:**
```sql
JSON_DEPTH(json_doc)
```

**JPA Module:**
```java
// Get depth
NumberExpression<Integer> depth = JPAJsonFunctions.jsonDepth(
    user.metadata
);

// In query
List<User> deepNested = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonDepth(user.metadata).gt(3))
    .fetch();

// Fluent API
NumberExpression<Integer> depth = JPAJsonExpression.of(user.data)
    .depth();
```

**SQL Module:**
```java
NumberExpression<Integer> depth = SqlJsonFunctions.jsonDepth(
    users.config
);
```

**Parameters:**
- `jsonDoc`: JSON document expression

**Returns:** `NumberExpression<Integer>`

**Note:** Empty arrays/objects have depth 1, scalars have depth 1.

---

### JSON_LENGTH

Returns the number of elements in a JSON document.

**MySQL SQL:**
```sql
JSON_LENGTH(json_doc)
JSON_LENGTH(json_doc, path)
```

**JPA Module:**
```java
// Get length of document
NumberExpression<Integer> length = JPAJsonFunctions.jsonLength(
    user.tags
);

// Get length at path
NumberExpression<Integer> length = JPAJsonFunctions.jsonLength(
    user.metadata,
    "$.items"
);

// In query
List<User> manyTags = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonLength(user.tags).goe(5))
    .fetch();

// Fluent API
NumberExpression<Integer> len = JPAJsonExpression.of(user.array)
    .length();

NumberExpression<Integer> len = JPAJsonExpression.of(user.doc)
    .length("$.path");
```

**SQL Module:**
```java
NumberExpression<Integer> length = SqlJsonFunctions.jsonLength(
    users.items
);
```

**Parameters:**
- `jsonDoc`: JSON document expression
- `path`: (Optional) Path to count

**Returns:** `NumberExpression<Integer>`

**Note:** For arrays: element count. For objects: key count. For scalars: 1.

---

### JSON_TYPE

Returns the type of a JSON value.

**MySQL SQL:**
```sql
JSON_TYPE(json_val)
```

**JPA Module:**
```java
// Get type
StringExpression type = JPAJsonFunctions.jsonType(user.field);

// In query - find arrays
List<User> arrayFields = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonType(user.data).eq("ARRAY"))
    .fetch();

// Fluent API
StringExpression type = JPAJsonExpression.of(user.value)
    .type();

// Type checks
BooleanExpression isArray = JPAJsonExpression.of(user.data)
    .isArray();

BooleanExpression isObject = JPAJsonExpression.of(user.data)
    .isObject();

BooleanExpression isScalar = JPAJsonExpression.of(user.data)
    .isScalar();
```

**SQL Module:**
```java
StringExpression type = SqlJsonFunctions.jsonType(users.column);
```

**Parameters:**
- `jsonValue`: JSON value expression

**Returns:** `StringExpression`

**Possible Return Values:**
- `"ARRAY"` - JSON array
- `"OBJECT"` - JSON object
- `"STRING"` - JSON string
- `"INTEGER"` - JSON integer
- `"DOUBLE"` - JSON double
- `"BOOLEAN"` - JSON boolean
- `"NULL"` - JSON null

---

### JSON_VALID

Tests whether a value is valid JSON.

**MySQL SQL:**
```sql
JSON_VALID(val)
```

**JPA Module:**
```java
// Check if valid JSON
BooleanExpression valid = JPAJsonFunctions.jsonValid(user.rawJson);

// Check string literal
BooleanExpression valid = JPAJsonFunctions.jsonValid(
    "{\"key\":\"value\"}"
);

// In query - find valid JSON
List<User> validData = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonValid(user.data))
    .fetch();

// Fluent API
BooleanExpression valid = JPAJsonExpression.of(user.field)
    .isValid();

// Check if empty
BooleanExpression empty = JPAJsonExpression.of(user.array)
    .isEmpty();

BooleanExpression notEmpty = JPAJsonExpression.of(user.array)
    .isNotEmpty();
```

**SQL Module:**
```java
BooleanExpression valid = SqlJsonFunctions.jsonValid(users.jsonText);
```

**Parameters:**
- `value`: Value or expression to validate

**Returns:** `BooleanExpression`

**Note:** Returns true for valid JSON, false otherwise.

---

## Utility Functions

Utility functions for JSON data.

### JSON_PRETTY

Formats a JSON document for human readability.

**MySQL SQL:**
```sql
JSON_PRETTY(json_val)
```

**JPA Module:**
```java
// Format for display
StringExpression formatted = JPAJsonFunctions.jsonPretty(
    user.metadata
);

// Format literal
StringExpression formatted = JPAJsonFunctions.jsonPretty(
    "{\"a\":1,\"b\":2}"
);
// Result:
// {
//   "a": 1,
//   "b": 2
// }

// In select
List<String> formattedJson = queryFactory
    .select(JPAJsonFunctions.jsonPretty(user.settings))
    .from(user)
    .fetch();

// Fluent API
StringExpression pretty = JPAJsonExpression.of(user.compact)
    .pretty();
```

**SQL Module:**
```java
StringExpression formatted = SqlJsonFunctions.jsonPretty(users.data);
```

**Parameters:**
- `jsonValue`: JSON value expression or string

**Returns:** `StringExpression`

**Note:** Adds indentation and newlines for readability.

---

### JSON_STORAGE_SIZE

Returns the storage size of a JSON document in bytes.

**MySQL SQL:**
```sql
JSON_STORAGE_SIZE(json_val)
```

**JPA Module:**
```java
// Get storage size
NumberExpression<Integer> size = JPAJsonFunctions.jsonStorageSize(
    user.metadata
);

// Find large documents
List<User> largeDocs = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonStorageSize(user.data).gt(1000))
    .fetch();

// Fluent API
NumberExpression<Integer> size = JPAJsonExpression.of(user.json)
    .storageSize();
```

**SQL Module:**
```java
NumberExpression<Integer> size = SqlJsonFunctions.jsonStorageSize(
    users.largeDocument
);
```

**Parameters:**
- `jsonValue`: JSON value expression or string

**Returns:** `NumberExpression<Integer>`

**Note:** Returns approximate storage size in bytes.

---

### JSON_STORAGE_FREE

Returns the freed space after a partial JSON update.

**MySQL SQL:**
```sql
JSON_STORAGE_FREE(json_val)
```

**JPA Module:**
```java
// Get freed space
NumberExpression<Integer> freed = JPAJsonFunctions.jsonStorageFree(
    user.metadata
);

// Monitor after updates
NumberExpression<Integer> freed = JPAJsonFunctions.jsonStorageFree(
    user.optimizedField
);

// Fluent API
NumberExpression<Integer> freed = JPAJsonExpression.of(user.updated)
    .storageFree();
```

**SQL Module:**
```java
NumberExpression<Integer> freed = SqlJsonFunctions.jsonStorageFree(
    users.column
);
```

**Parameters:**
- `jsonColumn`: JSON column expression

**Returns:** `NumberExpression<Integer>`

**Note:** Shows space freed by partial updates (binary JSON feature).

---

## Schema Validation Functions

Functions for validating JSON against schemas (MySQL 8.0.17+).

### JSON_SCHEMA_VALID

Validates a JSON document against a JSON Schema.

**MySQL SQL:**
```sql
JSON_SCHEMA_VALID(schema, document)
```

**JPA Module:**
```java
// Define schema
String userSchema = """
{
  "type": "object",
  "required": ["name", "email"],
  "properties": {
    "name": {"type": "string"},
    "email": {"type": "string", "format": "email"},
    "age": {"type": "integer", "minimum": 0}
  }
}
""";

// Validate
BooleanExpression valid = JPAJsonFunctions.jsonSchemaValid(
    userSchema,
    user.metadata
);

// Find invalid documents
List<User> invalidUsers = queryFactory
    .selectFrom(user)
    .where(JPAJsonFunctions.jsonSchemaValid(userSchema, user.metadata)
        .isFalse())
    .fetch();

// With expression
BooleanExpression valid = JPAJsonFunctions.jsonSchemaValid(
    schemaEntity.schemaJson,
    user.metadata
);

// Fluent API
BooleanExpression valid = JPAJsonExpression.of(user.data)
    .schemaValid(schema);
```

**SQL Module:**
```java
BooleanExpression valid = SqlJsonFunctions.jsonSchemaValid(
    schemaJson,
    users.data
);
```

**Parameters:**
- `schema`: JSON Schema (string or expression)
- `document`: JSON document to validate

**Returns:** `BooleanExpression`

**Note:** Supports Draft 4 of JSON Schema specification.

---

### JSON_SCHEMA_VALIDATION_REPORT

Returns a detailed validation report.

**MySQL SQL:**
```sql
JSON_SCHEMA_VALIDATION_REPORT(schema, document)
```

**JPA Module:**
```java
// Get validation report
StringExpression report = JPAJsonFunctions.jsonSchemaValidationReport(
    schemaJson,
    user.metadata
);

// Example report structure:
// {
//   "valid": false,
//   "reason": "The JSON document location '#/age' failed requirement 'minimum' at JSON Schema location '#/properties/age'",
//   "schema-location": "#/properties/age",
//   "document-location": "#/age",
//   "schema-failed-keyword": "minimum"
// }

// Select reports for invalid documents
List<Tuple> reports = queryFactory
    .select(
        user.id,
        JPAJsonFunctions.jsonSchemaValidationReport(schema, user.data)
    )
    .from(user)
    .fetch();

// Fluent API
StringExpression report = JPAJsonExpression.of(user.document)
    .schemaValidationReport(schema);
```

**SQL Module:**
```java
StringExpression report = SqlJsonFunctions.jsonSchemaValidationReport(
    schemaJson,
    users.data
);
```

**Parameters:**
- `schema`: JSON Schema (string or expression)
- `document`: JSON document to validate

**Returns:** `StringExpression` (JSON object with validation details)

**Note:** Provides detailed error information for debugging.

---

## Aggregate Functions

Functions that aggregate values from multiple rows.

### JSON_ARRAYAGG

Aggregates values into a JSON array.

**MySQL SQL:**
```sql
JSON_ARRAYAGG(value)
```

**JPA Module:**
```java
// Aggregate user names
List<String> nameArrays = queryFactory
    .select(JPAJsonFunctions.jsonArrayAgg(user.name))
    .from(user)
    .groupBy(user.department)
    .fetch();
// Result per group: ["Alice","Bob","Charlie"]

// With group by
List<Tuple> deptUsers = queryFactory
    .select(
        user.department,
        JPAJsonFunctions.jsonArrayAgg(user.name)
    )
    .from(user)
    .groupBy(user.department)
    .fetch();

// Aggregate expressions
List<JsonArrayExpression> arrays = queryFactory
    .select(JPAJsonFunctions.jsonArrayAgg(
        JPAJsonFunctions.jsonExtract(user.metadata, "$.role")
    ))
    .from(user)
    .groupBy(user.team)
    .fetch();

// Alias for conciseness
JsonArrayExpression array = JPAJsonFunctions.arrayAgg(user.email);
```

**SQL Module:**
```java
List<Tuple> aggregated = queryFactory
    .select(
        users.category,
        SqlJsonFunctions.jsonArrayAgg(users.name)
    )
    .from(users)
    .groupBy(users.category)
    .fetch();
```

**Parameters:**
- `value`: Expression to aggregate

**Returns:** `JsonArrayExpression`

**Note:** Returns NULL for empty groups (standard SQL aggregate behavior).

---

### JSON_OBJECTAGG

Aggregates key-value pairs into a JSON object.

**MySQL SQL:**
```sql
JSON_OBJECTAGG(key, value)
```

**JPA Module:**
```java
// Aggregate as object
List<JsonObjectExpression> objects = queryFactory
    .select(JPAJsonFunctions.jsonObjectAgg(
        user.name,
        user.email
    ))
    .from(user)
    .groupBy(user.department)
    .fetch();
// Result per group: {"Alice":"alice@ex.com","Bob":"bob@ex.com"}

// With string key
List<Tuple> roleMap = queryFactory
    .select(
        user.team,
        JPAJsonFunctions.jsonObjectAgg("user_name", user.name)
    )
    .from(user)
    .groupBy(user.team)
    .fetch();

// Complex aggregation
List<JsonObjectExpression> mapping = queryFactory
    .select(JPAJsonFunctions.jsonObjectAgg(
        user.id.stringValue(),
        JPAJsonFunctions.jsonExtract(user.metadata, "$.status")
    ))
    .from(user)
    .groupBy(user.region)
    .fetch();

// Alias for conciseness
JsonObjectExpression obj = JPAJsonFunctions.objectAgg(
    user.id,
    user.name
);
```

**SQL Module:**
```java
List<Tuple> objectAgg = queryFactory
    .select(
        users.group,
        SqlJsonFunctions.jsonObjectAgg(users.key, users.value)
    )
    .from(users)
    .groupBy(users.group)
    .fetch();
```

**Parameters:**
- `key`: Key expression (converted to string)
- `value`: Value expression

**Returns:** `JsonObjectExpression`

**Note:** Duplicate keys: last value wins. Returns NULL for empty groups.

---

## Table Functions

Functions that transform JSON into relational tables.

### JSON_TABLE

Converts JSON data into a relational table.

**MySQL SQL:**
```sql
JSON_TABLE(
    json_doc,
    path COLUMNS(
        column_name type PATH json_path,
        ...
    )
)
```

**JPA Module:**
```java
// Basic JSON_TABLE (note: JPA has limited support, primarily for native queries)
String sql = """
SELECT jt.*
FROM users u,
JSON_TABLE(
    u.orders,
    '$.items[*]' COLUMNS(
        item_id INT PATH '$.id',
        item_name VARCHAR(255) PATH '$.name',
        quantity INT PATH '$.qty'
    )
) AS jt
""";

List<Object[]> results = entityManager
    .createNativeQuery(sql)
    .getResultList();

// Builder pattern (for SQL module - see below)
```

**SQL Module:**
```java
// Build JSON_TABLE expression
JsonTableExpression table = SqlJsonFunctions.jsonTable(
    users.orders,
    "$.items[*]"
)
.column("item_id", "INT", "$.id")
.column("item_name", "VARCHAR(255)", "$.name")
.column("quantity", "INT", "$.qty")
.build();

// Helper methods for common types
JsonTableExpression table = SqlJsonFunctions.jsonTable()
    .from(users.orders, "$.items[*]")
    .intColumn("id", "$.id")
    .varcharColumn("name", 255, "$.name")
    .intColumn("qty", "$.qty")
    .build();

// With ordinality (row number)
JsonTableExpression table = SqlJsonFunctions.jsonTable(
    users.data,
    "$.records[*]"
)
.ordinalityColumn("row_num")
.intColumn("id", "$.id")
.jsonColumn("details", "$.info")
.build();

// With EXISTS column
JsonTableExpression table = SqlJsonFunctions.jsonTable(
    users.metadata,
    "$"
)
.varcharColumn("email", 255, "$.email")
.existsColumn("has_phone", "$.phone")
.build();
```

**Column Types:**
- `column(name, type, path)` - Generic column
- `intColumn(name, path)` - INT column
- `varcharColumn(name, length, path)` - VARCHAR column
- `jsonColumn(name, path)` - JSON column
- `existsColumn(name, path)` - EXISTS column (1/0)
- `ordinalityColumn(name)` - Row number

**Returns:** `JsonTableExpression.Builder` → `JsonTableExpression`

**Note:** Most useful in SQL module for flattening nested JSON into rows.

**Example Usage in Query:**
```java
// SQL Module - flatten orders
QUsers users = QUsers.users;

// Note: JSON_TABLE typically used in FROM clause
// QueryDSL SQL support varies by version
```

---

## Quick Reference

### Function Categories Summary

| Category | Count | Use Cases |
|----------|-------|-----------|
| **Creation** | 3 | Building JSON from data |
| **Search** | 10 | Querying JSON content |
| **Modification** | 9 | Updating JSON documents |
| **Attribute** | 4 | Analyzing JSON structure |
| **Utility** | 3 | Formatting and diagnostics |
| **Schema** | 2 | Validation against schemas |
| **Aggregate** | 2 | Summarizing data as JSON |
| **Table** | 1 | Flattening JSON to rows |

### Most Commonly Used Functions

1. **JSON_EXTRACT** - Extract values from JSON
2. **JSON_CONTAINS** - Check if value exists
3. **JSON_SET** - Update JSON values
4. **JSON_ARRAY** - Create JSON arrays
5. **JSON_OBJECT** - Create JSON objects
6. **JSON_LENGTH** - Count elements
7. **JSON_TYPE** - Check value type
8. **JSON_ARRAYAGG** - Aggregate as array

### Performance Tips

1. **Use Indexes**: Create functional indexes on frequently queried JSON paths
```sql
CREATE INDEX idx_user_role
ON users((CAST(JSON_EXTRACT(metadata, '$.role') AS CHAR(50))));
```

2. **Generated Columns**: For frequently accessed paths
```sql
ALTER TABLE users ADD COLUMN
    role_generated VARCHAR(50)
    GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_EXTRACT(metadata, '$.role')));
CREATE INDEX idx_role ON users(role_generated);
```

3. **Minimize Extractions**: Extract once, reuse the result
```java
// Good
JsonExpression<String> role = JPAJsonFunctions.jsonExtract(user.metadata, "$.role");
where(role.eq("\"admin\"").and(role.isNotNull()))

// Bad (extracts twice)
where(JPAJsonFunctions.jsonExtract(user.metadata, "$.role").eq("\"admin\"")
    .and(JPAJsonFunctions.jsonExtract(user.metadata, "$.role").isNotNull()))
```

---

## Related Documentation

- [Main README](./README.md) - Project overview
- [JPA Module README](./querydsl-mysql-json-jpa/README.md) - JPA module guide
- [SQL Module README](./querydsl-mysql-json-sql/README.md) - SQL module guide
- [INTEGRATION_GUIDE.md](./INTEGRATION_GUIDE.md) - Framework integration
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Internal architecture
- [MySQL JSON Functions](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html) - Official MySQL docs

---

**Need Help?** Check the module-specific READMEs or open an issue on GitHub.
