package com.oliynick.max.elm.time.travel.app.transport.serialization

import com.google.gson.*
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.gson.Gson
import com.oliynick.max.elm.time.travel.gson.JsonObject

internal val GSON by lazy {
    Gson {}
}

fun Value<*>.toJsonElement(): JsonElement =
    when (this) {
        is PrimitiveWrapper<*> -> toJsonElement()
        is Null -> toJsonElement()
        is CollectionWrapper -> this.toJsonElement()
        is Ref -> this.toJsonElement()
    }

private fun Null.toJsonElement(): JsonObject = JsonObject {
    addProperty("@type", type.name)
    add("@value", JsonNull.INSTANCE)
}

private fun PrimitiveWrapper<*>.toJsonElement(): JsonObject = JsonObject {

    addProperty("@type", type.name)

    val property = when (this@toJsonElement) {
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

    add("@value", property)
}

fun JsonObject.toValue(): Value<*> {

    val value: JsonElement? = get("@value")
    val type = Type.of(get("@type").asString)

    return when {
        value == null || value.isJsonNull -> Null(type)
        value.isJsonPrimitive -> value.asJsonPrimitive.toValue(type)
        value.isJsonArray -> value.asJsonArray.toValue(type)
        value.isJsonObject -> value.asJsonObject.toValueInner(type)
        else -> error("Don't know how to deserialize $this")
    }
}

private fun Ref.toJsonElement(): JsonElement {
    return JsonObject().apply {

        addProperty("@type", type.name)

        val nested = JsonObject {
            for (property in properties) {
                add(property.name, property.v.toJsonElement())
            }
        }

        add("@value", nested)
    }
}

private fun CollectionWrapper.toJsonElement(): JsonObject = JsonObject {

    addProperty("@type", type.name)

    val arr = value.fold(JsonArray(value.size)) { acc, v ->
        acc.add(v.toJsonElement())
        acc
    }

    add("@value", arr)
}

private fun JsonObject.toValueInner(
    type: Type
): Ref {
    val entrySet = entrySet()

    val props = entrySet.mapTo(HashSet<Property<*>>(entrySet.size)) { entry ->

        Property(
            entry.key,
            entry.value.asJsonObject.toValue()
        )
    }

    return Ref(type, props)
}

// fixme add explicit type param
private fun JsonPrimitive.toValue(
    type: Type
): Value<*> = when {
    isBoolean -> BooleanWrapper(type, asBoolean)
    isString -> StringWrapper(type, asString)
    isNumber -> toNumberValue(type)
    else -> error("Don't know how to wrap $this")
}

private fun JsonPrimitive.toNumberValue(
    type: Type
): PrimitiveWrapper<*> =
    when (asNumber) {
        is Float -> FloatWrapper(type, asFloat)
        is Double -> DoubleWrapper(type, asDouble)
        is Int -> IntWrapper(type, asInt)
        is Long -> LongWrapper(type, asLong)
        is Short -> ShortWrapper(type, asShort)
        is Byte -> ByteWrapper(type, asByte)
        else -> error("Don't know how to wrap $this")
    }

private fun JsonArray.toValue(
    type: Type
): Value<*> =
    CollectionWrapper(type, map { it.asJsonObject.toValue() })

