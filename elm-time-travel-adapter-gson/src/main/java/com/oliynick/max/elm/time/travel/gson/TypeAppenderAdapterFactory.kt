@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import com.google.gson.internal.ConstructorConstructor
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Array
import kotlin.Array as KArray

/**
 * [TypeAdapterFactory] that wraps values in [json object][JsonObject] that holds actual type of the value and its actual json representation.
 *
 * Note that this type adapter will wrap each property or element (in case of array or collection serialization) of the value in json wrapper
 * recursively
 *
 * Consider the following example, if we have the following json structure:
 * ```
 * {
 * "property": "value"
 * }
 * ```
 * it'll be transformed to the next json object:
 * ```
 * {
 * "@type": "java.lang.String",
 * "@value":
 *      {
 *       "property": "value"
 *      }
 * }
 * ```
 */
object TypeAppenderAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(
        gson: Gson,
        type: TypeToken<T>
    ): TypeAdapter<T> = object : TypeAdapter<T>() {

        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        override fun write(
            out: JsonWriter,
            value: T
        ) = elementAdapter.write(out, JsonObject {
            addProperty(
                "@type",
                value.clazz.name
            )

            val delegate = gson.getDelegateAdapter(this@TypeAppenderAdapterFactory, type)

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

    val token by unsafeLazy { TypeToken(get("@type").asString) }
    val value: JsonElement? = get("@value")
    val array by unsafeLazy { value!!.asJsonArray }

    @Suppress("UNCHECKED_CAST")
    return when {
        value == null || value.isJsonNull -> null
        value.isJsonArray && token.rawType.isArray -> array.fromJsonArray(
            token.rawType,
            gson,
            skipFactory
        )
        value.isJsonArray -> array.fromJsonArray(gson, token.cast(), skipFactory)
        else -> gson.getDelegateAdapter(skipFactory, token).fromJsonTree(value)
    }
}

@Suppress("UNCHECKED_CAST")
private fun JsonArray.fromJsonArray(
    cl: Class<*>,
    gson: Gson,
    skipFactory: TypeAdapterFactory
): KArray<Any?> =
    cl.cast<Any?>()
        .newArray(size())
        .also { array ->
            this@fromJsonArray.forEachIndexed { index, jsonElement ->
                array[index] = jsonElement.asJsonObject.fromJsonElement(gson, skipFactory)
            }
        }

private fun JsonArray.fromJsonArray(
    gson: Gson,
    cl: TypeToken<out MutableCollection<Any?>>,
    skipFactory: TypeAdapterFactory
): Collection<Any?> =
    asSequence()
        .map { jsonElement -> jsonElement.asJsonObject }
        .mapTo(gson.constructorConstructor.construct(cl)) { jsonObject ->
            jsonObject.fromJsonElement(
                gson,
                skipFactory
            )
        }

private fun <T> Class<T>.newArray(
    size: Int
): KArray<T> {
    require(isArray) { "Class $this is not array" }
    @Suppress("UNCHECKED_CAST")
    return Array.newInstance(componentType, size) as KArray<T>
}

private fun TypeToken(
    name: String
): TypeToken<*> =
    typeTokenCache.getOrPut(name) { TypeToken.get(Class.forName(name)) }

private fun <T> ConstructorConstructor.construct(
    token: TypeToken<out T>
): T = get(token).construct() as T

private val typeTokenCache = mutableMapOf<String, TypeToken<*>>()

private val constructorConstructorField by lazy {
    Gson::class.java.getDeclaredField("constructorConstructor")
        .apply { isAccessible = true }
}

private inline val Gson.constructorConstructor: ConstructorConstructor
    get() = constructorConstructorField.get(this) as ConstructorConstructor

private inline val Any?.clazz: Class<out Any>
    get() = (if (this == null) Any::class else this::class).java

@Suppress("UNCHECKED_CAST")
private fun <T> TypeToken<*>.cast() = this as TypeToken<T>

@Suppress("UNCHECKED_CAST")
private fun <T> Class<*>.cast() = this as Class<T>

private fun <T> unsafeLazy(
    init: () -> T
) = lazy(LazyThreadSafetyMode.NONE, init)
