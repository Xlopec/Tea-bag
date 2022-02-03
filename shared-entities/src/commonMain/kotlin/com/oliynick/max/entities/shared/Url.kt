@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

public expect class Url

public expect fun UrlFor(
    s: String
): Url

public expect fun Url.toExternalValue(): String