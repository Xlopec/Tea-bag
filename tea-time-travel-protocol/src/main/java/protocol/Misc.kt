package protocol

import com.google.gson.JsonObject

@Suppress("FunctionName")
inline fun JsonObject(
    builder: JsonObject.() -> Unit
): JsonObject = JsonObject().apply(builder)
