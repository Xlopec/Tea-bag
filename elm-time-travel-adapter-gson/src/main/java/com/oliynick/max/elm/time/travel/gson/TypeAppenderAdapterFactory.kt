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
        ) = elementAdapter.write(out, value.toJsonTree(gson, type as TypeToken<Any?>, this@TypeAppenderAdapterFactory))

        @Suppress("UNCHECKED_CAST")
        override fun read(
            `in`: JsonReader
        ): T = elementAdapter
            .read(`in`)
            .asJsonObject
            .fromJsonElement(gson, this@TypeAppenderAdapterFactory) as T
    }

}

private fun Any?.toJsonTree(
    gson: Gson,
    type: TypeToken<Any?>,
    skipFactory: TypeAdapterFactory
) = JsonObject {

    addProperty("@type", this@toJsonTree.clazz.name)
    // workaround to serialize null values properly
    val tree = when(this@toJsonTree) {
        null -> null
        is Map<*, *> -> TODO("Not implemented")//this@toJsonTree.toJsonTree(gson, skipFactory)
        is Iterable<*> -> this@toJsonTree.toJsonTree(gson, skipFactory)
        else -> gson.getDelegateAdapter(skipFactory, type).toJsonTree(this@toJsonTree)
    }

    add("@value", tree)
}

private fun Iterable<*>.toJsonTree(
    gson: Gson,
    skipFactory: TypeAdapterFactory
): JsonArray = JsonArray((this as? Collection<*>)?.size ?: 10) {

    this@toJsonTree.forEach { e ->
        @Suppress("UNCHECKED_CAST")
        add(e.toJsonTree(gson, TypeToken(e.clazz) as TypeToken<Any?>, skipFactory))
    }
}

/*private fun Map<*, *>.toJsonTree(
    gson: Gson,
    skipFactory: TypeAdapterFactory
): JsonArray = JsonArray(entries.size) {

    entries.forEach { e ->

        JsonObject {

        }

        add(e.toJsonTree(gson, TypeToken(e.clazz) as TypeToken<Any?>, skipFactory))
    }
}*/

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
    cl: Class<*>
): TypeToken<*> = TypeToken(cl.name!!)

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
