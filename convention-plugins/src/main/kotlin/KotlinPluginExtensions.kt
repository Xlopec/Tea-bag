/*
 * MIT License
 *
 * Copyright (c) 2026. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
        browser()
    }

    macosArm64()
    iosArm64()
    iosSimulatorArm64()
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
private fun KotlinMultiplatformExtension.applyProjectHierarchyTemplate() {
    applyDefaultHierarchyTemplate {
        common {
            group("nonWeb") {
                group("native")
                group("jvm") {
                    withJvm()
                }
            }
        }
    }
}
