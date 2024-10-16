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

import io.github.xlopec.reader.app.model.SourceDescription
import io.github.xlopec.reader.app.model.SourceId
import io.github.xlopec.reader.app.model.SourceName
import io.github.xlopec.reader.app.model.tryCreate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object SourceIdSerializer : KSerializer<SourceId> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceId): Unit =
        encoder.encodeString(value.value)

    override fun deserialize(decoder: Decoder): SourceId = SourceId(decoder.decodeString())
}

public object SourceNameSerializer : KSerializer<SourceName> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceName", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceName): Unit =
        encoder.encodeNullableString(value.value)

    override fun deserialize(decoder: Decoder): SourceName = SourceName(decoder.decodeString())
}

public object SourceDescriptionSerializer : KSerializer<SourceDescription?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("SourceDescription", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SourceDescription?): Unit =
        encoder.encodeNullableString(value?.value)

    override fun deserialize(decoder: Decoder): SourceDescription? = tryCreate(decoder.decodeString(), ::SourceDescription)
}
