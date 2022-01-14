package com.oliynick.max.entities.shared

import java.util.UUID as JavaUUID

public actual typealias UUID = java.util.UUID

public actual fun randomUUID(): UUID =
    JavaUUID.randomUUID()

public actual fun String.toUUID(): UUID =
    JavaUUID.fromString(this)

public actual fun UUID.toHumanReadable(): String = toString()