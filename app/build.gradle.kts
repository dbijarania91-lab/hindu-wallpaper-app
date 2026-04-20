plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    kotlin("kapt")
}

android {
    namespace = "com.mahiinfo.hinduwallpaper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mahiinfo.hinduwallpaper"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // AdMob App ID — replace with your real ID from AdMob dashboard
        manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713"

        // Supabase config from local.properties or CI secrets
        buildConfigField("String", "SUPABASE_URL", "\"${project.findProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${project.findProperty("SUPABASE_ANON_KEY") ?: ""}\"")
    }

    signingConfigs {
        create("release") {
            val keystorePath = project.findProperty("KEYSTORE_PATH") as String? ?: ""
            if (keystorePath.isNotEmpty()) {
                storeFile = file(keystorePath)
                storePassword = project.findProperty("KEYSTORE_PASSWORD") as String? ?: ""
                keyAlias = project.findProperty("KEY_ALIAS") as String? ?: ""
                keyPassword = project.findProperty("KEY_PASSWORD") as String? ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val keystorePath = project.findProperty("KEYSTORE_PATH") as String? ?: ""
            if (keystorePath.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // DI
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Network
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Supabase BOM — manages all supabase lib versions together
    implementation(platform("io.github.jan-tennert.supabase:bom:2.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation(libs.ktor.client.okhttp)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.video)

    // Video
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.ui)

    // Local DB & Prefs
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)
    implementation(libs.datastore.preferences)

    // AdMob
    implementation(libs.admob)

    // UI extras
    implementation(libs.lottie.compose)
    implementation(libs.accompanist.permissions)
    implementation(libs.palette)

    // Background
    implementation(libs.workmanager)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

kapt { correctErrorTypes = true }
