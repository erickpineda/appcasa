plugins {
    id("appcasa.android.feature")
    id("appcasa.android.compose")
    id("appcasa.android.test")
}

android {
    namespace = "com.appcasa.feature.finance"
}

dependencies {
    implementation(libs.androidx.biometric)
    implementation(libs.coroutines.android)

    // Compose Extras
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.coil.compose)

    // Hilt Extras
    implementation(libs.hilt.navigation.compose)
    
    // ML Kit
    implementation(libs.mlkit.text.recognition)

    // Módulos Core
    implementation(project(":core:data"))

    testImplementation(libs.mlkit.text.recognition)
}
