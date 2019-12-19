package com.oliynick.max.elm.time.travel

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory
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

inline fun <reified T> GsonBuilder.registerReflectiveTypeAdapter() {
    RuntimeTypeAdapterFactory.of(T::class.java)
        .also { adapterFactory ->
            T::class.sealedSubclasses.forEach { sub ->
                adapterFactory.registerSubtype(sub.java)
            }
        }
        .also { adapterFactory ->
            registerTypeAdapterFactory(adapterFactory)
        }
}

internal class GsonSerializer(
    private val gson: Gson
) : JsonSerializer {

    override fun Any?.toJson(): String = gson.toJson(this).also {
        println("Serialized to $it")
    }

    override fun <T> Json.fromJson(t: Class<out T>): T? =
        runCatching { gson.fromJson(this, t) }
            .getOrElse { th -> throw JsonParseException("Couldn't deserialize $this", th) }

}
