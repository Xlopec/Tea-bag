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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import io.github.xlopec.tea.time.travel.plugin.model.BooleanWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CharWrapper
import io.github.xlopec.tea.time.travel.plugin.model.CollectionWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Null
import io.github.xlopec.tea.time.travel.plugin.model.NumberWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Property
import io.github.xlopec.tea.time.travel.plugin.model.Ref
import io.github.xlopec.tea.time.travel.plugin.model.StringWrapper
import io.github.xlopec.tea.time.travel.plugin.model.Type
import io.github.xlopec.tea.time.travel.plugin.model.Value

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
    addProperty("@type", type.name)

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

    val entrySet = entrySet().filter { e -> e.key != "@type" }
    // should be sorted to produce idempotent values
    val props = entrySet.sortedBy { it.key }.mapTo(LinkedHashSet(entrySet.size)) { entry ->
        Property(
            entry.key,
            entry.value.toValue()
        )
    }

    return Ref(Type.of(this["@type"].asString), props)
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
