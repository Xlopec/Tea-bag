package com.oliynick.max.elm.time.travel.gson.serialization.serializer

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.Type

object PersistentListSerializer : JsonSerializer<PersistentList<*>>,
    JsonDeserializer<PersistentList<*>> {

    val typeToken = object : TypeToken<Any?>() {}

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): PersistentList<*> {

        val l = json.asJsonArray.asSequence()
            .map { e -> e.asJsonObject }
            .map { o ->

                val deser = context.deserialize<Any?>(o["@value"], o["@type"].asString.let { Class.forName(it) })



                deser
            }.toList()

        return l.toPersistentList()
        //context.deserialize<Collection<Any?>>(json, Collection::class.java).toPersistentList()
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
