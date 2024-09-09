import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.jetbrains.kotlin.utils.addIfNotNull

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.machiav3lli.fdroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.machiav3lli.fdroid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1018
        versionName = "1.0.6-alpha01"
        buildConfigField("String", "KEY_API_EXODUS", "\"81f30e4903bde25023857719e71c94829a41e6a5\"")

        javaCompileOptions {
            annotationProcessorOptions {
                ksp {
                    arg("room.schemaLocation", "$projectDir/schemas")
                    arg("room.incremental", "true")
                    arg("room.generateKotlin", "true")
                }
            }
        }
    }

    sourceSets.forEach { source ->
        val javaDir = source.java.srcDirs.find { it.name == "java" }
        source.java {
            srcDir(File(javaDir?.parentFile, "kotlin"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        freeCompilerArgs = listOf("-Xjvm-default=all-compatibility")
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    applicationVariants.all { variant ->
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = "Neo_Store_${variant.name}_${variant.versionName}.apk"
            }
        true
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            resValue("string", "application_name", "Neo Store - Debug")
        }
        create("neo") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".neo"
            resValue("string", "application_name", "Neo Store")
        }
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            resValue("string", "application_name", "Neo Store")
        }
        all {
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard.pro"
            )
        }
    }
    packaging {
        jniLibs {
            excludes += listOf("/okhttp3/internal/publicsuffix/*")
        }
        resources {
            excludes += listOf(
                "/DebugProbesKt.bin",
                "/kotlin/**.kotlin_builtins",
                "/kotlin/**.kotlin_metadata",
                "/META-INF/**.kotlin_module",
                "/META-INF/**.pro",
                "/META-INF/**.version", // comment out to enable layout inspector
                "/okhttp3/internal/publicsuffix/*"
            )
        }
    }

    lint {
        disable += "InvalidVectorPath"
        warning += "InvalidPackage"
    }

    dependenciesInfo {
        // Avoid Google-signed dependency metadata in builds
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {

    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp)
    implementation(libs.preference)
    implementation(libs.activity.compose)
    implementation(libs.collections.immutable)
    //debugImplementation(libs.leakcanary)

    // use the new WorkInfo.stopReason (report stopReason), setNextScheduleTimeOverride (Precise scheduling), Configuration.Builder.setContentUriTriggerWorkersLimit (limit for content uri workers)
    implementation(libs.work.runtime)
    implementation(libs.biometric)
    implementation(libs.material)

    // Coil, Ktor, Okhttp, Zxing
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.zxing.core)
    implementation(libs.ktor.core)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.logging)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.koin.annotations)
    ksp(libs.koin.compiler)

    // JSON, Markdown, LibSu
    implementation(libs.libsu.core)
    implementation(libs.jackson.core)
    implementation(libs.serialization.json)
    implementation(libs.markdown)
    implementation(libs.compose.markdown)

    // Storage
    implementation(libs.simple.storage)

    // Coroutines / Lifecycle
    implementation(libs.lifecycle)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Compose
    api(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.navigationsuite)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.adaptive.layout)
    implementation(libs.compose.adaptive.navigation)
    implementation(libs.compose.animation)
    implementation(libs.compose.navigation)
    implementation(libs.accompanist.permissions)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("src/main/res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile?.name?.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.addIfNotNull(languageCode)
        }
    }
    val langsListString = "{${langsList.sorted().joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")