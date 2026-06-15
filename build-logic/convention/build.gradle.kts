plugins {
    `kotlin-dsl`
}

group = "com.appcasa.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
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
        register("androidCompose") {
            id = "appcasa.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidRoom") {
            id = "appcasa.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidTest") {
            id = "appcasa.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
    }
}
