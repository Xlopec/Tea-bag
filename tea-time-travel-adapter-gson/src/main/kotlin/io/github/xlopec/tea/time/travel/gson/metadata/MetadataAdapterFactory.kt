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

package io.github.xlopec.tea.time.travel.gson.metadata

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.xlopec.tea.time.travel.gson.*

@Deprecated("will hide")
public const val SimpleType1: String = SimpleType

/**
 * [TypeAdapterFactory] that adds metadata to each serialized [json object][SyntheticWrapper]. Metadata includes the following information:
 * * instance type name, included as string property;
 * * synthetic wrapper object that holds instance type name and wrapped value;
 *
 * Consider the following example:
 * ```kotlin
 * val gson = Gson { setPrettyPrinting() }
 * val treeJson = gson.toJson(
 *     Project(
 *             id = 123,
 *             days = 10,
 *             coordinatorName = null,
 *             peopleIncluded = listOf("Max", "Nick", "James")
 *      )
 * )
 * ```
 * This will output the following result:
 * ```json
 * {
 *   "id": {
 *     "@type": "java.lang.Integer",
 *     "@value": 123
 *   },
 *   "days": {
 *     "@type": "java.lang.Integer",
 *     "@value": 10
 *   },
 *   "coordinatorName": {
 *     "@type": "java.lang.String",
 *     "@value": null
 *   },
 *   "peopleIncluded": {
 *     "@type": "java.util.List",
 *     "@value": [
 *       {
 *         "@type": "java.lang.String",
 *         "@value": "Max"
 *       },
 *       {
 *         "@type": "java.lang.String",
 *         "@value": "Nick"
 *       },
 *       {
 *         "@type": "java.lang.String",
 *         "@value": "James"
 *       }
 *     ]
 *   },
 *   "@type": "io.github.xlopec.tea.time.travel.gson.serialization.test.Project"
 * }
 * ```
 *
 * **Notes**:
 *
 * A [Map] which can hold entry with ```null``` key will be deserialized incorrectly since ```null``` key will
 * be transformed to a string during serialization.
 *
 * This type adapter will append metadata for each JSON element!
 */
internal object MetadataAdapterFactory : TypeAdapterFactory {

    override fun <T> create(
        gson: Gson,
        type: TypeToken<T>,
    ): TypeAdapter<T> = with(gson) { with(this) { MetadataTypeAdapter(type) } }

}

context (Gson, TypeAdapterFactory)
private class MetadataTypeAdapter<T>(
    private val type: TypeToken<T>,
) : TypeAdapter<T>() {
    private val elementAdapter: TypeAdapter<JsonElement> = getAdapter(JsonElement::class.java)

    override fun write(
        out: JsonWriter,
        value: T?,
    ) = elementAdapter.write(
        out,
        value.toJsonTreeWithMetaData(type)
    )

    override fun read(
        `in`: JsonReader,
    ): T? = elementAdapter
        .read(`in`)
        .fromJsonTreeWithMetadata(type)

    /**
     * Serializes current receiver instance to a JSON element. Depending on the type of the JSON element this function does the following:
     * * JSON object - type information will be added;
     * * JSON primitive, array or null - the JSON element will be wrapped into JSON object with type information and serialized value
     */
    private fun <T> T?.toJsonTreeWithMetaData(
        type: TypeToken<T>,
    ): JsonElement {
        val jsonElement = getDelegateAdapter(this@TypeAdapterFactory, type)
            .toJsonTree(this@toJsonTreeWithMetaData)

        return if (jsonElement.isJsonObject) {
            jsonElement.asJsonObject.addMetadata(type)
            jsonElement
        } else {
            SyntheticWrapper(type, jsonElement)
        }
    }

    /**
     * Deserializes current JSON element to an instance required type.
     */
    private fun <T> JsonElement.fromJsonTreeWithMetadata(
        type: TypeToken<T>,
    ): T {
        val adapter = getDelegateAdapter(this@TypeAdapterFactory, type)

        return if (isJsonObject) {
            val copy = asJsonObject.deepCopy()
            val flatten = copy.flattenSynthetics()

            adapter.fromJsonTree(flatten)
        } else {
            adapter.fromJsonTree(this)
        }
    }
}