package com.oliynick.max.reader.domain

import java.net.URL
import java.util.*

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
}

actual class CommonDate(
    val impl: java.util.Date,
) {
    actual companion object {
        actual fun now(): CommonDate = CommonDate(java.util.Date())
    }

    override fun equals(other: Any?): Boolean =
        if (other is CommonDate) impl == other.impl
        else false


    override fun hashCode(): Int = impl.hashCode()

    override fun toString(): String =
        impl.toString()
}