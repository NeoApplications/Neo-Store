pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven("../../../prebuilts/sdk/current/androidx/m2repository")
        maven("../../../prebuilts/fullsdk-darwin/extras/android/m2repository")
        maven("../../../prebuilts/fullsdk-linux/extras/android/m2repository")
        maven(url = "https://jitpack.io")
    }
}
include(":iconloaderlib")
project(":iconloaderlib").projectDir =
    File(rootDir, "platform_frameworks_libs_systemui/iconloaderlib")

include(":searchuilib")
project(":searchuilib").projectDir = File(rootDir, "platform_frameworks_libs_systemui/searchuilib")

include(":SystemUIShared")

include(":CompatLib")
include(":CompatLibVR")
include(":CompatLibVS")
rootProject.name = "Neo Launcher"