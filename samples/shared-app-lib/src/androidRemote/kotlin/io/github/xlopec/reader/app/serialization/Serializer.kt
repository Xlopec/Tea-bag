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

package io.github.xlopec.reader.app.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet

// gson serializers to enable debugging facilities

internal interface Serializer<T> : JsonSerializer<T>, JsonDeserializer<T>

internal object PersistentListSerializer : Serializer<PersistentList<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): PersistentList<*> = json.asJsonArray.map { element ->
        // For our app we know that we always deal with objects,
        // so it's safe to access "@type" property without additional checks
        context.deserialize<Any?>(element, Class.forName(element.asJsonObject["@type"].asString))
    }.toPersistentList()

    override fun serialize(
        src: PersistentList<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement = JsonArray()
        .apply { src.map(context::serialize).forEach(::add) }
}

internal object PersistentSetSerializer : Serializer<PersistentSet<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): PersistentSet<*> = json.asJsonArray.map { element ->
        // For our app we know that we always deal with objects,
        // so it's safe to access "@type" property without additional checks
        context.deserialize<Any?>(element, Class.forName(element.asJsonObject["@type"].asString))
    }.toPersistentSet()

    override fun serialize(
        src: PersistentSet<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement = JsonArray()
        .apply { src.map(context::serialize).forEach(::add) }
}
