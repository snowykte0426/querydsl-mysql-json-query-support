
allprojects {
    group = "com.github.snowykte0426"
    version = "0.1.0-Dev.3"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(System.getProperty("java.toolchain.version", "25"))
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
                    name.set("QueryDSL MySQL JSON Query Support - ${project.name}")
                    description.set("A QueryDSL extension for MySQL JSON query support - ${project.name} module")
                    url.set("https://github.com/snowykte0426/querydsl-mysql-json-query-support")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("snowykte0426")
                            name.set("Kim Tae Eun")
                            email.set("snowykte0426@naver.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/snowykte0426/querydsl-mysql-json-query-support.git")
                        developerConnection.set("scm:git:ssh://github.com:snowykte0426/querydsl-mysql-json-query-support.git")
                        url.set("https://github.com/snowykte0426/querydsl-mysql-json-query-support")
                    }
                }
            }
        }
    }
}