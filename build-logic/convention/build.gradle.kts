plugins {
    `kotlin-dsl`
}

group = "com.appcasa.buildlogic"

dependencies {
    compileOnly(libs.plugins.android.application)
    compileOnly(libs.plugins.android.library)
    compileOnly(libs.plugins.kotlin.android)
    compileOnly(libs.plugins.kotlin.compose)
    compileOnly(libs.plugins.hilt)
    compileOnly(libs.plugins.ksp)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "appcasa.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "appcasa.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "appcasa.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "appcasa.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
