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

package io.github.xlopec.tea.time.travel.plugin.feature.server

import com.google.gson.*
import io.github.xlopec.tea.time.travel.gson.addMetadata
import io.github.xlopec.tea.time.travel.gson.metadata.SimpleType1
import io.github.xlopec.tea.time.travel.gson.rawSyntheticType
import io.github.xlopec.tea.time.travel.plugin.model.*

internal fun Value.toJsonElement(): JsonElement =
    when (this) {
        is Null -> JsonNull.INSTANCE
        is CollectionWrapper -> toJsonArray()
        is Ref -> toJsonElement()
        is NumberWrapper -> JsonPrimitive(value)
        is CharWrapper -> JsonPrimitive(value)
        is StringWrapper -> JsonPrimitive(value)
        is BooleanWrapper -> JsonPrimitive(value)
    }

internal fun JsonElement.toValue(): Value =
    when {
        isJsonPrimitive -> asJsonPrimitive.toValue()
        isJsonArray -> asJsonArray.toCollectionWrapper()
        isJsonObject -> asJsonObject.toRef()
        isJsonNull -> Null
        else -> error("Don't know how to deserialize $this")
    }

internal fun Ref.toJsonElement(): JsonElement = JsonObject().apply {
    addMetadata(type.name)

    for (property in properties) {
        add(property.name, property.v.toJsonElement())
    }
}

internal fun CollectionWrapper.toJsonArray(): JsonArray = items.toJsonArray(Value::toJsonElement)

internal inline fun <T> Collection<T>.toJsonArray(
    mapper: (T) -> JsonElement
): JsonArray =
    fold(JsonArray(size)) { acc, v ->
        acc.add(mapper(v))
        acc
    }

internal fun JsonObject.toRef(): Ref {
    val entrySet = entrySet().filter { e -> e.key != SimpleType1 }
    // should be sorted to produce idempotent values
    val props = entrySet.sortedBy { it.key }.mapTo(LinkedHashSet(entrySet.size)) { entry ->
        Property(
            entry.key,
            entry.value.toValue()
        )
    }

    return Ref(Type.of(rawSyntheticType), props)
}

internal fun JsonPrimitive.toValue(): Value = when {
    isBoolean -> BooleanWrapper.of(asBoolean)
    isString -> StringWrapper(asString)
    isNumber -> toNumberValue()
    else -> error("Don't know how to wrap $this")
}

internal fun JsonPrimitive.toNumberValue(): NumberWrapper = NumberWrapper(asNumber)

internal fun Iterable<JsonElement>.toCollectionWrapper(): CollectionWrapper =
    CollectionWrapper(map { it.asJsonObject.toRef() })
