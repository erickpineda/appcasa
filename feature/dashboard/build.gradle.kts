plugins {
    id("appcasa.android.feature")
    id("appcasa.android.compose")
}

android {
    namespace = "com.appcasa.feature.dashboard"
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
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)
    
    // WorkManager
    implementation(libs.workmanager.ktx)
    
    // Biometric
    implementation(libs.androidx.biometric)

    // ML Kit
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.mlkit.text.recognition)
    implementation(libs.zxing.core)

    // Módulos Core
    implementation(project(":core:data"))

    // Features para el Hub de Gestión
    implementation(project(":feature:tasks"))
    implementation(project(":feature:finance"))
    implementation(project(":feature:family"))
    implementation(project(":feature:lists"))
}
