package com.oliynick.max.tea.core.debug.app.transport.serialization

import com.google.gson.*
import com.oliynick.max.tea.core.debug.app.domain.*
import com.oliynick.max.tea.core.debug.gson.Gson

internal val GSON = Gson()

fun Value.toJsonElement(): JsonElement =
    when (this) {
        is Null -> JsonNull.INSTANCE
        is CollectionWrapper -> toJsonElement()
        is Ref -> toJsonElement()
        is NumberWrapper -> JsonPrimitive(value)
        is CharWrapper -> JsonPrimitive(value)
        is StringWrapper -> JsonPrimitive(value)
        is BooleanWrapper -> JsonPrimitive(value)
    }

fun JsonElement.toValue(): Value =
    when {
        isJsonPrimitive -> asJsonPrimitive.toValue()
        isJsonArray -> asJsonArray.toValue()
        isJsonObject -> asJsonObject.toValue()
        isJsonNull -> Null
        else -> error("Don't know how to deserialize $this")
    }

private fun Ref.toJsonElement(): JsonElement = JsonObject().apply {
    addProperty("@type", type.name)

    for (property in properties) {
        add(property.name, property.v.toJsonElement())
    }
}

private fun CollectionWrapper.toJsonElement(): JsonArray =
    value.fold(JsonArray(value.size)) { acc, v ->
        acc.add(v.toJsonElement())
        acc
    }

private fun JsonObject.toValue(): Ref {

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

private fun JsonPrimitive.toNumberValue(): Value = NumberWrapper(asNumber)

private fun JsonArray.toValue(): Value =
    CollectionWrapper(map { it.asJsonObject.toValue() })

