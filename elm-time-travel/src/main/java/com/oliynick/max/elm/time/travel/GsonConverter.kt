package com.oliynick.max.elm.time.travel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.oliynick.max.elm.time.travel.gson.gson
import protocol.Json

internal object GsonConverter : JsonConverter {

    private val gson = gson()

    override fun toJson(any: Any): String = gson.toJson(any)

    override fun <T> fromJson(json: String, cl: Class<T>): T = gson.fromJson(json, cl)

}

fun gsonSerializer(
    config: GsonBuilder.() -> Unit = {}
): JsonSerializer = GsonSerializer(gson(config))

internal class GsonSerializer(
     private val gson: Gson
) : JsonSerializer {

    override fun Any?.toJson(): String = gson.toJson(this)

    override fun <T> Json.fromJson(t: Class<out T>): T? = gson.fromJson(this, t)

}
