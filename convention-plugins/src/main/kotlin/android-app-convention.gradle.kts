plugins {
    id("com.android.application")
    id("kotlin-android")
}

kotlin {
    compilerOptions {
        optIn.addAll(DefaultOptIns + "kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}
