plugins {
    id("appcasa.android.feature")
    id("appcasa.android.compose")
}

android {
    namespace = "com.appcasa.feature.calendar"
}

dependencies {
    implementation(libs.coroutines.android)

    // Compose Extras
    implementation(libs.compose.material.icons)
    debugImplementation(libs.compose.ui.tooling)

    // Navigation
    implementation(libs.navigation.compose)

    // Módulos Core
    implementation(project(":core:data"))
}
