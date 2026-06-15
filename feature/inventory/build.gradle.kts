plugins {
    id("appcasa.android.feature")
    id("appcasa.android.compose")
    id("appcasa.android.test")
}

android {
    namespace = "com.appcasa.feature.inventory"
}

dependencies {
    implementation(libs.coroutines.android)

    // Compose Extras
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt Extras
    implementation(libs.hilt.navigation.compose)
    
    // ML Kit
    implementation(libs.mlkit.barcode.scanning)

    // Módulos Core
    implementation(project(":core:data"))
}
