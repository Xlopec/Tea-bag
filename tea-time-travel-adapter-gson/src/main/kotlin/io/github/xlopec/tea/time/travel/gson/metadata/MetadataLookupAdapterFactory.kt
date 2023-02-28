package io.github.xlopec.tea.time.travel.gson.metadata

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal object MetadataLookupAdapterFactory : TypeAdapterFactory {

    override fun <T> create(
        gson: Gson,
        type: TypeToken<T>,
    ): TypeAdapter<T> = with(gson) { with(this) { MetadataLookupTypeAdapter(type) } }

}

context (Gson, TypeAdapterFactory)
private class MetadataLookupTypeAdapter<T>(
    private val type: TypeToken<T>,
) : TypeAdapter<T>() {

    private val elementAdapter: TypeAdapter<JsonElement> = getAdapter(JsonElement::class.java)

    override fun write(out: JsonWriter, value: T) =
        getDelegateAdapter(this@TypeAdapterFactory, type).write(out, value)

    override fun read(`in`: JsonReader): T {
        val element = elementAdapter.read(`in`)

        return if (element.isJsonObject && element.asJsonObject.has(Metadata)) {
            val parseObject = element.asJsonObject[Metadata].asJsonObject
            val type = parseObject[FullType].asJsonObject.fromJsonTypeTree()
            val token = TypeToken.get(type)

            getDelegateAdapter(this@TypeAdapterFactory, token as TypeToken<T>).fromJsonTree(element)
        } else {
            getDelegateAdapter(this@TypeAdapterFactory, type).fromJsonTree(element)
        }
    }

}

public fun JsonObject.typeTokenOrNull(): TypeToken<*>? {
    if (has(Metadata)) {
        val parseObject = this[Metadata].asJsonObject
        val type = parseObject[FullType].asJsonObject.fromJsonTypeTree()
        val token = TypeToken.get(type)

        return token
    }

    return null
}

