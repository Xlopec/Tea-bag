@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

import platform.Foundation.NSURL

@Suppress("CONFLICTING_OVERLOADS")
public actual typealias Url = NSURL

public actual fun UrlFor(
    s: String
): Url = NSURL(string = s)

public actual fun Url.toExternalValue(): String = toString()