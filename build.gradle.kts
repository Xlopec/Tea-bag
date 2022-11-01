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

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

// Top-level build file where you can add configuration options common to all subprojects/modules.

installGitHooks()

plugins {
    kotlin("jvm")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
    id("io.github.gradle-nexus.publish-plugin")
}

version = libraryVersion.toVersionName()
group = "io.github.xlopec"

nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(SonatypeProfileId)
            nexusUrl.set(NexusUrl)
            snapshotRepositoryUrl.set(SnapshotNexusUrl)
            username.set(project.ossrhUser)
            password.set(project.ossrhPassword)
        }
    }

    useStaging.set(!libraryVersion.isSnapshot)
}

allprojects {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }

    apply {
        plugin("io.gitlab.arturbosch.detekt")
    }

    optIn(DefaultOptIns)

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
    }

    afterEvaluate {
        tasks.withType<Test>().configureEach {
            configureOutputLocation(htmlTestReportsDir, xmlTestReportsDir)
        }
    }
}

val detektAll by tasks.registering(Detekt::class) {
    description = "Runs analysis task over whole codebase"
    debug = false
    parallel = true
    ignoreFailures = false
    disableDefaultRuleSets = false
    buildUponDefaultConfig = true
    setSource(files(projectDir))
    config.setFrom(detektConfig)
    baseline.set(detektBaseline)

    include("**/*.kt", "**/*.kts")
    exclude("compose-jetbrains-theme/**", "resources/", "**/build/**", "**/test/java/**")

    reports {
        xml.required.set(false)
        txt.required.set(false)
        html.required.set(true)
    }
}

val detektProjectBaseline by tasks.registering(DetektCreateBaselineTask::class) {
    ignoreFailures.set(true)
    parallel.set(true)
    setSource(files(rootDir))
    config.setFrom(detektConfig)
    baseline.set(detektBaseline)
    include("**/*.kt", "**/*.kts")
    exclude("compose-jetbrains-theme/**", "**/resources/**", "**/build/**")
}

val detektFormat by tasks.registering(Detekt::class) {
    parallel = true
    autoCorrect = true
    ignoreFailures = false
    setSource(files(projectDir))

    include("**/*.kt", "**/*.kts")
    exclude("compose-jetbrains-theme/**", "**/resources/**", "**/build/**")

    config.setFrom(detektConfig)
    baseline.set(detektBaseline)
}
