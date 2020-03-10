@file:Suppress("FunctionName")

package com.oliynick.max.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.contracts.contract

/**
 * [TypeAdapterFactory] that adds type token to each [json object][JsonObject]. Type token is included as string property with name `@type`
 * that holds actual class name
 *
 * Note that this type adapter will append type token for each json object recursively
 *
 * Consider the following example:
 * ```
 * data class C(val value: String = "C")
 * ```
 * it'll be transformed to the next json object (if only default serializers are used):
 * ```
 * {
 * "@type": "java.lang.String",
 * "value": "C"
 * }
 * ```
 *
 * **Note** that [Map] which can hold entry with ```null``` key will be deserialized incorrectly since ```null``` key will
 * be transformed to a string during serialization.
 */
internal object TypeAppenderAdapterFactory : TypeAdapterFactory {

    override fun <T> create(
        gson: Gson,
        type: TypeToken<T>
    ): TypeAdapter<T> = object : TypeAdapter<T>() {

        val elementAdapter = gson.getAdapter(JsonElement::class.java)

        override fun write(
            out: JsonWriter,
            value: T?
        ) = elementAdapter.write(
            out,
            value.toJsonTree(gson, type, this@TypeAppenderAdapterFactory)
        )

        override fun read(
            `in`: JsonReader
        ): T? = elementAdapter
            .read(`in`)
            .let { element ->

                if (element.isJsonObject) {
                    element.asJsonObject.asAny(gson, this@TypeAppenderAdapterFactory, type)
                } else {
                    gson.getDelegateAdapter(this@TypeAppenderAdapterFactory, type)
                        .fromJsonTree(element)
                }
            }
    }

}

private fun <T> T?.toJsonTree(
    gson: Gson,
    type: TypeToken<T>,
    skipFactory: TypeAdapterFactory
): JsonElement =
    gson.getDelegateAdapter(skipFactory, type)
        .toJsonTree(this@toJsonTree)
        .also { jsonElement ->

            if (shouldAddTypeLabel(this@toJsonTree, jsonElement)) {
                @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                jsonElement.addProperty("@type", this@toJsonTree!!::class.java.name)
            }
        }

private fun <T> JsonObject.asAny(
    gson: Gson,
    skipFactory: TypeAdapterFactory,
    type: TypeToken<T>
): T? = gson.getDelegateAdapter(skipFactory, type() ?: type)
    .fromJsonTree(this)

@Suppress("UNCHECKED_CAST")
private fun <T> TypeToken(
    name: String
): TypeToken<T> =
    typeTokenCache.getOrPut(name) { TypeToken.get(Class.forName(name)) } as TypeToken<T>

private val typeTokenCache = mutableMapOf<String, TypeToken<*>>()

private fun <T> JsonObject.type(): TypeToken<T>? = get("@type")?.asString?.let(::TypeToken)

private fun shouldAddTypeLabel(
    src: Any?,
    srcAsJson: JsonElement
): Boolean {
    contract {
        returns(true) implies (srcAsJson is JsonObject)
        returns(true) implies (src != null)
    }
    // check is needed in case custom adapter returns some default json object for
    // null instance of T. Also, Map is serialized by Gson as json object and we want
    // to avoid adding type label to serialized Map json object
    return srcAsJson.isJsonObject && (src != null && src !is Map<*, *>)
}
