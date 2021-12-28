package com.oliynick.max.entities.shared

import java.util.UUID as JavaUUID

actual typealias UUID = java.util.UUID

actual fun randomUUID(): UUID =
    JavaUUID.randomUUID()

actual fun String.toUUID(): UUID =
    JavaUUID.fromString(this)

actual fun UUID.toHumanReadable(): String = toString()