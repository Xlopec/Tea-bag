package com.oliynick.max.reader.app.serialization

import com.google.gson.*
import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import com.oliynick.max.reader.app.domain.tryCreate
import com.oliynick.max.tea.data.Date
import com.oliynick.max.tea.data.Url
import java.lang.reflect.Type
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale.ENGLISH

// Gson adapters, needed for to enable debugging facilities

interface TypeAdapter<T> : JsonSerializer<T>, JsonDeserializer<T>

val ArticleAdapters = mapOf(
    String::class to StringAdapter,
    Url::class to UrlAdapter,
    Title::class to TitleAdapter,
    Author::class to AuthorAdapter,
    Description::class to DescriptionAdapter,
    Date::class to DateAdapter
)

private object StringAdapter : TypeAdapter<String> {
    override fun serialize(
        src: String,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = json.asString?.takeUnless(CharSequence::isNullOrEmpty)
}

private object UrlAdapter : TypeAdapter<Url> {
    override fun serialize(
        src: Url,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.toExternalForm())

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = URL(json.asString)
}

private object DateAdapter : TypeAdapter<Date> {

    private val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", ENGLISH)

    override fun serialize(
        src: Date,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(parser.format(src))

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = json.asString?.let(parser::parse)
}

private object TitleAdapter : TypeAdapter<Title> {
    override fun serialize(
        src: Title,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Title.tryCreate(json.asString)
}

private object AuthorAdapter : TypeAdapter<Author> {
    override fun serialize(
        src: Author,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Author.tryCreate(json.asString)
}

private object DescriptionAdapter : TypeAdapter<Description> {
    override fun serialize(
        src: Description,
        typeOfSrc: Type?,
        context: JsonSerializationContext?,
    ) =
        JsonPrimitive(src.value)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ) = Description.tryCreate(json.asString)
}