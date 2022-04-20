import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.io.FileInputStream
import java.util.*

val composeVersion = "1.2.0-alpha08"

plugins {
    id("com.android.application").version("7.1.3")
    kotlin("android").version("1.6.10")
    kotlin("kapt").version("1.6.20")
    kotlin("plugin.serialization").version("1.6.20")
}

apply(plugin = "com.android.application")
apply(plugin = "kotlin-android")
apply(plugin = "kotlin-kapt")

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.looker.droidify"
        minSdk = 23
        targetSdk = 32
        versionCode = 903
        versionName = "0.9.0"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
            }
        }
    }

    sourceSets.forEach {
        val javaDir = it.java.srcDirs.find { it.name == "java" }
        it.java {
            srcDir(File(javaDir?.parentFile, "kotlin"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
        freeCompilerArgs = listOf("-Xjvm-default=compatibility")
    }

    buildFeatures {
        compose = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-alpha4"
            resValue("string", "application_name", "Neo Store-Debug")
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
        }
        create("neo") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".neo"
            versionNameSuffix = "-alpha4"
            resValue("string", "application_name", "Neo Store-alpha")
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
        }
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            resValue("string", "application_name", "Neo Store")
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
        all {
            isCrunchPngs = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard.pro"
            )
        }
    }
    packagingOptions {
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
                "/META-INF/**.version",
                "/okhttp3/internal/publicsuffix/*"
            )
        }
    }


    val keystorePropertiesFile = rootProject.file("keystore.properties")
    lint {
        disable += "InvalidVectorPath"
        warning += "InvalidPackage"
    }
    if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties()
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))

        val signing = object {
            val storeFile = keystoreProperties.getProperty("store.file")
            val storePassword = keystoreProperties.getProperty("store.password")
            val keyAlias = keystoreProperties.getProperty("key.alias")
            val keyPassword = keystoreProperties.getProperty("key.password")
        }

        if (signing.takeIf { it.storeFile != null && it.storePassword != null && it.keyAlias != null && it.keyPassword != null } != null) {
            signingConfigs {
                create("primary") {
                    storeFile = file(signing.storeFile)
                    storePassword = signing.storePassword
                    keyAlias = signing.keyAlias
                    keyPassword = signing.keyPassword
                    enableV2Signing = false
                }
            }

            buildTypes {
                named("debug") { signingConfig = signingConfigs["primary"] }
                named("release") { signingConfig = signingConfigs["primary"] }
            }
        }
    }
}

dependencies {

    // Core
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.20")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("androidx.fragment:fragment-ktx:1.5.0-beta01")
    implementation("androidx.activity:activity-ktx:1.5.0-beta01")
    implementation("androidx.activity:activity-compose:1.5.0-beta01")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.0-beta01")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.0-beta01")

    // Material3
    implementation("com.google.android.material:material:1.7.0-alpha01")

    // Coil
    implementation("io.coil-kt:coil:2.0.0-rc03")
    implementation("io.coil-kt:coil-compose:2.0.0-rc03")

    // OkHttps
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.6")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.4")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    // LibSu
    implementation("com.github.topjohnwu.libsu:core:3.2.1")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    // Markdown
    implementation("org.jetbrains:markdown:0.3.1")

    // Coroutines / Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0-beta01")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1")

    // Room
    implementation("androidx.room:room-runtime:2.4.2")
    implementation("androidx.room:room-ktx:2.4.2")
    implementation("androidx.room:room-rxjava3:2.4.2")
    kapt("androidx.room:room-compiler:2.4.2")

    // Compose
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.material3:material3:1.0.0-alpha10")
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation("com.google.android.material:compose-theme-adapter:1.1.6")
    implementation("com.google.android.material:compose-theme-adapter-3:1.0.6")

    debugImplementation ("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation ("androidx.compose.ui:ui-tooling-preview:$composeVersion")
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("src/main/res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField("String[]", "DETECTED_LOCALES", langsListString)
}
tasks.preBuild.dependsOn("detectAndroidLocals")