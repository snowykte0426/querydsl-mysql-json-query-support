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
    // OpenFeign QueryDSL Core (maintained fork)
    api("io.github.openfeign.querydsl:querydsl-core:7.1")

    // JetBrains Annotations
    compileOnly("org.jetbrains:annotations:24.1.0")

    // MySQL Connector (compileOnly, users will provide their own)
    compileOnly("com.mysql:mysql-connector-j:8.2.0")

    // JSON Processing (for utilities)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testcontainers
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.3"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")

    // MySQL for Tests
    testImplementation("com.mysql:mysql-connector-j:8.2.0")

    // Assertions
    testImplementation("org.assertj:assertj-core:3.25.1")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("test.mysql.image", System.getProperty("test.mysql.image", "mysql:8.0.33"))
}
