plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.snowykte0426"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "querydsl-mysql-json-query-support"
            version = project.version.toString()

            from(components["java"])

            pom {
                name = "QueryDSL MySQL JSON Query Support"
                description = "A QueryDSL extension for MySQL JSON query support."
                url = "https://github.com/snowykte0426/querydsl-mysql-json-query-support"

                licenses {
                    license {
                        name = "MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }

                developers {
                    developer {
                        id = "snowykte0426"
                        name = "Kim Tae Eun"
                        email = "snowykte0426@naver.com"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/snowykte0426/querydsl-mysql-json-query-support.git"
                    developerConnection = "scm:git:ssh://github.com:snowykte0426/querydsl-mysql-json-query-support.git"
                    url = "https://github.com/snowykte0426/querydsl-mysql-json-query-support"
                }
            }
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}