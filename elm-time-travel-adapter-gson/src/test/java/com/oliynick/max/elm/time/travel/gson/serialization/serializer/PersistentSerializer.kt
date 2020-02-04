package com.oliynick.max.elm.time.travel.gson.serialization.serializer

import com.google.gson.*
import com.oliynick.max.elm.time.travel.gson.isJsonPrimitive
import com.oliynick.max.elm.time.travel.gson.type
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
