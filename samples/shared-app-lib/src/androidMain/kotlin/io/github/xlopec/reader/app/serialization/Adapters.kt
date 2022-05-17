/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.reader.app.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.github.xlopec.reader.app.model.Author
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.reader.app.model.tryCreate
import io.github.xlopec.tea.data.Date
import io.github.xlopec.tea.data.Url
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
