package com.oliynick.max.reader.app.feature.network

import kotlinx.serialization.encoding.Encoder

fun Encoder.encodeNullableString(
    s: String?
) = s?.let(::encodeString) ?: encodeNull()