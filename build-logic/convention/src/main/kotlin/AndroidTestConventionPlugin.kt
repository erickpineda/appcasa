import appcasa.android.config.libs
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType

class AndroidTestConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      val extension = extensions.findByType<BaseExtension>()

      extension?.apply {
        testOptions {
          unitTests {
            isReturnDefaultValues = true
          }
        }
      }

      dependencies {
        add("testImplementation", libs.findLibrary("junit").get())
        add("testImplementation", libs.findLibrary("mockk").get())
        add("testImplementation", libs.findLibrary("coroutines-test").get())
      }
    }
  }
}
