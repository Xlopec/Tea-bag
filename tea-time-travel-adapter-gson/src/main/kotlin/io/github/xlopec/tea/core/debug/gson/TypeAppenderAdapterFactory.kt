/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

@file:Suppress("FunctionName")

package io.github.xlopec.tea.core.debug.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * [TypeAdapterFactory] that adds type token to each [json object][JsonObject]. Type token is included as string property with name `@type`
 * that holds actual class name
 *
 * Note that this type adapter will append type token for each json object recursively
 *
 * Consider the following example:
 * ```
 * data class C(val value: String = "C", val list: List<Int> = listOf(1, 2 ,3))
 * ```
 * it'll be transformed to the next json object (if only default serializers are used):
 * ```
 * {
 * "@type": "java.lang.String",
 * "value": "C",
 * "list": [1, 2, 3]
 * }
 * ```
 *
 * **Note** that [Map] which can hold entry with ```null``` key will be deserialized incorrectly since ```null``` key will
 * be transformed to a string during serialization.
 */
// `<->` is two way conversion and `->` is one way conversion
// json conversion as is:
// Object <-> { "@type": "${instance.javaClass}", other properties }
// Collection -> [elements]
// Primitive <-> json primitive
// json conversion that'd be nice to have:
// Object <-> { "@type": "${instance.javaClass}", "@type.${property.name}": "${property.javaClass}" }
// Collection <-> { "@type": "${instance.javaClass}", "values": [elements] }
// Primitive <-> json primitive
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

@OptIn(ExperimentalContracts::class)
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
