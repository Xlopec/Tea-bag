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

package io.github.xlopec.reader.app.feature.network

import io.github.xlopec.reader.app.model.Author
import io.github.xlopec.reader.app.model.Description
import io.github.xlopec.reader.app.model.Title
import io.github.xlopec.reader.app.model.tryCreate
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.UrlFor
import io.github.xlopec.tea.data.toExternalValue
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object AuthorSerializer : KSerializer<Author?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Author", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Author?): Unit =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder): Author? = Author.tryCreate(decoder.decodeString())
}

public object DescriptionSerializer : KSerializer<Description?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Description", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Description?): Unit =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder): Description? =
        Description.tryCreate(decoder.decodeString())
}

public object TitleSerializer : KSerializer<Title> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Title", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Title): Unit = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Title = Title(decoder.decodeString())
}

public object CommonDateSerializer : KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CommonDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime): Unit =
        encoder.encodeString(value.toJson())

    override fun deserialize(decoder: Decoder): LocalDateTime =
        decoder.decodeString().toDate()
}

public object UrlSerializer : KSerializer<Url> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url): Unit =
        encoder.encodeString(value.toExternalValue())

    override fun deserialize(decoder: Decoder): Url = UrlFor(decoder.decodeString())
}

internal fun LocalDateTime.toJson(): String = toInstant(TimeZone.UTC)
    .format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)

internal fun String.toDate(): LocalDateTime {
    return Instant.parse(this, DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET)
        .toLocalDateTime(TimeZone.currentSystemDefault())
}
