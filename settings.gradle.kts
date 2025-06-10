pluginManagement {
    repositories {
        mavenCentral()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        //maven(url = "https://androidx.dev/snapshots/builds/13508953/artifacts/repository") // FOR jetpack SNAPSHOT ONLY
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven(url = "https://jitpack.io")
        //maven(url = "https://androidx.dev/snapshots/builds/13508953/artifacts/repository") // FOR jetpack SNAPSHOT ONLY
    }
}
rootProject.name = "Neo Store"