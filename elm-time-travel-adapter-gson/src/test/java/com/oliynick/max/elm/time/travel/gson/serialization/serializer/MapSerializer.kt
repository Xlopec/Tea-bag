package com.oliynick.max.elm.time.travel.gson.serialization.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object MapDeserializer : JsonDeserializer<Map<*, *>> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Map<*, *> {

        val genericKeyArgType = (typeOfT as ParameterizedType).actualTypeArguments[1] as Class<*>

        return json.asJsonObject.entrySet()
            .associate { (k, v) ->
                (if (k == "null") null else k) to context.deserialize<Any?>(
                    v,
                    genericKeyArgType
                )
            }
    }

}
