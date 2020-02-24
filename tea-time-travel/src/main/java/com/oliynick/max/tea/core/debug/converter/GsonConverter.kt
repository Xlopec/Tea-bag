@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oliynick.max.tea.core.debug.gson.Gson
import protocol.JsonTree

fun GsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonConverter = GsonConverter(Gson(config))

private class GsonConverter(
    private val gson: Gson
) : JsonConverter {

    override fun <T> toJsonTree(
        any: T
    ): JsonTree = gson.toJsonTree(any)

    override fun <T> fromJsonTree(
        json: JsonTree,
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