@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

import platform.Foundation.NSURL

@Suppress("CONFLICTING_OVERLOADS")
actual typealias Url = NSURL

actual fun UrlFor(
    s: String
): Url = NSURL(string = s)

actual fun Url.toExternalValue(): String = toString()