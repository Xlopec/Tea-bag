/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
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

import java.io.File
import java.net.URL
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.GradleDokkaSourceSetBuilder
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val DefaultOptIns = listOf(
    "kotlin.RequiresOptIn",
    "kotlin.ExperimentalStdlibApi",
    "kotlin.contracts.ExperimentalContracts",
)

fun Project.installGitHooks() = afterEvaluate {
    projectHooksDir.listFiles { f -> f.extension == "sh" }
        ?.forEach { f ->
            val target = File(gitHooksDir, f.nameWithoutExtension)

            f.copyTo(target, overwrite = true)
            target.setExecutable(true, false)
        }
}

fun Project.sourceSetDir(
    sourceSetName: String
): File = file("src/$sourceSetName/kotlin")

fun KotlinMultiplatformExtension.optIn(
    vararg annotationNames: String
) {
    optIn(listOf(*annotationNames))
}

fun KotlinMultiplatformExtension.optIn(
    annotationNames: Iterable<String>
) {
    sourceSets.all {
        languageSettings {
            annotationNames.forEach(::optIn)
        }
    }
}

fun Project.optIn(
    vararg annotationNames: String
) = optIn(listOf(*annotationNames))

fun Project.optIn(
    annotationNames: Iterable<String>
) = afterEvaluate {
    if (hasKotlinMultiplatformPlugin) {
        extensions.findByType<KotlinMultiplatformExtension>()?.optIn(annotationNames)
    } else {
        tasks.withType<KotlinCompile>().all {
            kotlinOptions {
                @Suppress("SuspiciousCollectionReassignment")
                freeCompilerArgs += annotationNames.map { "-opt-in=$it" }
            }
        }
    }
}

fun GradleDokkaSourceSetBuilder.linkSourcesForSourceSet(
    project: Project,
    sourceSetName: String
) = sourceLink {
    localDirectory.set(project.file("src/$sourceSetName/kotlin"))
    remoteUrl.set(URL("https://github.com/Xlopec/Tea-bag/tree/$branchOrDefault/${project.name}/src/$sourceSetName/kotlin"))
    remoteLineSuffix.set("#L")
}

fun Provider<out Task>.dependsOn(
    vararg other: String
): Task = get().dependsOn(*other)
