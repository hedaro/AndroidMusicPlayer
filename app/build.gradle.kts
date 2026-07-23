import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Release signing credentials, loaded from a git-ignored keystore.properties if present.
// Absent (dev machines, CI without secrets) => release builds are simply left unsigned.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}

// Name build outputs "MusicPlayer-<buildType>.apk" instead of the default "app-<buildType>.apk".
base {
    archivesName.set("MusicPlayer")
}

android {
    namespace = "com.hedaro.musicplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hedaro.musicplayer"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Sign with the release key when keystore.properties is present; otherwise unsigned.
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    // --- Ad-readiness (planned, intentionally NOT enabled in v1) ---------------
    // When/if the app is published with strictly non-intrusive banner ads, split
    // the build into a permanently ad-free personal flavor and an ad-carrying one.
    // No ad SDK is present today; enabling ads = uncomment, add the AdMob dependency
    // to `withAds`, and provide an AdMobAdProvider (see ads/ package).
    //
    // flavorDimensions += "ads"
    // productFlavors {
    //     create("free")    { dimension = "ads" }          // no ads, ever
    //     create("withAds") { dimension = "ads" }          // banner ads for Play Store
    // }
    // ---------------------------------------------------------------------------

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core / lifecycle / activity
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Compose (BOM keeps all Compose artifacts on compatible versions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Media3 — playback backbone (ExoPlayer + MediaSession)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)

    // Room — playlist persistence
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt — dependency injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coil — album art
    implementation(libs.coil.compose)

    // DataStore — app settings (theme preference)
    implementation(libs.androidx.datastore.preferences)

    // Splash screen (hold until theme preference loads)
    implementation(libs.androidx.core.splashscreen)

    // Drag-to-reorder playlist tracks
    implementation(libs.reorderable)
}
