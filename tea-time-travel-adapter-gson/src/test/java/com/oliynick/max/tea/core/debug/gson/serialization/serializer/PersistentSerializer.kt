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

package com.oliynick.max.tea.core.debug.gson.serialization.serializer

import com.google.gson.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object PersistentListSerializer : JsonSerializer<PersistentList<*>>,
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

inline val JsonObject.type: Class<*>
    get() = Class.forName(this["@type"].asString)
