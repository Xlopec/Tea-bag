package com.oliynick.max.reader.domain

import platform.Foundation.NSDate

actual class CommonDate(
    val impl: NSDate
) {
    actual companion object {
        actual fun now(): CommonDate = CommonDate(NSDate())
    }

    override fun equals(other: Any?): Boolean =
        if (other is CommonDate) impl == other.impl
        else false


    override fun hashCode(): Int = impl.hashCode()

    override fun toString(): String =
        impl.toString()
}