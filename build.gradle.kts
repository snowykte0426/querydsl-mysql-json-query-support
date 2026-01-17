import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

allprojects {
    group = "io.github.snowykte0426"
    version = "0.1.0-Dev.4"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.vanniktech.maven.publish")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(System.getProperty("java.toolchain.version", "25"))
        }
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
        
        signAllPublications()

        pom {
            name.set("QueryDSL MySQL JSON Query Support - ${project.name}")
            description.set("A QueryDSL extension for MySQL JSON query support - ${project.name} module")
            url.set("https://github.com/snowykte0426/querydsl-mysql-json-query-support")
            inceptionYear.set("2024")

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
