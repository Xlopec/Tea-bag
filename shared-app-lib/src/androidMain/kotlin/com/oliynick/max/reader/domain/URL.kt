package com.oliynick.max.reader.domain

import java.net.URL
import java.util.*

actual typealias Url = URL

actual fun String.toUrl(): Url = URL(this)

actual fun Url.toExternalValue(): String = toString()
