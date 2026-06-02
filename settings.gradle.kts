// settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/404-Factory/build-logic")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: providers.gradleProperty("gpr.user").orNull
                        ?: error("GITHUB_ACTOR must be set in environment")
                password = System.getenv("GITHUB_TOKEN") ?: providers.gradleProperty("gpr.token").orNull
                        ?: error("GITHUB_TOKEN must be set in environment")
            }
        }
    }

    plugins {
        id("com.factory.spring-application-conventions") version "1.0.5"
        id("com.factory.maven-consumer-conventions") version "1.0.5"
    }
}

rootProject.name = "notification-service"