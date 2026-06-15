package appcasa.android.config

import com.android.build.gradle.BaseExtension
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
  commonExtension: BaseExtension,
) {
  commonExtension.apply {
    compileSdkVersion(libs.findVersion("compileSdk").get().requiredVersion.toInt())

    defaultConfig {
      minSdk = libs.findVersion("minSdk").get().requiredVersion.toInt()
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }
  }
  
  when (commonExtension) {
    is ApplicationExtension -> commonExtension.defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
    is LibraryExtension -> commonExtension.defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
  }

  tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }
}
