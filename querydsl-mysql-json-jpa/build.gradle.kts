plugins {
    `java-library`
}

group = "com.github.snowykte0426"
version = "1.0-SNAPSHOT"

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

    // QueryDSL JPA
    api("com.querydsl:querydsl-jpa:5.1.0:jakarta")

    // Jakarta Persistence API
    compileOnly("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // MySQL Connector (compileOnly, users will provide their own)
    compileOnly("com.mysql:mysql-connector-j:8.2.0")

    // Code Generation (for annotation processing)
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers for integration tests
    testImplementation("org.testcontainers:testcontainers:1.19.3")
    testImplementation("org.testcontainers:mysql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")

    // MySQL for Tests
    testImplementation("com.mysql:mysql-connector-j:8.2.0")

    // Hibernate (for JPA integration tests)
    testImplementation("org.hibernate.orm:hibernate-core:6.4.1.Final")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")

    // Spring Boot (optional, for JPA integration tests)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.1")

    // Logging for Tests
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("org.slf4j:slf4j-api:2.0.9")

    // Assertions
    testImplementation("org.assertj:assertj-core:3.25.1")
}

tasks.test {
    useJUnitPlatform()
}
