plugins {
  id("appcasa.android.application")
  id("appcasa.android.compose")
  id("appcasa.android.hilt")
  id("appcasa.android.test")
  alias(libs.plugins.google.services)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.appcasa"

  defaultConfig {
    applicationId = "com.appcasa"
    versionCode   = 1
    versionName   = "1.0.0"
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

  // ─── Compose Extras ───────────────────────────────────
  implementation(libs.compose.material.icons)
  debugImplementation(libs.compose.ui.tooling)

  // ─── Navigation ───────────────────────────────────────
  implementation(libs.navigation.compose)

  // ─── Hilt Extras ──────────────────────────────────────
  implementation(libs.hilt.navigation.compose)
  implementation(libs.hilt.work)
  ksp(libs.hilt.work.compiler)

  // ─── Room ─────────────────────────────────────────────
  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.sqlcipher.android)
  implementation(libs.sqlite.ktx)
  ksp(libs.room.compiler)

  // ─── DataStore ────────────────────────────────────────
  implementation(libs.datastore.preferences)

  // ─── WorkManager ──────────────────────────────────────
  implementation(libs.workmanager.ktx)

  // ─── Coil ─────────────────────────────────────────────
  implementation(libs.coil.compose)

  // ─── Coroutines ───────────────────────────────────────
  implementation(libs.coroutines.android)

  // ─── Firebase ─────────────────────────────────────────
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.messaging)
  implementation(libs.firebase.analytics)
  implementation(libs.kotlinx.serialization.json)

  // ─── Módulos Locales ──────────────────────────────────
  implementation(project(":core:ui"))
  implementation(project(":core:domain"))
  implementation(project(":core:data"))
  implementation(project(":feature:dashboard"))
  implementation(project(":feature:family"))
  implementation(project(":feature:lists"))
  implementation(project(":feature:tasks"))
  implementation(project(":feature:finance"))
  implementation(project(":feature:inventory"))
  implementation(project(":feature:calendar"))
  implementation(project(":feature:settings"))
}
