plugins {
    id("appcasa.android.library")
    id("appcasa.android.hilt")
    id("appcasa.android.room")
}

android {
    namespace = "com.appcasa.core.data"
}

dependencies {
    implementation(libs.androidx.security.crypto)
    implementation(libs.coroutines.android)
    
    // Hilt Extras
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // WorkManager
    implementation(libs.workmanager.ktx)

    // Location
    implementation(libs.google.play.location)

    // ML Kit
    implementation(libs.mlkit.barcode.scanning)
    implementation(libs.mlkit.text.recognition)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.storage)

    // Domain
    implementation(project(":core:domain"))
}
