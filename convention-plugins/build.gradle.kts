
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

group = "io.github.xlopec"
version = "SNAPSHOT"


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

//noinspection UseTomlInstead
dependencies {
    implementation(libs.convention.agp)
    implementation(libs.convention.kotlin)
    implementation(libs.convention.intellij.platform)
    implementation(libs.convention.dokka)
    implementation(libs.convention.serializtion)
    implementation(libs.convention.sqldelight)
    implementation(libs.convention.compose.plugin)
    implementation(libs.convention.compose.compiler)
    implementation(libs.convention.nexus.publish)
    implementation(libs.convention.detekt)
    implementation(libs.convention.versions)
    /*



    testImplementation("junit:junit:4.13.2")
    // used for tests under buildSrc directory
    testImplementation("io.kotlintest:kotlintest-runner-junit4:3.4.2")*/
}
