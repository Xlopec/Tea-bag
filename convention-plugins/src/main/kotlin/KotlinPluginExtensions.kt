import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

@OptIn(ExperimentalWasmDsl::class)
fun KotlinMultiplatformExtension.enableUiTargets() {
    applyProjectHierarchyTemplate()
    
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }

    js {
        browser {
            testTask {
                useMocha {
                    timeout = "60s"
                }
            }
        }
    }

    wasmJs {
        browser {
            testTask {
                useMocha {
                    timeout = "60s"
                }
            }
        }
    }

    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private fun KotlinMultiplatformExtension.applyProjectHierarchyTemplate() {
    applyDefaultHierarchyTemplate {
        group("common") {
            group("nonWeb") {
                group("native")
                group("jvm") {
                    withJvm()
                }
            }
        }
    }
}
