package com.oliynick.max.elm.time.travel.gson

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class TypeAppenderAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T> =
        object : TypeAdapter<T>() {

            val elementAdapter = gson.getAdapter(JsonElement::class.java)
            val delegate = gson.getDelegateAdapter(this@TypeAppenderAdapterFactory, type)

            override fun write(out: JsonWriter, value: T) {

                val jsonObject = JsonObject {
                    addProperty(
                        "@type",
                        (if (value == null) Any::class else value!!::class).java.name
                    )
                    add("@value", delegate.toJsonTree(value))
                }

                elementAdapter.write(out, jsonObject)
            }

            override fun read(`in`: JsonReader): T = TODO("not implemented")



        }

}
