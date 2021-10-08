package com.oliynick.max.reader.app

expect class UUID

expect fun randomUUID(): UUID

expect fun String.toUUID(): UUID

expect fun UUID.toHumanReadable(): String
