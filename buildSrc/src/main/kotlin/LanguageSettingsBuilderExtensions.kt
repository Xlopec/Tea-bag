import org.jetbrains.kotlin.gradle.plugin.LanguageSettingsBuilder

fun LanguageSettingsBuilder.optIn(
    vararg annotationNames: String
) = annotationNames.forEach(::optIn)