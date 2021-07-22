package com.oliynick.max.reader.domain

import platform.Foundation.NSURL

actual class Url(
    val impl: NSURL
) {
    actual companion object {
        actual fun fromString(
            url: String
        ): Url = Url(NSURL(string = url))
    }

    override fun equals(other: Any?): Boolean =
        if (other is Url) impl == other.impl
        else false

    override fun hashCode(): Int = impl.hashCode()

    override fun toString(): String =
        impl.toString()

    actual fun toExternalValue(): String = impl.toString()
}