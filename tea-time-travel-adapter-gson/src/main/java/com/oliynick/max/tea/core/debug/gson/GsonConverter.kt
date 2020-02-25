@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import protocol.JsonConverter

fun GsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonConverter<JsonElement> = GsonConverter(Gson(config))

private class GsonConverter(
    private val gson: Gson
) : JsonConverter<JsonElement> {

    override fun <T> toJsonTree(
        any: T
    ): JsonElement = gson.toJsonTree(any)

    override fun <T> fromJsonTree(
        json: JsonElement,
        cl: Class<T>
    ): T = gson.fromJson(json, cl)

    override fun <T> toJson(
        any: T
    ): String = gson.toJson(any)

    override fun <T> fromJson(
        json: String,
        cl: Class<T>
    ): T = gson.fromJson(json, cl)

}