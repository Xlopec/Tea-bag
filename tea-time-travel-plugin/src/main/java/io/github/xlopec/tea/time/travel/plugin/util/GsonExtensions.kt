package io.github.xlopec.tea.time.travel.plugin.util

import com.google.gson.Gson
import com.google.gson.JsonElement
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.reflect.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun Gson.toJson(
    any: Any,
    file: File,
    typeOfSrc: Type = any::class.java,
) = withContext(Dispatchers.IO) {
    BufferedWriter(FileWriter(file)).use { bw -> toJson(any, typeOfSrc, bw) }
}

internal suspend fun Gson.toJson(
    jsonElement: JsonElement,
    file: File,
) = withContext(Dispatchers.IO) {
    BufferedWriter(FileWriter(file)).use { bw -> toJson(jsonElement, bw) }
}
