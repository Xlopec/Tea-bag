package com.oliynick.max.tea.core.data

actual typealias Url = java.net.URL

actual fun UrlFor(
    s: String
): Url = java.net.URL(s)

actual typealias UUID = java.util.UUID

actual fun randomUUID(): UUID = java.util.UUID.randomUUID()