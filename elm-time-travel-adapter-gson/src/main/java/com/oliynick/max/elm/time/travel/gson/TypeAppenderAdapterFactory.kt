@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import com.google.gson.internal.ConstructorConstructor
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Array
import java.util.*

object TypeAppenderAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(
        gson: Gson,
        type: TypeToken<T>
    ): TypeAdapter<T> = object : TypeAdapter<T>() {

        val elementAdapter = gson.getAdapter(JsonElement::class.java)
        val delegate = gson.getDelegateAdapter(this@TypeAppenderAdapterFactory, type)

        override fun write(
            out: JsonWriter,
            value: T
        ) = elementAdapter.write(out, JsonObject {
            addProperty(
                "@type",
                value.clazz.name
            )
            add("@value", delegate.toJsonTree(value))
        })

        @Suppress("UNCHECKED_CAST")
        override fun read(
            `in`: JsonReader
        ): T = elementAdapter
            .read(`in`)
            .asJsonObject
            .fromJsonElement(gson, this@TypeAppenderAdapterFactory) as T
    }

}

private fun JsonObject.fromJsonElement(
    gson: Gson,
    skipFactory: TypeAdapterFactory
): Any? {

    require(has("@type")) {
        "json object should have property '@type', json was\n$this\n\n"
    }

    val token by lazy { TypeToken(get("@type").asString) }
    val value: JsonElement? = get("@value")

    @Suppress("UNCHECKED_CAST")
    return when {
        value == null || value.isJsonNull -> null
        value.isJsonArray -> value.asJsonArray.fromJsonArray(gson, token, skipFactory)
        else -> gson.getDelegateAdapter(skipFactory, token).fromJsonTreeSafe(value)
    }
}

private fun JsonArray.fromJsonArray(
    gson: Gson,
    cl: TypeToken<*>,
    skipFactory: TypeAdapterFactory
): Any {

    fun fromJsonElement(
        jsonObject: JsonObject
    ): Any? = gson.getDelegateAdapter(skipFactory, TypeToken(jsonObject["@type"].asString)).fromJsonTreeSafe(jsonObject["@value"])

    return if (cl.rawType.isArray) {
        (Array.newInstance((cl.rawType as Class<*>).componentType, size()) as kotlin.Array<Any?>).also {  arr ->
            forEachIndexed { index, jsonElement ->

                val from = fromJsonElement(jsonElement.asJsonObject)

                arr[index] = from
            }
        }
    } else {

        asSequence()
            .map { jsonElement -> jsonElement.asJsonObject }
            .mapTo(gson.constructorConstructor.construct(cl as TypeToken<out MutableCollection<Any?>>)) { jsonObject ->
                gson.getDelegateAdapter(skipFactory, TypeToken(jsonObject["@type"].asString)).fromJsonTreeSafe(jsonObject["@value"])
            }
    }
}


private fun TypeToken(
    name: String
): TypeToken<*> =
    typeTokenCache.getOrPut(name) { TypeToken.get(Class.forName(name)) }

private fun <T> ConstructorConstructor.construct(
    token: TypeToken<out T>
): T = get(token).construct() as T

private fun <T> TypeAdapter<T>.fromJsonTreeSafe(
    element: JsonElement?
): T? = if (element == null) null else fromJsonTree(element)

private val typeTokenCache = WeakHashMap<String, TypeToken<*>>()

private val constructorConstructorField by lazy {
    Gson::class.java.getDeclaredField("constructorConstructor")
        .apply { isAccessible = true }
}

private inline val Gson.constructorConstructor: ConstructorConstructor
    get() = constructorConstructorField.get(this) as ConstructorConstructor

private inline val Any?.clazz: Class<out Any>
    get() = (if (this == null) Any::class else this::class).java
