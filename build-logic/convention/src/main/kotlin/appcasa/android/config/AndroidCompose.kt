package appcasa.android.config

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: BaseExtension,
) {
    commonExtension.apply {
        buildFeatures.compose = true
    }

    dependencies {
        val bom = libs.findLibrary("compose-bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        add("implementation", libs.findLibrary("compose-ui").get())
        add("implementation", libs.findLibrary("compose-ui-graphics").get())
        add("implementation", libs.findLibrary("compose-ui-tooling-preview").get())
        add("implementation", libs.findLibrary("compose-material3").get())
    }
}
