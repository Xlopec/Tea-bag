package com.oliynick.max.reader.domain

import platform.Foundation.NSDate
import platform.Foundation.NSURL

actual class Url(
    val impl: NSURL
) {
    actual companion object {
        actual fun fromString(
            url: String
        ): Url = Url(NSURL(string = url))
    }
}

actual class CommonDate(
    val impl: NSDate
) {
    actual companion object {
        actual fun now(): CommonDate = CommonDate(NSDate())
    }
}