package com.oliynick.max.reader.domain

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