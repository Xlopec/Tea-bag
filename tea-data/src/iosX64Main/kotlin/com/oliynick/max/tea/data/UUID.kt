@file:Suppress("FunctionName")

package com.oliynick.max.tea.data

import platform.Foundation.NSUUID

public actual typealias UUID = NSUUID

public actual fun RandomUUID(): UUID = NSUUID()

public actual fun UUIDFrom(
    value: String,
): UUID = NSUUID(value)

public actual fun UUID.toHumanReadable(): String = UUIDString