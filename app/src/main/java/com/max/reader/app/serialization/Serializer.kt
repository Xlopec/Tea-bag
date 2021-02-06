/*
 * Copyright (C) 2021. Maksym Oliinyk.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.max.reader.app.serialization

import com.google.gson.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.Type

interface Serializer<T> : JsonSerializer<T>, JsonDeserializer<T>

object PersistentListSerializer : Serializer<PersistentList<*>> {

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
