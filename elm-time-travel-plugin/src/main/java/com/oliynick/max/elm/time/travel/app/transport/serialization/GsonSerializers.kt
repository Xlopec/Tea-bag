package com.oliynick.max.elm.time.travel.app.transport.serialization

import com.google.gson.*
import com.oliynick.max.elm.time.travel.app.domain.cms.*
import com.oliynick.max.elm.time.travel.gson.gson
import protocol.Json

internal val GSON by lazy { gson() }

//todo make private
internal fun Gson.fromRaw(
    any: Any
): Value<*> = toJsonTree(any).toValue()

internal fun Gson.asJson(
    value: Value<*>
): Json = toJson(asJsonElement(value))

private fun Gson.asJsonElement(
    value: Value<*>
): JsonElement =
    when (value) {
        is IntWrapper -> JsonPrimitive(value.value)
        is ByteWrapper -> JsonPrimitive(value.value)
        is ShortWrapper -> JsonPrimitive(value.value)
        is CharWrapper -> JsonPrimitive(value.value)
        is LongWrapper -> JsonPrimitive(value.value)
        is DoubleWrapper -> JsonPrimitive(value.value)
        is FloatWrapper -> JsonPrimitive(value.value)
        is StringWrapper -> JsonPrimitive(value.value)
        is BooleanWrapper -> JsonPrimitive(value.value)
        is Null -> JsonNull.INSTANCE
        is CollectionWrapper -> asJsonElement(value)
        is Ref -> asJsonElement(value)
    }

private fun Gson.asJsonElement(
    value: Ref
): JsonElement {
    return JsonObject().apply {
        for (property in value.properties) {
            add(property.name, asJsonElement(property.v))
        }
    }
}

private fun Gson.asJsonElement(
    value: CollectionWrapper
): JsonElement =
    value.value.fold(JsonArray(value.value.size)) { acc, v ->
        acc.add(toJson(v))
        acc
    }

fun JsonElement.toValue(): Value<*> =
    when {
        isJsonNull -> Null
        isJsonObject -> asJsonObject.toValue()
        isJsonPrimitive -> asJsonPrimitive.toValue()
        isJsonArray -> asJsonArray.toValue()
        else -> error("Should never happen $this")
    }

private fun JsonObject.toValue(): Ref {

    val entrySet = entrySet()

    return Ref(
        entrySet.mapTo(HashSet<Property<*>>(entrySet.size)) { entry ->
            Property(
                entry.key,
                entry.value.toValue()
            )
        }
    )
}

private fun JsonPrimitive.toValue(): Value<*> =
    when {
        isBoolean -> BooleanWrapper.of(asBoolean)
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
    CollectionWrapper(map { it.toValue() })

