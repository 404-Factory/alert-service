// settings.gradle.kts

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://maven.pkg.github.com/404-Factory/build-logic")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(System.getenv("GITHUB_ACTOR"))
                    .orNull ?: error("GITHUB_ACTOR must be set in environment")
                password = providers.gradleProperty("gpr.token")
                    .orElse(System.getenv("GITHUB_TOKEN"))
                    .orNull ?: error("GITHUB_TOKEN must be set in environment")
            }
        }
    }

    plugins {
        id("com.factory.spring-application-conventions") version "1.0.5"
        id("com.factory.maven-consumer-conventions") version "1.0.5"
    }
}

rootProject.name = "notification-service"