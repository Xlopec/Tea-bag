@file:Suppress("FunctionName")
package com.oliynick.max.elm.time.travel.gson

import com.google.gson.JsonArray
import com.google.gson.JsonObject

inline fun JsonObject(
    builder: JsonObject.() -> Unit
): JsonObject = JsonObject().apply(builder)

inline fun JsonArray(
    capacity: Int = 10,
    builder: JsonArray.() -> Unit
): JsonArray = JsonArray(capacity).apply(builder)
