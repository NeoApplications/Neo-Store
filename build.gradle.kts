import com.android.build.gradle.internal.tasks.factory.dependsOn

val activityVersion = "1.8.0-rc01"
val coilVersion = "2.4.0"
val composeCompilerVersion = "1.5.3"
val composeVersion = "1.5.1"
val composeAccompanistVersion = "0.32.0"
val composeHtmlVersion = "1.5.0"
val composeMaterial3Version = "1.1.2"
val jacksonVersion = "2.15.2"
val koinVersion = "3.5.0"
val koinKspVersion = "1.3.0"
val kotlinVersion = "1.9.10"
val kSerializationVersion = "1.6.0"
val kspVersion = "1.0.13"
val ktorVersion = "2.3.4"
val libsuVersion = "5.2.1"
val lifecycleVersion = "2.6.2"
val materialVersion = "1.9.0"
val navigationVersion = "2.7.3"
val okhttpVersion = "5.0.0-alpha.11"
val markdownVersion = "0.5.1"
val moshiVersion = "1.15.0"
val roomVersion = "2.6.0-rc01"
val simpleStorageVersion = "1.5.5"
val coroutinesVersion = "1.7.3"

plugins {
    id("com.android.application") version ("8.1.1")
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
        targetSdk = 33
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
        kotlinCompilerExtensionVersion = composeCompilerVersion
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kotlinVersion-$kspVersion")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.activity:activity-compose:$activityVersion")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    // Material3
    implementation("com.google.android.material:material:$materialVersion")

    // Coil
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")

    // Koin
    implementation("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-androidx-workmanager:$koinVersion")
    implementation("io.insert-koin:koin-annotations:$koinKspVersion")
    ksp("io.insert-koin:koin-ksp-compiler:$koinKspVersion")

    // Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

    // OkHttps
    implementation("com.squareup.okhttp3:okhttp:$okhttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okhttpVersion")

    // LibSu
    implementation("com.github.topjohnwu.libsu:core:$libsuVersion")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kSerializationVersion")

    // Markdown
    implementation("org.jetbrains:markdown:$markdownVersion")
    implementation("de.charlex.compose:html-text:$composeHtmlVersion")

    // Storage
    implementation("com.anggrayudi:storage:$simpleStorageVersion")

    // Coroutines / Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // Room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Compose
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.runtime:runtime-livedata:$composeVersion")
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.animation:animation:$composeVersion")
    implementation("androidx.navigation:navigation-compose:$navigationVersion")
    implementation("com.google.accompanist:accompanist-permissions:$composeAccompanistVersion")

    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
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