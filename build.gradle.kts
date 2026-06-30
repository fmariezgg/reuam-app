// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    base
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Google Firebase Dependency
    id("com.google.gms.google-services") version "4.4.4" apply false
}

val isRailwayBuild = System.getenv("RAILWAY_ENVIRONMENT") != null ||
    System.getenv("RAILWAY_PROJECT_ID") != null ||
    System.getenv("RAILWAY_SERVICE_ID") != null

if (isRailwayBuild) {
    if (tasks.findByName("test") == null) {
        tasks.register("test")
    }

    val backendInstallDist = tasks.register("backendInstallDist") {
        dependsOn(gradle.includedBuild("backend").task(":installDist"))
    }

    tasks.named("build") {
        dependsOn(backendInstallDist)
    }
}
