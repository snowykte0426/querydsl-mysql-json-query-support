plugins {
    `java-library`
}

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

    // OpenFeign QueryDSL JPA (maintained fork) - 7.1 already supports Jakarta
    api("io.github.openfeign.querydsl:querydsl-jpa:7.1")

    // Jakarta Persistence API
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // JetBrains annotations (for @Nullable)
    compileOnly("org.jetbrains:annotations:24.0.0")

    // Spring Data JPA (for QuerydslRepositorySupport)
    compileOnly("org.springframework.data:spring-data-jpa:3.2.1")

    // MySQL Connector (compileOnly, users will provide their own)
    compileOnly("com.mysql:mysql-connector-j:8.2.0")

    // Hibernate (for FunctionContributor SPI)
    compileOnly("org.hibernate.orm:hibernate-core:6.4.1.Final")

    // Code Generation (for annotation processing)
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Test annotation processing (for Q-classes from test entities)
    testAnnotationProcessor("io.github.openfeign.querydsl:querydsl-apt:7.1:jakarta")
    testAnnotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

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

    // Hibernate (for JPA integration tests)
    testImplementation("org.hibernate.orm:hibernate-core:6.4.1.Final")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Spring Boot (optional, for JPA integration tests)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.1")

    // Assertions
    testImplementation("org.assertj:assertj-core:3.25.1")
}

tasks.test {
    useJUnitPlatform()
}
