plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles.add(file("consumer-rules.pro"))
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
        create("neo") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }

    addFrameworkJar("framework-12.jar")
    namespace = "app.lawnchair.compatlib.twelve"
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation(project(":CompatLib"))
}

fun Project.addFrameworkJar(path: String) {
    val frameworkJar = File(rootProject.projectDir, "prebuilts/libs/$path")
    if (!frameworkJar.exists()) {
        throw IllegalArgumentException("Framework jar path doesn't exist")
    }
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            options.bootstrapClasspath =
                files(listOf(frameworkJar) + (options.bootstrapClasspath?.files as Iterable<File>))
        }
        tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask> {
            classpath.from(files(listOf(frameworkJar) + (classpath.files as Iterable<File>)))
        }
    }
}