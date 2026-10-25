plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "za.co.hbtechinc.isibalo"
    compileSdk = 37
    defaultConfig {
        applicationId = "za.co.hbtechinc.isibalo"
        minSdk = 25          // Android 7.1+. MediaPipe GenAI needs 24+
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SYNC_URL", "\"https://hbtechinc.co.za/isibalo/api/sync.php\"")
        buildConfigField("String", "SYNC_API_KEY", "\"CHANGE_ME\"")
    }
    buildTypes {
        release { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true; buildConfig = true }
    packaging { jniLibs { useLegacyPackaging = true } }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Icons are frozen at 1.7.8 (not versioned by the BOM anymore)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // AndroidX
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")

    // Room (offline-first DB)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    // Background sync
    implementation("androidx.work:work-runtime-ktx:2.12.0")

    // On-device AI (offline)
    implementation("com.google.mediapipe:tasks-genai:0.10.35")
}
