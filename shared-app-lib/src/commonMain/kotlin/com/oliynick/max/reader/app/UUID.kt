package com.oliynick.max.reader.app

// fixme merge/move to shared common module/file

@Deprecated("remove")
expect class UUID

@Deprecated("remove")
expect fun randomUUID(): UUID

@Deprecated("remove")
expect fun String.toUUID(): UUID

@Deprecated("remove")
expect fun UUID.toHumanReadable(): String
