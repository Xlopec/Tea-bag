package com.oliynick.max.tea.core.debug.app.transport.serialization

import com.google.gson.*
import com.oliynick.max.tea.core.debug.app.domain.cms.*
import com.oliynick.max.tea.core.debug.gson.Gson
import com.oliynick.max.tea.core.debug.gson.JsonObject
import com.oliynick.max.tea.core.debug.gson.TypeAppenderAdapterFactory

internal val GSON by lazy {
    Gson {
        registerTypeAdapterFactory(TypeAppenderAdapterFactory)
    }
}

fun Value<*>.toJsonElement(): JsonElement =
    when (this) {
        is PrimitiveWrapper<*> -> toJsonElement()
        is Null -> toJsonElement()
        is CollectionWrapper -> toJsonElement()
        is Ref -> toJsonElement()
    }

private fun Null.toJsonElement(): JsonNull = JsonNull.INSTANCE

private fun PrimitiveWrapper<*>.toJsonElement(): JsonPrimitive =
    when (this@toJsonElement) {
        is IntWrapper -> JsonPrimitive(value)
        is ByteWrapper -> JsonPrimitive(value)
        is ShortWrapper -> JsonPrimitive(value)
        is CharWrapper -> JsonPrimitive(value)
        is LongWrapper -> JsonPrimitive(value)
        is DoubleWrapper -> JsonPrimitive(value)
        is FloatWrapper -> JsonPrimitive(value)
        is StringWrapper -> JsonPrimitive(value)
        is BooleanWrapper -> JsonPrimitive(value)
    }

fun JsonElement.toValue(): Value<*> =
    when {
        isJsonPrimitive -> asJsonPrimitive.toValue()
        isJsonArray -> asJsonArray.toValue()
        isJsonObject -> asJsonObject.toValue()
        isJsonNull -> Null
        else -> error("Don't know how to deserialize $this")
    }

private fun Ref.toJsonElement(): JsonElement = JsonObject {
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

    val props = entrySet.mapTo(HashSet<Property<*>>(entrySet.size)) { entry ->
        Property(
            entry.key,
            entry.value.toValue()
        )
    }

    return Ref(Type.of(this["@type"].asString), props)
}

// fixme add explicit type param
private fun JsonPrimitive.toValue(): Value<*> = when {
    isBoolean -> BooleanWrapper(asBoolean)
    isString -> StringWrapper(asString)
    isNumber -> toNumberValue()
    else -> error("Don't know how to wrap $this")
}

private fun JsonPrimitive.toNumberValue(): PrimitiveWrapper<*> =
    when (asNumber) {
        is Float -> FloatWrapper(asFloat)
        is Double -> DoubleWrapper(asDouble)
        is Int -> IntWrapper(asInt)
        is Long -> LongWrapper(asLong)
        is Short -> ShortWrapper(asShort)
        is Byte -> ByteWrapper(asByte)
        else -> error("Don't know how to wrap $this")
    }

private fun JsonArray.toValue(): Value<*> =
    CollectionWrapper(map { it.asJsonObject.toValue() })

