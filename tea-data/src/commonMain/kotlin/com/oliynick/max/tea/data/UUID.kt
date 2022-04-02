@file:Suppress("FunctionName")

package com.oliynick.max.tea.data

public expect class UUID

public expect fun RandomUUID(): UUID

public expect fun UUIDFrom(
    value: String
): UUID

public expect fun UUID.toHumanReadable(): String