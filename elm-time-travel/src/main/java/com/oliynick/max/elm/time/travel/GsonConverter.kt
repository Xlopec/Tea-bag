package com.oliynick.max.elm.time.travel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oliynick.max.elm.time.travel.gson.Gson
import protocol.JsonTree

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

fun gsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonConverter = GsonConverter(Gson(config))
