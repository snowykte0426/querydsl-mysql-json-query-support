rootProject.name = "querydsl-mysql-json-query-support"

include(
    "querydsl-mysql-json-core",
    "querydsl-mysql-json-sql",
    "querydsl-mysql-json-jpa",
)

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

buildCache {
    local {
        directory = file("$rootDir/.gradle/build-cache")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
