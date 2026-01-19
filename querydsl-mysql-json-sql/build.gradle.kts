plugins {
    `java-library`
}

group = "io.github.snowykte0426"
version = "0.1.0-Beta.4"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Core module
    api(project(":querydsl-mysql-json-core"))

    // OpenFeign QueryDSL SQL (maintained fork)
    api("io.github.openfeign.querydsl:querydsl-sql:7.1")

    // MySQL Connector (compileOnly, users will provide their own)
    compileOnly("com.mysql:mysql-connector-j:8.2.0")

    // JetBrains Annotations
    compileOnly("org.jetbrains:annotations:24.1.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers for integration tests
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")

    // MySQL for Tests
    testImplementation("com.mysql:mysql-connector-j:8.2.0")

    // OpenFeign QueryDSL SQL Codegen
    testImplementation("io.github.openfeign.querydsl:querydsl-sql-codegen:7.1")

    // Assertions
    testImplementation("org.assertj:assertj-core:3.25.1")

    // HikariCP for connection pooling in tests
    testImplementation("com.zaxxer:HikariCP:5.1.0")
}

tasks.test {
    useJUnitPlatform()
}
