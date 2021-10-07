package com.oliynick.max.reader.network

import com.oliynick.max.reader.domain.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

object AuthorSerializer : KSerializer<Author> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Author", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Author) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Author = Author(decoder.decodeString())

}

object DescriptionSerializer : KSerializer<Description> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Description", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Description) = encoder.encodeString(value.value)
    override fun deserialize(decoder: Decoder): Description = Description(decoder.decodeString())

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

    override fun serialize(encoder: Encoder, value: Url) = encoder.encodeString(value.toExternalValue())
    override fun deserialize(decoder: Decoder): Url = decoder.decodeString().toUrl()

}