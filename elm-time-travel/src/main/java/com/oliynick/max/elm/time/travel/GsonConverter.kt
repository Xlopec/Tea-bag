package com.oliynick.max.elm.time.travel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
import com.oliynick.max.elm.time.travel.gson.TypeAppenderAdapterFactory
import com.oliynick.max.elm.time.travel.gson.gson
import protocol.JsonTree
import kotlin.reflect.KClass

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
    config: GsonBuilder.() -> Unit = { registerTypeAdapterFactory(TypeAppenderAdapterFactory) }
): JsonConverter = GsonConverter(gson(config))

@PublishedApi
internal fun <T : Any> RuntimeTypeAdapterFactory<T>.registerRecursively(sub: KClass<out T>) {
    if (sub.isSealed) {
        sub.sealedSubclasses.forEach { registerRecursively(it) }
    }

    println("registered $sub")
    registerSubtype(sub.java)
}
