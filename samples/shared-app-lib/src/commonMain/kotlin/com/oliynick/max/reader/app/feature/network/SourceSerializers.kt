package com.oliynick.max.reader.app.feature.network

import com.oliynick.max.reader.app.domain.SourceDescription
import com.oliynick.max.reader.app.domain.SourceId
import com.oliynick.max.reader.app.domain.SourceName
import com.oliynick.max.reader.app.domain.tryCreate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SourceIdSerializer : KSerializer<SourceId> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceId) =
        encoder.encodeString(value.value)

    override fun deserialize(decoder: Decoder) = SourceId(decoder.decodeString())

}

object SourceNameSerializer : KSerializer<SourceName> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceName", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceName) =
        encoder.encodeNullableString(value.value)

    override fun deserialize(decoder: Decoder) = SourceName(decoder.decodeString())

}

object SourceDescriptionSerializer : KSerializer<SourceDescription?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceDescription", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceDescription?) =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder) = tryCreate(decoder.decodeString(), ::SourceDescription)

}