import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// ---------------------------------------------------------------------------
// Version management
// versionCode: derived from git commit count (auto-increments on every commit)
// versionName: derived from the latest git tag (e.g. v1.2.3 → 1.2.3),
//              falls back to "1.0.0-SNAPSHOT" when no tag is found
// ---------------------------------------------------------------------------
fun gitVersionCode(): Int {
    return try {
        val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText().trim().toIntOrNull() ?: 1
    } catch (e: Exception) {
        1
    }
}

fun gitVersionName(): String {
    return try {
        val tagProcess = ProcessBuilder("git", "describe", "--tags", "--exact-match", "HEAD")
            .redirectErrorStream(true)
            .start()
        val tag = tagProcess.inputStream.bufferedReader().readText().trim()
        if (tag.isNotEmpty()) {
            tag.removePrefix("v")
        } else {
            val descProcess = ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
                .redirectErrorStream(true)
                .start()
            val nearestTag = descProcess.inputStream.bufferedReader().readText().trim()
            if (nearestTag.isNotEmpty()) {
                "${nearestTag.removePrefix("v")}-SNAPSHOT"
            } else {
                "1.0.0-SNAPSHOT"
            }
        }
    } catch (e: Exception) {
        "1.0.0-SNAPSHOT"
    }
}

// ---------------------------------------------------------------------------
// Signing configuration
// In CI, keystore.properties is generated from GitHub Secrets.
// Locally, copy keystore.properties.template → keystore.properties and fill in.
// ---------------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.cancleeric.dominoblockade"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cancleeric.dominoblockade"
        minSdk = 26
        targetSdk = 34
        versionCode = gitVersionCode()
        versionName = gitVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.datastore.preferences)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
