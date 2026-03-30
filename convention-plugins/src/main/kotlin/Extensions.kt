import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.PublishArtifact
import org.gradle.api.artifacts.dsl.ArtifactHandler
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun KotlinMultiplatformExtension.enableAllTargets() {
    enableUiTargets()
    // additional targets
    linuxX64()
    linuxArm64()
    mingwX64()
    watchosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    tvosSimulatorArm64()
    tvosArm64()
}

fun KotlinMultiplatformExtension.enableUiTargets() {
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

internal fun Project.signing(configure: Action<org.gradle.plugins.signing.SigningExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("signing", configure)

internal val TaskContainer.classes: TaskProvider<org.gradle.api.DefaultTask>
    get() = named<org.gradle.api.DefaultTask>("classes")

internal fun ArtifactHandler.archives(artifactNotation: Any): PublishArtifact =
    add("archives", artifactNotation)

internal fun Project.kotlinMultiplatform(configure: Action<KotlinMultiplatformExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlin", configure)

internal fun Project.kotlinJvm(configure: Action<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("kotlin", configure)

internal fun Project.publishing(configure: Action<org.gradle.api.publish.PublishingExtension>): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)
