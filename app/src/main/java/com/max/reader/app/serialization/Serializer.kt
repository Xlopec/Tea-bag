package com.max.reader.app.serialization

import com.google.gson.*
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.Type

interface Serializer<T> : JsonSerializer<T>, JsonDeserializer<T>

object PersistentListSerializer : Serializer<PersistentList<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext,
    ): PersistentList<*> = json.asJsonArray.map { element ->
        // For our app we know that we always deal with objects,
        // so it's safe to access "@type" property without additional checks
        context.deserialize<Any?>(element, Class.forName(element.asJsonObject["@type"].asString))
    }.toPersistentList()

    override fun serialize(
        src: PersistentList<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext,
    ): JsonElement = JsonArray()
        .apply { src.map(context::serialize).forEach(::add) }
}
