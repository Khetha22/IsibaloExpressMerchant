plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "za.co.hbtechinc.isibalo"
    compileSdk = 36
    defaultConfig {
        applicationId = "za.co.hbtechinc.isibalo"
        minSdk = 25          // Android 7.1+. MediaPipe GenAI needs 24+
        targetSdk = 36
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

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.JETBRAINS)
    }
}

dependencies {
    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    // Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // AndroidX
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Room (offline-first DB)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    // Background sync
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // On-device AI (offline)
    implementation("com.google.mediapipe:tasks-genai:0.10.35")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
}
