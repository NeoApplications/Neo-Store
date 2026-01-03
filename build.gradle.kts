import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.shizuku.refine)
}

val jvmVersion = JavaVersion.VERSION_17

val detectedLocales = detectLocales()
val langsListString = "{${detectedLocales.sorted().joinToString(",") { "\"$it\"" }}}"

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

android {
    namespace = "com.machiav3lli.fdroid"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.machiav3lli.fdroid"
        minSdk = 24
        targetSdk = 36
        versionCode = 1201
        versionName = "1.2.1"
        buildConfigField("String", "KEY_API_EXODUS", "\"81f30e4903bde25023857719e71c94829a41e6a5\"")
        buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
    }

    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Store_${variant.versionName}_${variant.buildType.name}.apk"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            resValue("string", "application_name", "Neo Store - Debug")
        }
        register("neo") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".neo"
            resValue("string", "application_name", "Neo Store - Neo")
        }
        release {
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
        kotlinExtension.sourceSets.all {
            languageSettings.enableLanguageFeature("ExplicitBackingFields")
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

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {

    // Core
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp)
    implementation(libs.preference)
    implementation(libs.activity.compose)
    implementation(libs.collections.immutable)
    implementation(libs.datetime)
    coreLibraryDesugaring(libs.jdk.desugar)
    //debugImplementation(libs.leakcanary)

    // use the new WorkInfo.stopReason (report stopReason), setNextScheduleTimeOverride (Precise scheduling), Configuration.Builder.setContentUriTriggerWorkersLimit (limit for content uri workers)
    implementation(libs.work.runtime)
    implementation(libs.biometric)
    implementation(libs.material)

    // Coil, Ktor, Okhttp, Zxing
    api(platform(libs.coil.bom))
    implementation(libs.coil)
    implementation(libs.coil.ktor)
    implementation(libs.coil.okhttp)
    implementation(libs.coil.compose)
    implementation(libs.zxing.core)
    implementation(libs.ktor.core)
    implementation(libs.ktor.okhttp)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.logging)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Koin
    api(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)
    implementation(libs.koin.compose)
    implementation(libs.koin.startup)
    implementation(libs.koin.annotations)
    ksp(libs.koin.compiler)

    // JSON, Markdown, LibSu
    implementation(libs.libsu.core)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.refine)
    compileOnly(libs.shizuku.hidden)
    implementation(libs.hiddenapi.bypass)
    implementation(libs.jackson.core)
    implementation(libs.serialization.json)
    implementation(libs.markdown)
    implementation(libs.compose.html)
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

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
    implementation(libs.compose.navigation3)
    implementation(libs.compose.navigation3.ui)
    implementation(libs.accompanist.permissions)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)

    // Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.junit.jupiter.params)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs =
            listOf("-Xjvm-default=all-compatibility", "-XXLanguage:+ExplicitBackingFields")
    }
}

fun detectLocales(): Set<String> {
    val langsList = mutableSetOf<String>()
    fileTree("src/main/res").visit {
        if (this.file.name == "strings.xml" && this.file.readText().contains("<string")) {
            val languageCode = this.file.parentFile?.name?.removePrefix("values-")?.let {
                if (it == "values") "en" else it
            }
            languageCode?.let { langsList.add(it) }
        }
    }
    return langsList
}