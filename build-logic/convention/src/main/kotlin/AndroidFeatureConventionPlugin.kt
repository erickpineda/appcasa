import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("appcasa.android.library")
                apply("appcasa.android.hilt")
            }

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:domain"))
                // Add any common feature dependencies here if needed
            }
        }
    }
}
