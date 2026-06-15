import appcasa.android.config.configureKotlinAndroid
import appcasa.android.config.libs
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      with(pluginManager) {
        apply("com.android.application")
        apply("org.jetbrains.kotlin.android")
      }

      extensions.configure<AppExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
      }

      dependencies {
        add("implementation", libs.findLibrary("androidx-core-ktx").get())
      }
    }
  }
}
