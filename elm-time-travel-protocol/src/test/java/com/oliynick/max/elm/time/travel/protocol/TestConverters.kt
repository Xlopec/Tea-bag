package com.oliynick.max.elm.time.travel.protocol

import protocol.Converter
import protocol.Converters
import protocol.StringWrapper
import protocol.wrap
import java.net.URL
import java.util.*

object URLConverter : Converter<URL, StringWrapper> {

    override fun from(v: StringWrapper, converters: Converters): URL? = URL(v.value)

    override fun to(t: URL, converters: Converters): StringWrapper =
        wrap(t.toExternalForm())

}

object UUIDConverter : Converter<UUID, StringWrapper> {
    override fun from(v: StringWrapper, converters: Converters): UUID =
        UUID.fromString(v.value)

    override fun to(t: UUID, converters: Converters): StringWrapper =
        wrap(t.toString())
}