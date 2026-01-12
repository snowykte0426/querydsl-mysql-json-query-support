plugins {
    `java-library` apply false
    `maven-publish` apply false
}

allprojects {
    group = "com.github.snowykte0426"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                pom {
                    name = "QueryDSL MySQL JSON Query Support - ${project.name}"
                    description = "A QueryDSL extension for MySQL JSON query support - ${project.name} module"
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
}