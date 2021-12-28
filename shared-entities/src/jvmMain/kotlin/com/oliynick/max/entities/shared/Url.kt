@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

actual typealias Url = java.net.URL

actual fun UrlFor(
    s: String
): Url = java.net.URL(s)

actual fun Url.toExternalValue(): String = toExternalForm()