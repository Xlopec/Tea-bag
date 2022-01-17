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

package com.oliynick.max.tea.core.debug.app.transport.serialization

import com.google.gson.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.gson.Gson

internal val GSON = Gson()

fun Value.toJsonElement(): JsonElement =
    when (this) {
        is Null -> JsonNull.INSTANCE
        is CollectionWrapper -> toJsonArray()
        is Ref -> toJsonElement()
        is NumberWrapper -> JsonPrimitive(value)
        is CharWrapper -> JsonPrimitive(value)
        is StringWrapper -> JsonPrimitive(value)
        is BooleanWrapper -> JsonPrimitive(value)
    }

fun JsonElement.toValue(): Value =
    when {
        isJsonPrimitive -> asJsonPrimitive.toValue()
        isJsonArray -> asJsonArray.toCollectionWrapper()
        isJsonObject -> asJsonObject.toRef()
        isJsonNull -> Null
        else -> error("Don't know how to deserialize $this")
    }

private fun Ref.toJsonElement(): JsonElement = JsonObject().apply {
    addProperty("@type", type.name)

    for (property in properties) {
        add(property.name, property.v.toJsonElement())
    }
}

private fun CollectionWrapper.toJsonArray(): JsonArray =
    items.fold(JsonArray(items.size)) { acc, v ->
        acc.add(v.toJsonElement())
        acc
    }

private fun JsonObject.toRef(): Ref {

    val entrySet = entrySet().filter { e -> e.key != "@type" }

    val props = entrySet.mapTo(HashSet(entrySet.size)) { entry ->
        Property(
            entry.key,
            entry.value.toValue()
        )
    }

    return Ref(Type.of(this["@type"].asString), props)
}

private fun JsonPrimitive.toValue(): Value = when {
    isBoolean -> BooleanWrapper.of(asBoolean)
    isString -> StringWrapper(asString)
    isNumber -> toNumberValue()
    else -> error("Don't know how to wrap $this")
}

private fun JsonPrimitive.toNumberValue(): NumberWrapper = NumberWrapper(asNumber)

fun Iterable<JsonElement>.toCollectionWrapper(): CollectionWrapper =
    CollectionWrapper(map { it.asJsonObject.toRef() })

