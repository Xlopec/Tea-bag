import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

group = "io.github.xlopec"
version = "SNAPSHOT"

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = "17"
    sourceCompatibility = "17"
}

afterEvaluate {
    tasks.withType<Test>().configureEach {
        val buildDir = File(File(File(rootProject.rootDir.parentFile, "build"), "junit-reports"), project.name)

        description = "$description Also copies test reports to $buildDir"

        reports {
            html.outputLocation.set(File(buildDir, "html"))
            junitXml.outputLocation.set(File(buildDir, "xml"))
        }
    }
}

dependencies {
    implementation(libs.convention.kotlin)
    implementation(libs.convention.intellij.platform)
    implementation(libs.convention.dokka)
    implementation(libs.convention.serializtion)
    implementation(libs.convention.agp)
    implementation(libs.convention.sqldelight)
    implementation(libs.convention.compose.plugin)
    implementation(libs.convention.compose.compiler)
    implementation(libs.convention.nexus.publish)
    implementation(libs.convention.detekt)
    implementation(libs.convention.versions)

    testImplementation(libs.junit)
    testImplementation(libs.junit.runner)
}
