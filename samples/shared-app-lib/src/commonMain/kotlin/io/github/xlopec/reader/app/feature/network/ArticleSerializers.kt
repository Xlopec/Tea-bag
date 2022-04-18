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

import io.github.xlopec.reader.app.domain.Author
import io.github.xlopec.reader.app.domain.Description
import io.github.xlopec.reader.app.domain.Title
import io.github.xlopec.reader.app.domain.tryCreate
import io.github.xlopec.tea.data.Date
import io.github.xlopec.tea.data.Url
import io.github.xlopec.tea.data.UrlFor
import io.github.xlopec.tea.data.toExternalValue
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object AuthorSerializer : KSerializer<Author?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Author", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Author?) =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder): Author? = Author.tryCreate(decoder.decodeString())

}

object DescriptionSerializer : KSerializer<Description?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Description", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Description?) =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder): Description? =
        Description.tryCreate(decoder.decodeString())

}

object TitleSerializer : KSerializer<Title> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Title", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Title) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Title = Title(decoder.decodeString())

}

expect fun Date.toJson(): String

expect fun String.toDate(): Date

object CommonDateSerializer : KSerializer<Date> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CommonDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) =
        encoder.encodeString(value.toJson())

    override fun deserialize(decoder: Decoder): Date =
        decoder.decodeString().toDate()

}

object UrlSerializer : KSerializer<Url> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Url", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Url) =
        encoder.encodeString(value.toExternalValue())

    override fun deserialize(decoder: Decoder): Url = UrlFor(decoder.decodeString())

}
