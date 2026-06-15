plugins {
    id("appcasa.android.library")
    id("appcasa.android.compose")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.appcasa.core.ui"
}

dependencies {
    implementation(libs.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    // Compose Extras
    implementation(libs.compose.material.icons)
    implementation(libs.lottie.compose)
    implementation(libs.zxing.core)
    debugImplementation(libs.compose.ui.tooling)

    // Domain
    implementation(project(":core:domain"))
}
