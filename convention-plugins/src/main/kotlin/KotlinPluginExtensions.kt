import org.gradle.kotlin.dsl.get
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
