package com.oliynick.max.entities.shared

public expect class UUID

public expect fun randomUUID(): UUID

public expect fun String.toUUID(): UUID

public expect fun UUID.toHumanReadable(): String