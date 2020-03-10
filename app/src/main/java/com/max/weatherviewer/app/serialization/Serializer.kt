package com.max.weatherviewer.app.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import java.lang.reflect.Type

interface Serializer<T> : JsonSerializer<T>, JsonDeserializer<T>

object PersistentListSerializer : Serializer<PersistentList<*>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): PersistentList<*> = context.deserialize<Collection<Any?>>(json, Collection::class.java).toPersistentList()

    /*
    json.asJsonArray.map {

            it.asJsonObject.run {
                context.deserialize<Any?>(this["value"], Class.forName(this["type"].asString))
            }


        }.toPersistentList()
    }
     */

    override fun serialize(
        src: PersistentList<*>,
        typeOfSrc: Type?,
        context: JsonSerializationContext
    ): JsonElement = //context.serialize(src, Collection::class.java)


        JsonArray().apply {
            for (v in src) {
                add(context.serialize(v))
                /*add(JsonObject().also {
                    it.addProperty("type", v!!::class.java.name)
                    it.add("value", context.serialize(v))
                }
                )*/
            }
        }


}
