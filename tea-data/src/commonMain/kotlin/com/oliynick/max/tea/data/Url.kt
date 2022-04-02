@file:Suppress("FunctionName")

package com.oliynick.max.tea.data

public expect class Url

public expect val Url.domain: String

public expect fun UrlFor(
    s: String
): Url

public expect fun Url.toExternalValue(): String