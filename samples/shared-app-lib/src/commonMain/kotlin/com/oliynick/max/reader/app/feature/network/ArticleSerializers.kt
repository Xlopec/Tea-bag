package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.reader.app.domain.Author
import com.oliynick.max.reader.app.domain.Description
import com.oliynick.max.reader.app.domain.Title
import com.oliynick.max.reader.app.domain.tryCreate
import com.oliynick.max.tea.data.Date
import com.oliynick.max.tea.data.Url
import com.oliynick.max.tea.data.UrlFor
import com.oliynick.max.tea.data.toExternalValue
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
