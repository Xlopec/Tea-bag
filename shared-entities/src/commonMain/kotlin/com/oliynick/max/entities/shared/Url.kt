@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

expect class Url

expect fun UrlFor(
    s: String
): Url

expect fun Url.toExternalValue(): String