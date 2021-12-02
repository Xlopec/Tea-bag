package com.oliynick.max.reader.app

// fixme merge/move to shared common module/file

expect class UUID

expect fun randomUUID(): UUID

expect fun String.toUUID(): UUID

expect fun UUID.toHumanReadable(): String
