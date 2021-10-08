package com.oliynick.max.reader.app

actual typealias UUID = java.util.UUID

actual fun randomUUID(): UUID =
    java.util.UUID.randomUUID()

actual fun String.toUUID(): UUID =
    java.util.UUID.fromString(this)

actual fun UUID.toHumanReadable(): String =
    toString()