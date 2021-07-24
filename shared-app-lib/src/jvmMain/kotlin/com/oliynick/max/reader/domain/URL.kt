package com.oliynick.max.reader.domain

import java.net.URL
import java.util.*

actual typealias Url = URL

/*
actual class Url(
    private val impl: URL,
) {
    actual companion object {
        actual fun fromString(url: String): Url = Url(URL(url))
    }

    fun toExternalForm(): String =
        impl.toExternalForm()

    override fun equals(other: Any?): Boolean =
        if (other is Url) impl == other.impl
        else false


    override fun hashCode(): Int = impl.hashCode()

    override fun toString(): String =
        impl.toString()

    actual fun toExternalValue(): String = impl.toString()
}
*/

actual fun String.toUrl(): Url = URL(this)

actual fun Url.toExternalValue(): String = toString()
