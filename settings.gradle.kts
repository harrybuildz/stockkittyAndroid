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

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "StockKitty"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":core:model")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:designsystem")
include(":core:testing")
include(":feature:watchlist")
