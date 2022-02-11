package com.oliynick.max.entities.shared

public expect class UUID

@Deprecated("remove", ReplaceWith("UUID()"))
public expect fun randomUUID(): UUID

public fun UUID(): UUID = randomUUID()

public fun UUID(
    value: String
): UUID = value.toUUID()

@Deprecated("remove")
public expect fun String.toUUID(): UUID

public expect fun UUID.toHumanReadable(): String