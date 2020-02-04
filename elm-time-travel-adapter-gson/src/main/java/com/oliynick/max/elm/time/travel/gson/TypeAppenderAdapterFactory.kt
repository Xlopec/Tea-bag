@file:Suppress("FunctionName")

package com.oliynick.max.elm.time.travel.gson

import com.google.gson.*
import com.google.gson.internal.ConstructorConstructor
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.oliynick.max.elm.core.component.UnstableApi
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
@UnstableApi
object TypeAppenderAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(
        gson: Gson,
        type: TypeToken<T>
    ): TypeAdapter<T> = object : TypeAdapter<T>() {

        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        override fun write(
            out: JsonWriter,
            value: T
        ) = elementAdapter.write(
            out,
            value.toJsonTree(gson, type.cast(), this@TypeAppenderAdapterFactory)
        )

        @Suppress("UNCHECKED_CAST")
        override fun read(
            `in`: JsonReader
        ): T {

            println(type)

            val jsonElement = elementAdapter
                .read(`in`)

            if (jsonElement.isJsonObject) {
                return jsonElement.asJsonObject
                    .asAny(gson, this@TypeAppenderAdapterFactory, type) as T
            }

            return gson.getDelegateAdapter(this@TypeAppenderAdapterFactory, type).fromJsonTree(jsonElement)
        }
    }

}

private fun Any?.toJsonTree(
    gson: Gson,
    type: TypeToken<Any?>,
    skipFactory: TypeAdapterFactory
): JsonElement {

    // workaround to serialize null values properly
    val tree: JsonElement = when (this@toJsonTree) {
        is Map<*, *> -> this@toJsonTree.toJsonTree(gson, skipFactory)
        is Iterable<*> -> this@toJsonTree.toJsonTree(gson, skipFactory)
        else -> gson.getDelegateAdapter(skipFactory, type).toJsonTree(this@toJsonTree)
    }

    if (tree.isJsonObject) {
        tree.asJsonObject.addProperty("@type", this@toJsonTree.clazz.name)
    }

    return tree
}

private fun Iterable<*>.toJsonTree(
    gson: Gson,
    skipFactory: TypeAdapterFactory
): JsonArray = JsonArray((this as? Collection<*>)?.size ?: 10) {

    this@toJsonTree.forEach { e ->
        add(e.toJsonTree(gson, TypeToken(e.clazz).cast(), skipFactory))
    }
}

private fun Map<*, *>.toJsonTree(
    gson: Gson,
    skipFactory: TypeAdapterFactory
): JsonArray = JsonArray(entries.size) {
    TODO("not implemented")
    entries.forEach { e ->
        add(JsonObject {
            add("@key", e.key.toJsonTree(gson, TypeToken(e.key.clazz).cast(), skipFactory))
            add("@value", e.value.toJsonTree(gson, TypeToken(e.value.clazz).cast(), skipFactory))
        })
    }
}

private fun JsonObject.asAny(
    gson: Gson,
    skipFactory: TypeAdapterFactory,
    type: TypeToken<*>
): Any? {

    require(has("@type")) {
        "json object should have property '@type', json was\n$this\n\n"
    }

    val token by unsafeLazy { TypeToken(get("@type").asString) }

    return gson.getDelegateAdapter(skipFactory, token).fromJsonTree(this)
    /*val array by unsafeLazy { value!!.asJsonArray }

    return when {
        isArray(token, value) -> array.asArray(token.rawType, gson, skipFactory)
        isCollection(token, value) -> array.asCollection(gson, token.cast(), skipFactory)
        isMap(token, value) -> array.asMap(gson, token.cast(), skipFactory)
        else -> gson.getDelegateAdapter(skipFactory, token).fromJsonTree(this)
    }*/
}

private fun JsonArray.asArray(
    cl: Class<*>,
    gson: Gson,
    skipFactory: TypeAdapterFactory
): KArray<Any?> =
    cl.cast<Any?>()
        .newArray(size())
        .also { array ->
            this@asArray.forEachIndexed { index, jsonElement ->
                array[index] = jsonElement.asJsonObject.asAny(gson, skipFactory, TODO())
            }
        }

private fun JsonArray.asCollection(
    gson: Gson,
    cl: TypeToken<out MutableCollection<Any?>>,
    skipFactory: TypeAdapterFactory
): Collection<Any?> =
    asSequence()
        .map { jsonElement -> jsonElement.asJsonObject }
        .mapTo(gson.constructorConstructor.construct(cl)) { jsonObject ->
            jsonObject.asAny(
                gson,
                skipFactory,
                TODO()
            )
        }

private fun JsonArray.asMap(
    gson: Gson,
    cl: TypeToken<out MutableMap<Any?, Any?>>,
    skipFactory: TypeAdapterFactory
): Map<Any?, Any?> =
    asSequence()
        .map { jsonElement -> jsonElement.asJsonObject }
        .associateByTo(
            destination = gson.constructorConstructor.construct(cl),
            keySelector = { entry -> entry["@key"].asJsonObject.asAny(gson, skipFactory, TODO()) },
            valueTransform = { entry -> entry["@value"].asJsonObject.asAny(gson, skipFactory, TODO()) }
        )

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

private fun isArray(
    token: TypeToken<out Any?>,
    value: JsonElement
): Boolean = value.isJsonArray && token.rawType.isArray

private fun isCollection(
    token: TypeToken<out Any?>,
    value: JsonElement
): Boolean = value.isJsonArray && Collection::class.java.isAssignableFrom(token.rawType)

private fun isMap(
    token: TypeToken<out Any?>,
    value: JsonElement
): Boolean = value.isJsonArray && Map::class.java.isAssignableFrom(token.rawType)
