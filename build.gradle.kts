import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.jetbrains.kotlin.gradle.internal.KaptWithoutKotlincTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

val vCompose = "1.3.0-beta02"
val vComposeCompiler = "1.3.1"
val vAccompanist =
    "0.26.1-alpha" //Do not update until rememberDrawablePainter is fixed (try the IconShapeItem to confirm)
val vRoom = "2.5.0-alpha03"

plugins {
    id("com.android.application").version("7.3.0")
    kotlin("android").version("1.7.20")
    kotlin("kapt").version("1.7.20")
    kotlin("plugin.parcelize").version("1.7.20")
    id("com.google.protobuf").version("0.8.18")
}

allprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
}

val prebuiltsDir: String = "prebuilts/"

android {
    namespace = "com.android.launcher3"
    compileSdk = 33

    val name = "0.9.3-alpha2"
    val code = 931


    defaultConfig {
        minSdk = 26
        targetSdk = 32
        applicationId = "com.saggitt.omega"

        versionName = name
        versionCode = code

        buildConfigField("String", "BUILD_DATE", "\"${getBuildDate()}\"")
        buildConfigField("boolean", "ENABLE_AUTO_INSTALLS_LAYOUT", "false")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    applicationVariants.all { variant ->
        variant.outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "NeoLauncher_v${variant.versionName}_build_${variant.versionCode}.apk"
        }
        variant.resValue(
            "string",
            "launcher_component",
            "${variant.applicationId}/com.saggitt.omega.OmegaLauncher"
        )
        true
    }
    buildTypes {
        named("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
            signingConfig = signingConfigs.getByName("debug")
        }
        create("neo") {
            isMinifyEnabled = false
            applicationIdSuffix = ".neo"
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher_debug"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round_debug"
        }

        named("release") {
            isMinifyEnabled = false
            setProguardFiles(listOf("proguard-android-optimize.txt", "proguard.flags"))
            manifestPlaceholders["appIcon"] = "@mipmap/ic_launcher"
            manifestPlaceholders["appIconRound"] = "@mipmap/ic_launcher_round"
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("primary") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildFeatures {
        compose = true
        dataBinding = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = vComposeCompiler
    }

    kotlinOptions {
        jvmTarget = compileOptions.sourceCompatibility.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    packagingOptions {
        jniLibs {
            pickFirsts += listOf("**/libeasyBypass.so")
        }
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }

    // The flavor dimensions = aospWithoutQuickstep)
    // See: https://developer.android.com/studio/build/build-variants#flavor-dimensions = dimensions
    flavorDimensionList.clear()
    flavorDimensionList.addAll(listOf("app", "recents", "custom"))

    productFlavors {
        create("aosp") {
            dimension = "app"
            applicationId = "com.saggitt.omega"
            testApplicationId = "com.android.launcher3.tests"
        }

        create("withQuickstep") {
            dimension = "recents"
            minSdk = 26
        }

        create("withoutQuickstep") {
            dimension = "recents"
        }

        create("omega") {
            dimension = "custom"
        }
    }

    sourceSets {
        named("main") {
            res.srcDirs(listOf("res"))
            java.srcDirs(listOf("src", "src_plugins"))
            assets.srcDirs(listOf("assets"))
            manifest.srcFile("AndroidManifest-common.xml")
        }

        named("androidTest") {
            res.srcDirs(listOf("tests/res"))
            java.srcDirs(listOf("tests/src", "tests/tapl"))
            manifest.srcFile("tests/AndroidManifest-common.xml")
        }

        named("androidTestDebug") {
            java.srcDirs("tests/src_common")
            manifest.srcFile("tests/AndroidManifest.xml")
        }

        named("aosp") {
            java.srcDirs(listOf("src_flags", "src_shortcuts_overrides"))
        }

        named("withoutQuickstep") {
            java.srcDirs(listOf("src_ui_overrides"))
        }

        named("withQuickstep") {
            res.srcDirs(listOf("quickstep/res", "quickstep/recents_ui_overrides/res"))
            java.srcDirs(listOf("quickstep/src", "quickstep/recents_ui_overrides/src"))
            manifest.srcFile("quickstep/AndroidManifest.xml")
        }

        named("omega") {
            res.srcDirs(listOf("Omega/res"))
            java.srcDirs(listOf("Omega/src", "Omega/src_ui_overrides"))
            aidl.srcDirs(listOf("Omega/aidl"))
            manifest.srcFile("Omega/AndroidManifest.xml")
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable += listOf("MissingTranslation", "ExtraTranslation")
    }

    addFrameworkJar("framework-12.jar")
}

dependencies {
    implementation(project(":iconloaderlib"))
    implementation(project(":searchuilib"))

    //UI
    implementation("androidx.appcompat:appcompat:1.6.0-rc01")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03")
    implementation("androidx.activity:activity-ktx:1.6.0-rc02")
    implementation("androidx.fragment:fragment-ktx:1.5.2")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.savedstate:savedstate-ktx:1.2.0")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.android.material:material:1.8.0-alpha01")
    implementation("com.jaredrummler:colorpicker:1.1.0")

    // Libs
    implementation("com.github.farmerbb:libtaskbar:2.2.0")
    implementation("com.github.ChickenHook:RestrictionBypass:2.2")
    implementation("io.github.hokofly:hoko-blur:1.3.7")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")
    implementation("com.google.protobuf:protobuf-javalite:3.21.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation(kotlin("stdlib", "1.7.20"))
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("com.github.KwabenBerko:OpenWeatherMap-Android-Library:2.0.1") {
        exclude("com.android.support", "support-compat")
        exclude("com.android.support", "appcompat-v7")
    }

    //Compose
    implementation("androidx.compose.compiler:compiler:$vComposeCompiler")
    implementation("androidx.compose.runtime:runtime:$vCompose")
    implementation("androidx.compose.ui:ui:$vCompose")
    implementation("androidx.compose.ui:ui-tooling:$vCompose")
    implementation("androidx.compose.ui:ui-tooling-preview:$vCompose")
    implementation("androidx.compose.foundation:foundation:$vCompose")
    implementation("androidx.compose.material3:material3:1.0.0-beta02")
    implementation("androidx.navigation:navigation-compose:2.5.2")
    implementation("androidx.activity:activity-compose:1.5.1")
    implementation("io.coil-kt:coil-compose:2.2.1")
    implementation("io.github.fornewid:material-motion-compose-core:0.8.4")
    implementation("com.google.android.material:compose-theme-adapter-3:1.0.18")

    //Accompanist
    implementation("com.google.accompanist:accompanist-flowlayout:$vAccompanist")
    implementation("com.google.accompanist:accompanist-insets-ui:$vAccompanist")
    implementation("com.google.accompanist:accompanist-navigation-animation:$vAccompanist")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$vAccompanist")
    implementation("com.google.accompanist:accompanist-drawablepainter:$vAccompanist")
    implementation("com.google.accompanist:accompanist-pager:$vAccompanist")
    implementation("com.google.accompanist:accompanist-pager-indicators:$vAccompanist")

    //Room Components
    implementation("androidx.room:room-runtime:$vRoom")
    implementation("androidx.room:room-ktx:$vRoom")
    kapt("androidx.room:room-compiler:$vRoom")

    // Recents lib dependency
    "withQuickstepImplementation"(project(":SystemUIShared"))
    implementation(
        fileTree(
            baseDir = "${prebuiltsDir}/libs"
        ).include(
            "wm_shell-aidls.jar"
        )
    )
    implementation(
        fileTree(
            baseDir = "${prebuiltsDir}/libs"
        ).include(
            "sysui_statslog.jar"
        )
    )

    // Required for AOSP to compile. This is already included in the sysui_shared.jar
    "withoutQuickstepImplementation"(
        fileTree(baseDir = "${prebuiltsDir}/libs").include(
            "plugin_core.jar"
        )
    )

    protobuf(files("protos/"))
    protobuf(files("quickstep/protos_overrides/"))

    testImplementation("junit:junit:4.13.2")
    implementation("junit:junit:4.13.2")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")

    androidTestImplementation("org.mockito:mockito-core:3.12.4")
    androidTestImplementation("com.google.dexmaker:dexmaker:1.2")
    androidTestImplementation("com.google.dexmaker:dexmaker-mockito:1.2")
    androidTestImplementation("androidx.annotation:annotation:1.4.0")
}

protobuf {
    // Configure the protoc executable
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// using a task as a preBuild dependency instead of a function that takes some time insures that it runs
task("detectAndroidLocals") {
    val langsList: MutableSet<String> = HashSet()

    // in /res are (almost) all languages that have a translated string is saved. this is safer and saves some time
    fileTree("res").visit {
        if (this.file.path.endsWith("strings.xml")
            && this.file.canonicalFile.readText().contains("<string")
        ) {
            var languageCode = this.file.parentFile.name.replace("values-", "")
            languageCode = if (languageCode == "values") "en" else languageCode
            langsList.add(languageCode)
        }
    }
    val langsListString = "{${langsList.joinToString(",") { "\"${it}\"" }}}"
    android.defaultConfig.buildConfigField(
        "String[]",
        "DETECTED_ANDROID_LOCALES",
        langsListString
    )
}
tasks.preBuild.dependsOn("detectAndroidLocals")

@SuppressWarnings(
    "UnnecessaryQualifiedReference",
    "SpellCheckingInspection",
    "GroovyUnusedDeclaration"
)
// Returns the build date in a RFC3339 compatible format. TZ is always converted to UTC
fun getBuildDate(): String {
    val RFC3339_LIKE = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    RFC3339_LIKE.timeZone = TimeZone.getTimeZone("UTC")
    return RFC3339_LIKE.format(Date())
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
        tasks.withType<KaptWithoutKotlincTask> {
            classpath.from(files(listOf(frameworkJar) + (classpath.files as Iterable<File>)))
        }
    }
}
