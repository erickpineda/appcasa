plugins {
    id("appcasa.android.library")
    id("appcasa.android.test")
}

android {
    namespace = "com.appcasa.core.domain"
}

dependencies {
    implementation(libs.coroutines.android)
    implementation(libs.google.play.location)
    implementation("javax.inject:javax.inject:1")
}
