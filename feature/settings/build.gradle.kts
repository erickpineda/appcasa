plugins {
    id("appcasa.android.feature")
    id("appcasa.android.compose")
}

android {
    namespace = "com.appcasa.feature.settings"
}

dependencies {
    implementation(libs.coroutines.android)

    // Compose Extras
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.coil.compose)

    // Hilt Extras
    implementation(libs.hilt.navigation.compose)

    // CameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.google.play.auth)

    // ML Kit Barcode
    implementation(libs.mlkit.barcode.scanning)

    // Módulos Core
    implementation(project(":core:data"))
}
