package com.oliynick.max.tea.core.debug.gson.serialization.serializer

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
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
