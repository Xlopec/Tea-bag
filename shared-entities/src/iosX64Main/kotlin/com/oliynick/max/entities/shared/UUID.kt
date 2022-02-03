package com.oliynick.max.entities.shared

import platform.Foundation.NSUUID

public actual typealias UUID = NSUUID

public actual fun randomUUID(): UUID = UUID()

public actual fun String.toUUID(): UUID = UUID(this)

public actual fun UUID.toHumanReadable(): String = UUIDString