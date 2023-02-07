/*
 * MIT License
 *
 * Copyright (c) 2021. Maksym Oliinyk.
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

package io.github.xlopec.tea.time.travel.gson.serialization.serializer

import com.google.gson.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

internal object PersistentListSerializer : JsonSerializer<PersistentList<*>>,
    JsonDeserializer<PersistentList<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PersistentList<*> {

        val genericArgType = (typeOfT as ParameterizedType).actualTypeArguments[0] as Class<*>

        return json.asJsonArray.asSequence()
            .map { element ->
                context.deserialize<Any?>(
                    element,
                    if (genericArgType.isJsonPrimitive) genericArgType else element.asJsonObject.type
                )
            }
            .toList()
            .toPersistentList()
    }

    override fun serialize(
        src: PersistentList<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = JsonArray().apply {
        for (v in src) {
            add(context.serialize(v))
        }
    }
}

private inline val Class<*>.isJsonPrimitive: Boolean
    get() = kotlin.javaPrimitiveType != null || this == String::class.java

private inline val JsonObject.type: Class<*>
    get() = Class.forName(this["@type"].asString)
