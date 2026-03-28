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

plugins {
    alias(libs.plugins.kotlin.android.temp)
    alias(libs.plugins.android.library)
}

android {
    compileSdk = 36
    namespace = "io.github.xlopec.shared.remote"
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        consumerProguardFile("proguard-rules.pro")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        api(project(":tea-time-travel"))
        api(project(":tea-time-travel-adapter-gson"))
        api(project(":samples:shared-app-lib"))
        implementation(libs.ktor.http)
        implementation(libs.gson)
        testImplementation(libs.kotlin.test)
        testImplementation(libs.junit)
        coreLibraryDesugaring(libs.desugar.jdk)
    }
}

kotlin {
    explicitApi()

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        optIn.addAll(
            DefaultOptIns +
                "kotlinx.serialization.ExperimentalSerializationApi" +
                "io.github.xlopec.tea.core.ExperimentalTeaApi",
        )
    }
}

val allTests by tasks.registering(Task::class) {
    dependsOn("test")
}

tasks.withType<Test>().configureEach {
    configureOutputLocation(testReportsDir(name, "html"), testReportsDir(name, "xml"))
}
