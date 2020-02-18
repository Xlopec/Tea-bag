import org.gradle.api.JavaVersion

inline val JavaVersion.majorVersionInt: Int
    get() = ordinal + 1