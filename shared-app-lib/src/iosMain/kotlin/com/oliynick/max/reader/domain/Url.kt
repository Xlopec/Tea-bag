package com.oliynick.max.reader.domain

import platform.Foundation.NSDate
import platform.Foundation.NSURL

@Suppress("CONFLICTING_OVERLOADS")
actual typealias Url = NSURL

actual fun String.toUrl(): Url = NSURL(string = this)

actual fun Url.toExternalValue(): String = toString()

