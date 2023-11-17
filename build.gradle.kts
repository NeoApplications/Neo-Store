import com.android.build.gradle.internal.tasks.factory.dependsOn

val vActivity = "1.8.0"
val vCoil = "2.5.0"
val vComposeCompiler = "1.5.3"
val vCompose = "1.5.4"
val vComposeAccompanist = "0.32.0"
val vComposeHtml = "1.5.0"
val vComposeMaterial3 = "1.1.2"
val vCoroutines = "1.7.3"
val vJackson = "2.15.2"
val vKoin = "3.5.0"
val vKoinKsp = "1.3.0"
val vKotlin = "1.9.10"
val vKSP = "1.0.13"
val vKtor = "2.3.6"
val vLibsu = "5.2.1"
val vLifecycle = "2.6.2"
val vMarkdown = "0.5.2"
val vMaterial = "1.10.0"
val vMoshi = "1.15.0"
val vNavigation = "2.7.5"
val vOkhttp = "5.0.0-alpha.11"
val vPreference = "1.2.1"
val vRoom = "2.6.0"
val vSerialization = "1.6.0"
val vSimpleStorage = "1.5.5"
val vWork = "2.9.0-rc01"
val vZXing = "3.5.2"

plugins {
    id("com.android.application") version ("8.1.3")
    kotlin("android") version ("1.9.10")
    kotlin("plugin.serialization") version ("1.9.10")
    id("com.google.devtools.ksp") version ("1.9.10-1.0.13")
}

android {
    namespace = "com.machiav3lli.fdroid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.machiav3lli.fdroid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1006
        versionName = "1.0.0-alpha07"
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

    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }

    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "Neo_Store_${variant.name}_${variant.versionName}.apk"
        }
        true
    }

    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            resValue("string", "application_name", "Neo Store-Debug")
        }
        create("neo") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".neo"
            resValue("string", "application_name", "Neo Store-beta")
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
                "/META-INF/**.version",
                "/okhttp3/internal/publicsuffix/*"
            )
        }
    }

    lint {
        disable += "InvalidVectorPath"
        warning += "InvalidPackage"
    }
}

dependencies {

    // Core
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$vKotlin")
    implementation("com.google.devtools.ksp:symbol-processing-api:$vKotlin-$vKSP")
    implementation("androidx.preference:preference-ktx:$vPreference")
    implementation("androidx.activity:activity-compose:$vActivity")
    implementation("androidx.work:work-runtime:$vWork")

    // Material3
    implementation("com.google.android.material:material:$vMaterial")

    // Coil
    implementation("io.coil-kt:coil:$vCoil")
    implementation("io.coil-kt:coil-compose:$vCoil")

    // ZXing
    implementation("com.google.zxing:core:$vZXing")

    // Koin
    implementation("io.insert-koin:koin-android:$vKoin")
    implementation("io.insert-koin:koin-androidx-workmanager:$vKoin")
    implementation("io.insert-koin:koin-annotations:$vKoinKsp")
    ksp("io.insert-koin:koin-ksp-compiler:$vKoinKsp")

    // Ktor
    implementation("io.ktor:ktor-client-core:$vKtor")
    implementation("io.ktor:ktor-client-okhttp:$vKtor")
    implementation("io.ktor:ktor-client-logging:$vKtor")

    // OkHttps
    implementation("com.squareup.okhttp3:okhttp:$vOkhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:$vOkhttp")

    // LibSu
    implementation("com.github.topjohnwu.libsu:core:$vLibsu")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-core:$vJackson")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$vSerialization")

    // Markdown
    implementation("org.jetbrains:markdown:$vMarkdown")
    implementation("de.charlex.compose:html-text:$vComposeHtml")

    // Storage
    implementation("com.anggrayudi:storage:$vSimpleStorage")

    // Coroutines / Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$vLifecycle")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$vCoroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$vCoroutines")

    // Room
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    ksp("androidx.room:room-compiler:$vRoom")

    // Compose
    implementation("androidx.compose.runtime:runtime:$vCompose")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.foundation:foundation:$vCompose")
    implementation("androidx.compose.runtime:runtime-livedata:$vCompose")
    implementation("androidx.compose.material3:material3:$vComposeMaterial3")
    implementation("androidx.compose.animation:animation:$vCompose")
    implementation("androidx.navigation:navigation-compose:$vNavigation")
    implementation("com.google.accompanist:accompanist-permissions:$vComposeAccompanist")

    debugImplementation("androidx.compose.ui:ui-tooling:$vCompose")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:$vCompose")
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