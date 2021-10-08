package com.oliynick.max.reader.app

import platform.Foundation.NSUUID

actual typealias UUID = NSUUID

actual fun randomUUID(): UUID =
    UUID()

actual fun String.toUUID(): UUID =
    UUID(this)

actual fun UUID.toHumanReadable(): String = UUIDString