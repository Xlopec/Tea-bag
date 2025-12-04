plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    implementation(project(":tea-core"))
}
