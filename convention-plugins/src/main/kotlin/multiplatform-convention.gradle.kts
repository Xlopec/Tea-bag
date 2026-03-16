plugins {
    kotlin("multiplatform")
}

kotlin {
    compilerOptions {
        optIn.addAll(DefaultOptIns)
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xconsistent-data-class-copy-visibility")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
