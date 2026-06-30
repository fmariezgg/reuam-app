pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ReUAM"

val isRailwayBuild = System.getenv("RAILWAY_ENVIRONMENT") != null ||
    System.getenv("RAILWAY_PROJECT_ID") != null ||
    System.getenv("RAILWAY_SERVICE_ID") != null

if (isRailwayBuild) {
    includeBuild("backend")
} else {
    include(":mobile-app")
}
