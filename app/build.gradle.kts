plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ksp)
  // alias(libs.plugins.google.services) // Requiere el archivo google-services.json en la carpeta /app
}

android {
  namespace   = "com.appcasa"
  compileSdk  = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    applicationId    = "com.appcasa"
    minSdk           = libs.versions.minSdk.get().toInt()
    targetSdk        = libs.versions.targetSdk.get().toInt()
    versionCode      = 1
    versionName      = "1.0.0"
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }

  // Exportar esquema Room para control de versiones y tests
  ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
  }
}

dependencies {
  // ─── Core Android ─────────────────────────────────────
  implementation(libs.androidx.core.ktx)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.activity.compose)
  implementation(libs.google.material)
  implementation(libs.androidx.biometric)

  // ─── Compose BOM ──────────────────────────────────────
  implementation(platform(libs.compose.bom))
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.graphics)
  implementation(libs.compose.ui.tooling.preview)
  implementation(libs.compose.material3)
  implementation(libs.compose.material.icons)
  debugImplementation(libs.compose.ui.tooling)

  // ─── Navigation ───────────────────────────────────────
  implementation(libs.navigation.compose)

  // ─── Hilt ─────────────────────────────────────────────
  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)
  implementation(libs.hilt.navigation.compose)

  // ─── Room ─────────────────────────────────────────────
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  ksp(libs.room.compiler)

  // ─── DataStore ────────────────────────────────────────
  implementation(libs.datastore.preferences)

  // ─── WorkManager ──────────────────────────────────────
  implementation(libs.workmanager.ktx)

  // ─── Coil ─────────────────────────────────────────────
  implementation(libs.coil.compose)

  // ─── Retrofit + OkHttp ────────────────────────────────
  implementation(libs.retrofit)
  implementation(libs.retrofit.gson)
  implementation(libs.okhttp.logging)

  // ─── Coroutines ───────────────────────────────────────
  implementation(libs.coroutines.android)

  // ─── Firebase ─────────────────────────────────────────
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.messaging)

  // ─── Módulos Locales ──────────────────────────────────
  implementation(project(":core:ui"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":feature:dashboard"))
  implementation(project(":feature:tasks"))
  implementation(project(":feature:finance"))
  implementation(project(":feature:inventory"))
  implementation(project(":feature:calendar"))
  implementation(project(":feature:settings"))
}
