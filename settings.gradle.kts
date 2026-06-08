pluginManagement {
  includeBuild("build-logic")
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
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "AppCasa"
include(":app")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":feature:dashboard")
include(":feature:family")
include(":feature:lists")
include(":feature:tasks")
include(":feature:finance")
include(":feature:inventory")
include(":feature:calendar")
include(":feature:settings")
