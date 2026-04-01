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

@file:Suppress("UnstableApiUsage")

rootProject.name = "Tea-bag"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("convention-plugins")
    repositories {
        gradlePluginPortal()
        maven("https://maven.google.com/")
    }
}

include(
    ":tea-core",
    ":tea-compose",
    ":tea-time-travel",
    ":tea-time-travel-protocol",
    ":tea-time-travel-adapter-gson",
    ":tea-navigation",
    ":samples:app",
    ":samples:shared-app-lib",
    ":samples:shared-app-lib-remote",
    ":samples:counter",
    ":samples:compose-effects",
)

dependencyResolutionManagement {
    // todo revert to FAIL_ON_PROJECT_REPOS once issue is resolved for js
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS

    repositories {
        mavenCentral()
        google()
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
        // https://youtrack.jetbrains.com/projects/KT/issues/KT-68533/Kotlin-2.0-WasmJs-error-when-using-RepositoriesMode.FAILONPROJECTREPOS
        exclusiveContent {
            forRepository {
                ivy("https://nodejs.org/dist/") {
                    name = "Node Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]-[classifier]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("org.nodejs", "node") }
                }
            }
            filter { includeGroup("org.nodejs") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/yarnpkg/yarn/releases/download") {
                    name = "Yarn Distributions at $url"
                    patternLayout { artifact("v[revision]/[artifact](-v[revision]).[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.yarnpkg", "yarn") }
                }
            }
            filter { includeGroup("com.yarnpkg") }
        }
        exclusiveContent {
            forRepository {
                ivy("https://github.com/WebAssembly/binaryen/releases/download") {
                    name = "Binaryen Distributions at $url"
                    patternLayout { artifact("version_[revision]/[module]-version_[revision]-[classifier].[ext]") }
                    metadataSources { artifact() }
                    content { includeModule("com.github.webassembly", "binaryen") }
                }
            }
            filter { includeGroup("com.github.webassembly") }
        }
    }
}
