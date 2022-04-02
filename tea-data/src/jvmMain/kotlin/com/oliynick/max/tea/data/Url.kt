@file:Suppress("FunctionName")

package com.oliynick.max.tea.data

public actual typealias Url = java.net.URL

public actual fun UrlFor(
    s: String
): Url = java.net.URL(s)

public actual fun Url.toExternalValue(): String = toExternalForm()

public actual val Url.domain: String
    get() = host