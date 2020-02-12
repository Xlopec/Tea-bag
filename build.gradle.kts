/*
 * Copyright (C) 2019 Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    /* ext {
         kotlin_version = "1.3.61"
         ktor_version = "1.2.4"
         coroutines_version = "1.3.3"
     }*/

    repositories {
        mavenLocal()
        google()
        jcenter()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
        maven { setUrl("https://kotlin.bintray.com/ktor") }
        maven { setUrl("https://dl.bintray.com/kotlin/kotlin-eap") }

    }
    dependencies {
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.androidGradlePlugin)
        classpath(BuildPlugins.androidMaven)
        classpath(BuildPlugins.androidJacoco)

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

//apply plugin: "idea"
//apply plugin: "com.vanniktech.android.junit.jacoco"

/*idea.module {
    excludeDirs += file("$projectDir/androidx_prebuilts")
}*/

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven {
            setUrl("https://artifactory.cronapp.io/public-release/")
        }
        // maven { url = java.net.URI("https://dl.bintray.com/kotlin/kotlin-eap") }
        // maven { url "https://artifactory.cronapp.io/public-release/" }

    }

}

tasks.register("clean").configure {
    delete(rootProject.buildDir)
}

project.afterEvaluate {

    allprojects {

        tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
            kotlinOptions {
                // will disable warning about usage of experimental Kotlin features
                freeCompilerArgs = listOf(
                    "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes",
                    "-Xuse-experimental=kotlin.Experimental",
                    "-Xuse-experimental=kotlin.contracts.ExperimentalContracts",
                    "-Xuse-experimental=kotlinx.coroutines.FlowPreview",
                    "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI",
                    "-XXLanguage:+NewInference",
                    "-XXLanguage:+InlineClasses"
                )
            }
        }
    }
}