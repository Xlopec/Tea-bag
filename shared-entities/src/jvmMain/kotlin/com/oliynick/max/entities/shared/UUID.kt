@file:Suppress("FunctionName")

package com.oliynick.max.entities.shared

import java.util.UUID as JavaUUID

public actual typealias UUID = java.util.UUID

public actual fun RandomUUID(): UUID =
    JavaUUID.randomUUID()

public actual fun UUIDFrom(
    value: String
): UUID = JavaUUID.fromString(value)

public actual fun UUID.toHumanReadable(): String = toString()